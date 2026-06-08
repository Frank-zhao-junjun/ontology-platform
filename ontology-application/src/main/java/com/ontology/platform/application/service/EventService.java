package com.ontology.platform.application.service;

import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.entity.DomainEventDefinition;
import com.ontology.platform.domain.entity.EventHandler;
import com.ontology.platform.domain.entity.EventRoute;
import com.ontology.platform.domain.repository.DomainEventRepository;
import com.ontology.platform.domain.repository.EventHandlerRepository;
import com.ontology.platform.domain.repository.EventRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {
    private final EventRouteRepository routeRepo;
    private final EventHandlerRepository handlerRepo;
    private final DomainEventRepository eventRepo;
    private final BehaviorService behaviorService;

    // ── Event Routes (US-E03) ──

    public EventRoute createEventRoute(String contextId, String manifestCode, String sourceEventId,
                                       String routeTargetsJson, String filterConditionsJson) {
        if (routeRepo.existsByContextIdAndManifestCode(contextId, manifestCode)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "路由 manifestCode 已存在: " + manifestCode);
        }
        // verify source event exists
        eventRepo.findById(sourceEventId)
                .orElseThrow(() -> new ResourceNotFoundException("事件不存在: " + sourceEventId));
        EventRoute route = EventRoute.create(contextId, manifestCode, sourceEventId,
                routeTargetsJson, filterConditionsJson);
        routeRepo.save(route);
        return route;
    }

    public List<EventRoute> listEventRoutes(String contextId) {
        return routeRepo.findByContextId(contextId);
    }

    public List<EventRoute> listEventRoutesForEvent(String eventId) {
        return routeRepo.findBySourceEventId(eventId);
    }

    // ── Event Handlers (US-E04) ──

    public EventHandler createEventHandler(String contextId, String manifestCode, String eventId,
                                           String handlerBehaviorId, String scenarioId,
                                           String preconditionState, int priority, String executionMode) {
        if (handlerRepo.existsByContextIdAndManifestCode(contextId, manifestCode)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "处理器 manifestCode 已存在: " + manifestCode);
        }
        // verify event exists
        eventRepo.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("事件不存在: " + eventId));
        // verify handler behavior exists and is invocable by event
        var behavior = behaviorService.getAction(handlerBehaviorId);
        if (behavior.getInvocationMode().name().equals("AGENT_ONLY")) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "行为 " + behavior.getName() + " 仅支持主动调用模式，不能作为事件处理器");
        }
        EventHandler handler = EventHandler.create(contextId, manifestCode, eventId,
                handlerBehaviorId, scenarioId, preconditionState, priority, executionMode);
        handlerRepo.save(handler);
        return handler;
    }

    public List<EventHandler> listEventHandlers(String contextId) {
        return handlerRepo.findByContextId(contextId);
    }

    public List<EventHandler> listEventHandlersForEvent(String eventId) {
        return handlerRepo.findByEventId(eventId);
    }

    /**
     * 获取事件处理器矩阵 (事件×场景×状态→行为)。
     * US-E04 AC-2: 表格形式展示所有处理器映射。
     */
    public Map<String, Object> getHandlerMatrix(String contextId) {
        List<EventHandler> handlers = handlerRepo.findByContextId(contextId);
        List<DomainEventDefinition> events = eventRepo.findByContextId(contextId);

        // build event name lookup
        Map<String, String> eventNames = events.stream()
                .collect(Collectors.toMap(DomainEventDefinition::getId, DomainEventDefinition::getName));

        // group handlers by event → scenario → state
        List<Map<String, Object>> matrix = new ArrayList<>();
        for (EventHandler h : handlers) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", h.getId());
            row.put("manifestCode", h.getManifestCode());
            row.put("eventName", eventNames.getOrDefault(h.getEventId(), h.getEventId()));
            row.put("scenarioId", h.getScenarioId() != null ? h.getScenarioId() : "*");
            row.put("preconditionState", h.getPreconditionState() != null ? h.getPreconditionState() : "*");
            row.put("handlerBehaviorId", h.getHandlerBehaviorId());
            row.put("priority", h.getPriority());
            row.put("executionMode", h.getExecutionMode());
            matrix.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("eventCount", events.size());
        result.put("handlerCount", handlers.size());
        result.put("matrix", matrix);
        return result;
    }
}

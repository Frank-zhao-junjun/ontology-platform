package com.ontology.platform.api.controller;

import com.ontology.platform.application.service.BehaviorService;
import com.ontology.platform.application.service.EventService;
import com.ontology.platform.application.service.MetricService;
import com.ontology.platform.common.enums.EventType;
import com.ontology.platform.common.enums.InvocationMode;
import com.ontology.platform.domain.entity.DomainEventDefinition;
import com.ontology.platform.domain.entity.EventHandler;
import com.ontology.platform.domain.entity.EventRoute;
import com.ontology.platform.domain.entity.Metric;
import com.ontology.platform.domain.entity.OntologyAction;
import com.ontology.platform.domain.entity.ValidationRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/contexts/{contextId}")
@RequiredArgsConstructor
@Tag(name = "Behavior", description = "行为 / 规则 / 事件 / 指标 (US-B01/B03/B05/E01/E03/E04)")
public class BehaviorController {
    private final BehaviorService behaviorService;
    private final MetricService metricService;
    private final EventService eventService;

    @PostMapping("/validation-rules")
    @Operation(summary = "创建校验规则 (US-B03)")
    public ResponseEntity<Map<String, Object>> createRule(@PathVariable String contextId,
                                                           @RequestBody Map<String, Object> body) {
        ValidationRule r = behaviorService.createRule(contextId,
                str(body, "manifestCode"), str(body, "name"), str(body, "ruleType"),
                str(body, "expressionJson", "{}"), str(body, "errorMessage"),
                str(body, "failurePayloadSchema"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("code", 0, "message", "success", "data", toRuleMap(r)));
    }

    @GetMapping("/validation-rules")
    public ResponseEntity<Map<String, Object>> listRules(@PathVariable String contextId) {
        var data = behaviorService.listRules(contextId).stream().map(this::toRuleMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    @PostMapping("/actions")
    @Operation(summary = "创建行为 (US-B01)")
    public ResponseEntity<Map<String, Object>> createAction(@PathVariable String contextId,
                                                            @RequestBody Map<String, Object> body) {
        InvocationMode mode = body.get("invocationMode") != null
                ? InvocationMode.fromCode(body.get("invocationMode").toString()) : InvocationMode.BOTH;
        OntologyAction a = behaviorService.createAction(contextId,
                str(body, "manifestCode"), str(body, "name"), str(body, "nameEn"),
                str(body, "description"), str(body, "aggregateRootId"), mode,
                str(body, "parametersJson", "[]"), str(body, "publishesEventIdsJson", "[]"),
                str(body, "allowedStateFromJson", "[]"), str(body, "businessScenarioIdsJson", "[]"),
                str(body, "mcpToolName"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("code", 0, "message", "success", "data", toActionMap(a)));
    }

    @GetMapping("/actions")
    public ResponseEntity<Map<String, Object>> listActions(@PathVariable String contextId) {
        var data = behaviorService.listActions(contextId).stream().map(this::toActionMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    @PutMapping("/actions/{actionId}/rules")
    @Operation(summary = "关联校验规则到行为，有序 (US-B03 AC-3)")
    public ResponseEntity<Map<String, Object>> linkRules(@PathVariable String contextId,
                                                         @PathVariable String actionId,
                                                         @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> ruleIds = (List<String>) body.get("ruleIds");
        behaviorService.linkRulesToAction(actionId, ruleIds);
        return ResponseEntity.ok(Map.of("code", 0, "message", "success",
                "data", Map.of("actionId", actionId, "ruleIds", behaviorService.listRuleIdsForAction(actionId))));
    }

    @PostMapping("/domain-events")
    @Operation(summary = "创建领域事件 (US-E01)")
    public ResponseEntity<Map<String, Object>> createEvent(@PathVariable String contextId,
                                                           @RequestBody Map<String, Object> body) {
        DomainEventDefinition e = behaviorService.createDomainEvent(contextId,
                str(body, "manifestCode"), str(body, "name"), str(body, "nameEn"),
                EventType.fromCode(str(body, "eventType", "DOMAIN_EVENT")),
                str(body, "aggregateRootId"), str(body, "triggerActionId"),
                str(body, "payloadSchemaJson", "{}"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("code", 0, "message", "success", "data", toEventMap(e)));
    }

    @GetMapping("/domain-events")
    public ResponseEntity<Map<String, Object>> listEvents(@PathVariable String contextId) {
        var data = behaviorService.listDomainEvents(contextId).stream().map(this::toEventMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    @PostMapping("/event-routes")
    @Operation(summary = "创建事件路由 (US-E03)")
    public ResponseEntity<Map<String, Object>> createEventRoute(@PathVariable String contextId,
                                                                 @RequestBody Map<String, Object> body) {
        EventRoute r = eventService.createEventRoute(contextId,
                str(body, "manifestCode"), str(body, "sourceEventId"),
                str(body, "routeTargetsJson", "[]"), str(body, "filterConditionsJson", "[]"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("code", 0, "message", "success", "data", toRouteMap(r)));
    }

    @GetMapping("/event-routes")
    public ResponseEntity<Map<String, Object>> listEventRoutes(@PathVariable String contextId) {
        var data = eventService.listEventRoutes(contextId).stream().map(this::toRouteMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    @PostMapping("/event-handlers")
    @Operation(summary = "创建事件处理器 (US-E04)")
    public ResponseEntity<Map<String, Object>> createEventHandler(@PathVariable String contextId,
                                                                   @RequestBody Map<String, Object> body) {
        EventHandler h = eventService.createEventHandler(contextId,
                str(body, "manifestCode"), str(body, "eventId"),
                str(body, "handlerBehaviorId"), str(body, "scenarioId"),
                str(body, "preconditionState"),
                intVal(body, "priority", 100), str(body, "executionMode", "SYNC"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("code", 0, "message", "success", "data", toHandlerMap(h)));
    }

    @GetMapping("/event-handlers")
    public ResponseEntity<Map<String, Object>> listEventHandlers(@PathVariable String contextId) {
        var data = eventService.listEventHandlers(contextId).stream().map(this::toHandlerMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    @GetMapping("/event-handlers/matrix")
    @Operation(summary = "事件处理器矩阵 (US-E04 AC-2)")
    public ResponseEntity<Map<String, Object>> getHandlerMatrix(@PathVariable String contextId) {
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", eventService.getHandlerMatrix(contextId)));
    }
    @Operation(summary = "创建业务指标 (US-B05)")
    public ResponseEntity<Map<String, Object>> createMetric(@PathVariable String contextId,
                                                            @RequestBody Map<String, Object> body) {
        Metric m = metricService.createMetric(contextId,
                str(body, "manifestCode"), str(body, "name"), str(body, "nameEn"),
                str(body, "formula"), str(body, "dataSourceRefJson", "[]"),
                str(body, "aggregationDimensionsJson", "[]"), str(body, "period"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("code", 0, "message", "success", "data", toMetricMap(m)));
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> listMetrics(@PathVariable String contextId) {
        var data = metricService.listMetrics(contextId).stream().map(this::toMetricMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    private Map<String, Object> toMetricMap(Metric m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("manifestCode", m.getManifestCode());
        map.put("name", m.getName());
        map.put("nameEn", m.getNameEn());
        map.put("formula", m.getFormula());
        map.put("period", m.getPeriod());
        return map;
    }

    private Map<String, Object> toRuleMap(ValidationRule r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("manifestCode", r.getManifestCode());
        m.put("name", r.getName());
        m.put("ruleType", r.getRuleType());
        m.put("enabled", r.isEnabled());
        return m;
    }

    private Map<String, Object> toActionMap(OntologyAction a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("manifestCode", a.getManifestCode());
        m.put("name", a.getName());
        m.put("nameEn", a.getNameEn());
        m.put("aggregateRootId", a.getAggregateRootId());
        m.put("invocationMode", a.getInvocationMode().name());
        return m;
    }

    private Map<String, Object> toEventMap(DomainEventDefinition e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("manifestCode", e.getManifestCode());
        m.put("name", e.getName());
        m.put("nameEn", e.getNameEn());
        m.put("eventType", e.getEventType().getCode());
        m.put("triggerActionId", e.getTriggerActionId() != null ? e.getTriggerActionId() : "");
        return m;
    }

    private Map<String, Object> toRouteMap(EventRoute r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("manifestCode", r.getManifestCode());
        m.put("sourceEventId", r.getSourceEventId());
        return m;
    }

    private Map<String, Object> toHandlerMap(EventHandler h) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", h.getId());
        m.put("manifestCode", h.getManifestCode());
        m.put("eventId", h.getEventId());
        m.put("handlerBehaviorId", h.getHandlerBehaviorId());
        m.put("scenarioId", h.getScenarioId());
        m.put("preconditionState", h.getPreconditionState());
        m.put("priority", h.getPriority());
        m.put("executionMode", h.getExecutionMode());
        return m;
    }

    private static int intVal(Map<String, Object> body, String key, int def) {
        Object v = body.get(key);
        if (v instanceof Number n) return n.intValue();
        if (v instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    private static String str(Map<String, Object> body, String key) {
        return str(body, key, null);
    }

    private static String str(Map<String, Object> body, String key, String def) {
        Object v = body.get(key);
        return v != null ? v.toString() : def;
    }
}

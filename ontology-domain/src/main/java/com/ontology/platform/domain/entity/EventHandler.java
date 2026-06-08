package com.ontology.platform.domain.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * 事件处理器矩阵定义 (US-E04)。
 * 事件×业务场景×对象状态→处理行为，形成三维矩阵。
 * 处理行为复用 B01 行为库。
 */
@Getter
public class EventHandler {
    private final String id, contextId, manifestCode;
    private final String eventId;
    private final String handlerBehaviorId;
    private final String scenarioId;
    private final String preconditionState;
    private final int priority;
    private final String executionMode;
    private final Instant createdAt;

    @Builder
    public EventHandler(String id, String contextId, String manifestCode,
                        String eventId, String handlerBehaviorId,
                        String scenarioId, String preconditionState,
                        int priority, String executionMode, Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.contextId = contextId;
        this.manifestCode = manifestCode;
        this.eventId = eventId;
        this.handlerBehaviorId = handlerBehaviorId;
        this.scenarioId = scenarioId;
        this.preconditionState = preconditionState;
        this.priority = priority > 0 ? priority : 100;
        this.executionMode = executionMode != null ? executionMode : "SYNC";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static EventHandler create(String contextId, String manifestCode,
                                      String eventId, String handlerBehaviorId,
                                      String scenarioId, String preconditionState,
                                      int priority, String executionMode) {
        return EventHandler.builder().contextId(contextId).manifestCode(manifestCode)
                .eventId(eventId).handlerBehaviorId(handlerBehaviorId)
                .scenarioId(scenarioId).preconditionState(preconditionState)
                .priority(priority).executionMode(executionMode).build();
    }
}

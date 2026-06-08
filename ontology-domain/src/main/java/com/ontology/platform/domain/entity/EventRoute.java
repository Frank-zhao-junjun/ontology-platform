package com.ontology.platform.domain.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * 事件路由定义 (US-E03)。
 * 为领域/集成事件定义路由规则——事件应被分发到哪些限界上下文和外部系统。
 */
@Getter
public class EventRoute {
    private final String id, contextId, manifestCode;
    private final String sourceEventId;
    private final String routeTargetsJson;
    private final String filterConditionsJson;
    private final Instant createdAt;

    @Builder
    public EventRoute(String id, String contextId, String manifestCode,
                      String sourceEventId, String routeTargetsJson,
                      String filterConditionsJson, Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.contextId = contextId;
        this.manifestCode = manifestCode;
        this.sourceEventId = sourceEventId;
        this.routeTargetsJson = routeTargetsJson != null ? routeTargetsJson : "[]";
        this.filterConditionsJson = filterConditionsJson != null ? filterConditionsJson : "[]";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static EventRoute create(String contextId, String manifestCode,
                                    String sourceEventId, String routeTargetsJson,
                                    String filterConditionsJson) {
        return EventRoute.builder().contextId(contextId).manifestCode(manifestCode)
                .sourceEventId(sourceEventId).routeTargetsJson(routeTargetsJson)
                .filterConditionsJson(filterConditionsJson).build();
    }
}

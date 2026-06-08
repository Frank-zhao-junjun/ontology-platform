package com.ontology.platform.domain.entity;

import com.ontology.platform.common.enums.EventType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class DomainEventDefinition {
    private final String id, contextId, manifestCode, name, nameEn;
    private final EventType eventType;
    private final String aggregateRootId, triggerActionId, payloadSchemaJson;
    private final Instant createdAt;

    @Builder
    public DomainEventDefinition(String id, String contextId, String manifestCode, String name, String nameEn,
                                 EventType eventType,
                                 String aggregateRootId, String triggerActionId, String payloadSchemaJson,
                                 Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.contextId = contextId;
        this.manifestCode = manifestCode;
        this.name = name;
        this.nameEn = nameEn;
        this.eventType = eventType != null ? eventType : EventType.DOMAIN_EVENT;
        this.aggregateRootId = aggregateRootId;
        this.triggerActionId = triggerActionId;
        this.payloadSchemaJson = payloadSchemaJson != null ? payloadSchemaJson : "{}";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static DomainEventDefinition create(String contextId, String manifestCode, String name, String nameEn,
                                               EventType eventType,
                                               String aggregateRootId, String triggerActionId,
                                               String payloadSchemaJson) {
        return DomainEventDefinition.builder().contextId(contextId).manifestCode(manifestCode).name(name)
                .nameEn(nameEn).eventType(eventType)
                .aggregateRootId(aggregateRootId).triggerActionId(triggerActionId)
                .payloadSchemaJson(payloadSchemaJson).build();
    }
}

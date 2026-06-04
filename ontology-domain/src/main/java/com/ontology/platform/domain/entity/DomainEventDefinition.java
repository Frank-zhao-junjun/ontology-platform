package com.ontology.platform.domain.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class DomainEventDefinition {
    private final String id, contextId, manifestCode, name, nameEn;
    private final String aggregateRootId, triggerActionId, payloadSchemaJson;
    private final Instant createdAt;

    @Builder
    public DomainEventDefinition(String id, String contextId, String manifestCode, String name, String nameEn,
                                 String aggregateRootId, String triggerActionId, String payloadSchemaJson,
                                 Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.contextId = contextId;
        this.manifestCode = manifestCode;
        this.name = name;
        this.nameEn = nameEn;
        this.aggregateRootId = aggregateRootId;
        this.triggerActionId = triggerActionId;
        this.payloadSchemaJson = payloadSchemaJson != null ? payloadSchemaJson : "{}";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static DomainEventDefinition create(String contextId, String manifestCode, String name, String nameEn,
                                               String aggregateRootId, String triggerActionId,
                                               String payloadSchemaJson) {
        return DomainEventDefinition.builder().contextId(contextId).manifestCode(manifestCode).name(name)
                .nameEn(nameEn).aggregateRootId(aggregateRootId).triggerActionId(triggerActionId)
                .payloadSchemaJson(payloadSchemaJson).build();
    }
}

package com.ontology.platform.domain.entity;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
public class ObjectTypeV2 {
    private final String id, contextId, aggregateRootId, parentObjectId;
    private final String name, code, objectKind, description;
    private String attributes; // JSON string for now (JSONB in production)
    private final Instant createdAt;

    @Builder
    public ObjectTypeV2(String id, String contextId, String aggregateRootId, String parentObjectId,
                      String name, String code, String objectKind, String description, String attributes) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.contextId = contextId; this.aggregateRootId = aggregateRootId;
        this.parentObjectId = parentObjectId; this.name = name; this.code = code;
        this.objectKind = objectKind != null ? objectKind : "ENTITY";
        this.description = description; this.attributes = attributes != null ? attributes : "[]";
        this.createdAt = Instant.now();
    }

    public static ObjectTypeV2 create(String contextId, String name, String code, String objectKind, String aggregateRootId) {
        return ObjectTypeV2.builder().contextId(contextId).name(name).code(code).objectKind(objectKind).aggregateRootId(aggregateRootId).build();
    }

    public void setAttributes(String attrs) { this.attributes = attrs; }
}

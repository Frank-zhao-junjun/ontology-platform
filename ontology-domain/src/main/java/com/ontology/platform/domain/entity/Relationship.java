package com.ontology.platform.domain.entity;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
public class Relationship {
    private final String id, contextId, sourceObjectId, targetObjectId;
    private final String name, code, cardinality, relationKind;
    private final boolean crossContext;
    private final String targetContextId;
    private final Instant createdAt;

    @Builder
    public Relationship(String id, String contextId, String sourceObjectId, String targetObjectId,
                        String name, String code, String cardinality, String relationKind,
                        Boolean crossContext, String targetContextId) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.contextId = contextId; this.sourceObjectId = sourceObjectId; this.targetObjectId = targetObjectId;
        this.name = name; this.code = code;
        this.cardinality = cardinality != null ? cardinality : "1:N";
        this.relationKind = relationKind != null ? relationKind : "REFERENCE";
        this.crossContext = crossContext != null ? crossContext : false;
        this.targetContextId = targetContextId;
        this.createdAt = Instant.now();
    }

    public static Relationship create(String contextId, String sourceId, String targetId,
                                       String name, String code, String cardinality, String relationKind) {
        return Relationship.builder().contextId(contextId).sourceObjectId(sourceId).targetObjectId(targetId)
                .name(name).code(code).cardinality(cardinality).relationKind(relationKind).build();
    }
}

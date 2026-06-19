package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class EpcEdge {
    private String id;
    private String chainId;
    private String sourceNodeId;
    private String targetNodeId;
    private String edgeType;
    private String label;
    private String conditionExpr;
    private String metadata;
    private Integer sortOrder;
    private Instant createdAt;
    private Instant updatedAt;

    public static EpcEdge create() {
        return EpcEdge.builder().id(UUID.randomUUID().toString())
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}

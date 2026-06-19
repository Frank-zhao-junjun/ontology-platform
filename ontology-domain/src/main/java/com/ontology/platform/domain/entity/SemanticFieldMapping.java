package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SemanticFieldMapping {
    private String id;
    private String entityId;
    private String fieldNameEn;
    private String businessTermId;
    private String mappingType;
    private String transformRule;
    private Instant createdAt;
    private Instant updatedAt;

    public static SemanticFieldMapping create() {
        return SemanticFieldMapping.builder().id(UUID.randomUUID().toString())
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}

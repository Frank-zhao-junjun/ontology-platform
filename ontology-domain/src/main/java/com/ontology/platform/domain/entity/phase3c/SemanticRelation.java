package com.ontology.platform.domain.entity.phase3c;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class SemanticRelation {
    private String id;
    private String sourceTermId;
    private String targetTermId;
    private String relationType;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public static SemanticRelation create() {
        return SemanticRelation.builder().id(UUID.randomUUID().toString())
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}

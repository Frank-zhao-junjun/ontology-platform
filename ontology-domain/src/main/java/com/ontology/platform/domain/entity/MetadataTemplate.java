package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataTemplate {
    private String id;
    private String ontologyId;
    private String name;
    private String nameEn;
    private String description;
    private String domain;
    private String templateType;
    private Instant createdAt;
    private Instant updatedAt;

    public static MetadataTemplate create(String ontologyId) {
        return MetadataTemplate.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

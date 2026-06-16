package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryDefinition {
    private String id;
    private String ontologyId;
    private String queryName;
    private String description;
    private String queryType;
    private String queryTemplate;
    private String parameters;
    private String resultSchema;
    private Integer timeoutMs;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean deleted;

    public static QueryDefinition create(String ontologyId, String queryName, String queryTemplate) {
        return QueryDefinition.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .queryName(queryName)
                .queryTemplate(queryTemplate)
                .queryType("CUSTOM")
                .parameters("[]")
                .resultSchema("{}")
                .timeoutMs(30000)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .deleted(false)
                .build();
    }
}

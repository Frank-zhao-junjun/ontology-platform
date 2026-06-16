package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComputeDefinition {
    private String id;
    private String ontologyId;
    private String computeName;
    private String description;
    private String inputSchema;
    private String formula;
    private String outputType;
    private String outputSchema;
    private Integer timeoutMs;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean deleted;

    public static ComputeDefinition create(String ontologyId, String computeName, String formula) {
        return ComputeDefinition.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .computeName(computeName)
                .formula(formula)
                .inputSchema("{}")
                .outputType("NUMBER")
                .outputSchema("{}")
                .timeoutMs(30000)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .deleted(false)
                .build();
    }
}

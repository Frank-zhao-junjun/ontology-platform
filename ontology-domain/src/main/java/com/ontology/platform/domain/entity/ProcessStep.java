package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStep {
    private String id;
    private String ontologyId;
    private String orchestrationId;
    private String name;
    private String stepType;
    private String description;
    private Integer sortOrder;
    private String config;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProcessStep create(String ontologyId) {
        return ProcessStep.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

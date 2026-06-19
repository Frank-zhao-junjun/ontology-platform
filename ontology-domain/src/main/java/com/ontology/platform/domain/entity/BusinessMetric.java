package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessMetric {
    private String id;
    private String ontologyId;
    private String name;
    private String nameEn;
    private String description;
    private String formula;
    private String dataSourceRef;
    private String period;
    private String targetEntity;
    private Instant createdAt;
    private Instant updatedAt;

    public static BusinessMetric create(String ontologyId) {
        return BusinessMetric.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

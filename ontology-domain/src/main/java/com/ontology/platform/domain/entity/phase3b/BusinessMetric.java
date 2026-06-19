package com.ontology.platform.domain.entity.phase3b;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessMetric {
    private String id;
    private String name;
    private String nameEn;
    private String description;
    private String formula;
    private String dataSourceRef;
    private String period;
    private String targetEntity;
    private Instant createdAt;
    private Instant updatedAt;

    public static BusinessMetric create() {
        return BusinessMetric.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

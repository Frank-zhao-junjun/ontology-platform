package com.ontology.platform.domain.entity.phase3b;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStep {
    private String id;
    private String orchestrationId;
    private String name;
    private String stepType;
    private String description;
    private Integer sortOrder;
    private String config;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProcessStep create() {
        return ProcessStep.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

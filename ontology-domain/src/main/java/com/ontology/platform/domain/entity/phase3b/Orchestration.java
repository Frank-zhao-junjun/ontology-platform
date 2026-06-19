package com.ontology.platform.domain.entity.phase3b;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orchestration {
    private String id;
    private String name;
    private String description;
    private String entryPoints;
    private Instant createdAt;
    private Instant updatedAt;

    public static Orchestration create() {
        return Orchestration.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

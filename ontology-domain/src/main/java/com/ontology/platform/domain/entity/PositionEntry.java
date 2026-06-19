package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionEntry {
    private String id;
    private String ontologyId;
    private String name;
    private String nameEn;
    private String description;
    private String departmentId;
    private String responsibilities;
    private Instant createdAt;
    private Instant updatedAt;

    public static PositionEntry create(String ontologyId) {
        return PositionEntry.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

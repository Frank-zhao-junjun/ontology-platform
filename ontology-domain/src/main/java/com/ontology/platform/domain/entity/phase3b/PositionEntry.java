package com.ontology.platform.domain.entity.phase3b;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionEntry {
    private String id;
    private String name;
    private String nameEn;
    private String description;
    private String departmentId;
    private String responsibilities;
    private Instant createdAt;
    private Instant updatedAt;

    public static PositionEntry create() {
        return PositionEntry.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

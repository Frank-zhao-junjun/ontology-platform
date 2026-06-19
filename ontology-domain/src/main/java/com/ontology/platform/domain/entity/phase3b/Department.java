package com.ontology.platform.domain.entity.phase3b;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department {
    private String id;
    private String name;
    private String nameEn;
    private String description;
    private String parentDepartmentId;
    private Instant createdAt;
    private Instant updatedAt;

    public static Department create() {
        return Department.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department {
    private String id;
    private String ontologyId;
    private String name;
    private String nameEn;
    private String description;
    private String parentDepartmentId;
    private Instant createdAt;
    private Instant updatedAt;

    public static Department create(String ontologyId) {
        return Department.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

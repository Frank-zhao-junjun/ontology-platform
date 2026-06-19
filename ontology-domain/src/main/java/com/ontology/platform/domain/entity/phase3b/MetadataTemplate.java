package com.ontology.platform.domain.entity.phase3b;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataTemplate {
    private String id;
    private String name;
    private String nameEn;
    private String description;
    private String domain;
    private String templateType;
    private Instant createdAt;
    private Instant updatedAt;

    public static MetadataTemplate create() {
        return MetadataTemplate.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

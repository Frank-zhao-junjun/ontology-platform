package com.ontology.platform.domain.entity.phase3b;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessTerm {
    private String id;
    private String name;
    private String nameEn;
    private String definition;
    private String synonyms;
    private String ontologyId;
    private Instant createdAt;
    private Instant updatedAt;

    public static BusinessTerm create() {
        return BusinessTerm.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

package com.ontology.platform.domain.entity;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Causality {

    private String id;
    private String ontologyId;
    private String causeEventId;
    private String effectEventId;
    private String description;
    private Integer delayMs;
    private String condition;
    private Instant createdAt;

    public static Causality create(String ontologyId, String causeEventId,
                                    String effectEventId, String description) {
        return Causality.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .causeEventId(causeEventId)
                .effectEventId(effectEventId)
                .description(description)
                .delayMs(0)
                .createdAt(Instant.now())
                .build();
    }
}

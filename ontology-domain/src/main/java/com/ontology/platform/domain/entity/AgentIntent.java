package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentIntent {
    private String id;
    private String ontologyId;
    private String name;
    private String description;
    private String triggerPhrases;
    private String actionId;
    private Instant createdAt;
    private Instant updatedAt;

    public static AgentIntent create(String ontologyId) {
        return AgentIntent.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

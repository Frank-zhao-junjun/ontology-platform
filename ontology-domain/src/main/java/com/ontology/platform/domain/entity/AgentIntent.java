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
    private String category;
    private String targetEntityId;
    private String triggerPhrases;
    private String actionId;
    private Integer priority;
    private Boolean requiresConfirmation;
    private Instant createdAt;
    private Instant updatedAt;

    public static AgentIntent create(String ontologyId) {
        return AgentIntent.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .priority(0)
                .requiresConfirmation(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

package com.ontology.platform.domain.entity;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateMachine {

    private String id;
    private String ontologyId;
    private String entityId;
    private String name;
    private String initialState;
    private String states;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean deleted;

    public static StateMachine create(String ontologyId, String entityId, String name,
                                       String initialState, String states) {
        return StateMachine.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .entityId(entityId)
                .name(name)
                .initialState(initialState)
                .states(states != null ? states : "[]")
                .deleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public void softDelete() {
        this.deleted = true;
        this.updatedAt = Instant.now();
    }
}

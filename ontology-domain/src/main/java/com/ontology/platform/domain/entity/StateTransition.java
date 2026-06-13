package com.ontology.platform.domain.entity;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateTransition {

    private String id;
    private String stateMachineId;
    private String fromState;
    private String toState;
    private String trigger;
    private String guardCondition;
    private Instant createdAt;

    public static StateTransition create(String stateMachineId, String fromState,
                                          String toState, String trigger, String guardCondition) {
        return StateTransition.builder()
                .id(UUID.randomUUID().toString())
                .stateMachineId(stateMachineId)
                .fromState(fromState)
                .toState(toState)
                .trigger(trigger)
                .guardCondition(guardCondition)
                .createdAt(Instant.now())
                .build();
    }
}

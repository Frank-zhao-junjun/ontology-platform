package com.ontology.platform.application.dto.domain;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateMachineResponse {
    private String id;
    private String name;
    private String entityId;
    private String initialState;
    private String states;
    private List<StateTransitionResponse> transitions;
}

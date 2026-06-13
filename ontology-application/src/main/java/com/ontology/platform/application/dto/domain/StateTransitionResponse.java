package com.ontology.platform.application.dto.domain;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateTransitionResponse {
    private String id;
    private String fromState;
    private String toState;
    private String trigger;
    private String guardCondition;
}

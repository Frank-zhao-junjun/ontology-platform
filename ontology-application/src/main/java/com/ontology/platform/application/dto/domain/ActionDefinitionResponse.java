package com.ontology.platform.application.dto.domain;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionDefinitionResponse {
    private String id;
    private String name;
    private String displayName;
    private String description;
    private String actionType;
    private String domain;
    private String riskLevel;
    private Boolean isAsync;
    private Integer timeoutMs;
    private String entityId;
    private String inputSchema;
    private String outputSchema;
    private String preRules;
    private String postRules;
    private List<StateMachineResponse> stateMachines;
}

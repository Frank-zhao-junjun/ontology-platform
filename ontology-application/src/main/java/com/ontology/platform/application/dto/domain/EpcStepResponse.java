package com.ontology.platform.application.dto.domain;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpcStepResponse {
    private String id;
    private String flowName;
    private Integer stepOrder;
    private String triggerEventId;
    private String triggerEventName;
    private String actionId;
    private String actionName;
    private String conditions;
    private String guards;
    private Integer timeoutMs;
}

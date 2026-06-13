package com.ontology.platform.domain.entity;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpcStep {

    private String id;
    private String ontologyId;
    private String flowName;
    private Integer stepOrder;
    private String triggerEventId;
    private String actionId;
    private String conditions;
    private String guards;
    private Integer timeoutMs;
    private Instant createdAt;
    private Instant updatedAt;

    public static EpcStep create(String ontologyId, String flowName, Integer stepOrder,
                                  String actionId, Integer timeoutMs) {
        return EpcStep.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .flowName(flowName)
                .stepOrder(stepOrder)
                .actionId(actionId)
                .conditions("[]")
                .guards("[]")
                .timeoutMs(timeoutMs != null ? timeoutMs : 60000)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

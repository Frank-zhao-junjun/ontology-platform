package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuardrailRule {
    private String id;
    private String ontologyId;
    private String ruleName;
    private String description;
    private String conditionExpr;
    private String actionType;
    private String actionConfig;
    private Integer priority;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean deleted;

    public static GuardrailRule create(String ontologyId, String ruleName, String conditionExpr, String actionType) {
        return GuardrailRule.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .ruleName(ruleName)
                .conditionExpr(conditionExpr)
                .actionType(actionType != null ? actionType : "BLOCK")
                .actionConfig("{}")
                .priority(0)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .deleted(false)
                .build();
    }
}

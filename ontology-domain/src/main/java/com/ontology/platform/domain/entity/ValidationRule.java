package com.ontology.platform.domain.entity;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRule {
    private String id;
    private String ontologyId;
    private String entityId;
    private String fieldName;
    private String ruleType;
    private String ruleName;
    private String description;
    private String severity;
    private String expression;
    private String errorMessage;
    private Boolean enabled;
    private Integer sortOrder;
    private String extendedData;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean deleted;

    public static ValidationRule create(String ontologyId, String ruleName, String ruleType, String expression) {
        return ValidationRule.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .ruleName(ruleName)
                .ruleType(ruleType != null ? ruleType : "CUSTOM")
                .severity("ERROR")
                .enabled(true)
                .sortOrder(0)
                .extendedData("{}")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .deleted(false)
                .build();
    }
}

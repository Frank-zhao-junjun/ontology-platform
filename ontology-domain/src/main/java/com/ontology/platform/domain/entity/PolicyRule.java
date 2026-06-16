package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRule {
    private String id;
    private String ontologyId;
    private String policyName;
    private String description;
    private String policyType;
    private String rules;
    private String effect;
    private Integer priority;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean deleted;

    public static PolicyRule create(String ontologyId, String policyName, String policyType) {
        return PolicyRule.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .policyName(policyName)
                .policyType(policyType != null ? policyType : "ACCESS")
                .rules("[]")
                .effect("ALLOW")
                .priority(0)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .deleted(false)
                .build();
    }
}

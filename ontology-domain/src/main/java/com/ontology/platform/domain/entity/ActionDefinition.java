package com.ontology.platform.domain.entity;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionDefinition {

    private String id;
    private String ontologyId;
    private String entityId;
    private String name;
    private String displayName;
    private String description;
    private String actionType;
    private String inputSchema;
    private String outputSchema;
    private String preRules;
    private String postRules;
    private String domain;
    private String riskLevel;
    private Boolean isAsync;
    private Integer timeoutMs;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean deleted;

    public static ActionDefinition create(String ontologyId, String entityId, String name,
                                           String displayName, String actionType, String domain,
                                           String riskLevel) {
        return ActionDefinition.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .entityId(entityId)
                .name(name)
                .displayName(displayName)
                .actionType(actionType)
                .domain(domain)
                .riskLevel(riskLevel != null ? riskLevel : "READ")
                .isAsync(false)
                .timeoutMs(30000)
                .inputSchema("{}")
                .outputSchema("{}")
                .preRules("[]")
                .postRules("[]")
                .deleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public boolean isWriteOperation() {
        return "WRITE".equalsIgnoreCase(riskLevel)
                || "DELETE".equalsIgnoreCase(riskLevel)
                || "APPROVAL".equalsIgnoreCase(riskLevel);
    }

    public void softDelete() {
        this.deleted = true;
        this.updatedAt = Instant.now();
    }
}

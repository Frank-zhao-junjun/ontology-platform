package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class AgentPolicySemantic {
    private String id;
    private String name;
    private String roleId;
    private String intentPatterns;
    private String allowActions;
    private String denyActions;
    private String requireConfirm;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    public static AgentPolicySemantic create() {
        return AgentPolicySemantic.builder().id(UUID.randomUUID().toString())
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}

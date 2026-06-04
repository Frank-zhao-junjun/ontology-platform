package com.ontology.platform.domain.entity;

import lombok.Getter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
public class AgentSandbox {
    private final String id;
    private final String name;
    private final String manifestVersionId;
    private final String agentRoleId;
    private final List<String> allowedTools;
    private final List<String> allowedAggregateRoots;
    private final List<String> allowedBehaviors;
    private final int maxOpsPerSecond;
    private final boolean active;
    private final Instant createdAt;

    private AgentSandbox(String id, String name, String manifestVersionId, String agentRoleId,
                         List<String> allowedTools, List<String> allowedAggregateRoots,
                         List<String> allowedBehaviors, int maxOpsPerSecond) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name;
        this.manifestVersionId = manifestVersionId;
        this.agentRoleId = agentRoleId;
        this.allowedTools = allowedTools != null ? List.copyOf(allowedTools) : List.of();
        this.allowedAggregateRoots = allowedAggregateRoots != null ? List.copyOf(allowedAggregateRoots) : List.of();
        this.allowedBehaviors = allowedBehaviors != null ? List.copyOf(allowedBehaviors) : List.of();
        this.maxOpsPerSecond = maxOpsPerSecond > 0 ? maxOpsPerSecond : 10;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public static AgentSandbox create(String name, String manifestVersionId, String agentRoleId,
                                      List<String> allowedTools, List<String> allowedAggregateRoots,
                                      List<String> allowedBehaviors, int maxOpsPerSecond) {
        return new AgentSandbox(null, name, manifestVersionId, agentRoleId,
                allowedTools, allowedAggregateRoots, allowedBehaviors, maxOpsPerSecond);
    }
}

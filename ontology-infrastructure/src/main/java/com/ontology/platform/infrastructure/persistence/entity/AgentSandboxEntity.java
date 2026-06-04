package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

@Entity
@Table(name = "agent_sandboxes")
@Getter
@Setter
public class AgentSandboxEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(name = "manifest_version_id", length = 36)
    private String manifestVersionId;
    @Column(name = "agent_role_id", length = 36)
    private String agentRoleId;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allowed_tools", columnDefinition = "json")
    private String allowedTools = "[]";
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allowed_aggregate_roots", columnDefinition = "json")
    private String allowedAggregateRoots = "[]";
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allowed_behaviors", columnDefinition = "json")
    private String allowedBehaviors = "[]";
    @Column(name = "max_ops_per_second")
    private int maxOpsPerSecond = 10;
    @Column(name = "is_active")
    private boolean active = true;
    @Column(name = "created_at")
    private Instant createdAt;
}

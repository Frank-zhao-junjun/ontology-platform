package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
public class AuditLogEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "tenant_id", length = 36)
    private String tenantId;
    @Column(name = "api_key_name", length = 100)
    private String apiKeyName;
    @Column(name = "sandbox_id", length = 36)
    private String sandboxId;
    @Column(name = "agent_role_name", length = 100)
    private String agentRoleName;
    @Column(name = "action", nullable = false, length = 100)
    private String action;
    @Column(name = "action_type", length = 50)
    private String actionType;
    @Column(name = "object_type", length = 100)
    private String objectType;
    @Column(name = "object_id", length = 255)
    private String objectId;
    @Column(name = "request_path", length = 500)
    private String requestPath;
    @Column(name = "response_code")
    private int responseCode;
    @Column(name = "error_message")
    private String errorMessage;
    @Column(name = "execution_time_ms")
    private long executionTimeMs;
    @Column(name = "timestamp")
    private Instant timestamp;
}

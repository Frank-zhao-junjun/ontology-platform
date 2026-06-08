package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "validation_rules", uniqueConstraints = @UniqueConstraint(columnNames = {"context_id", "manifest_code"}))
@Getter
@Setter
public class ValidationRuleEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "context_id", nullable = false, length = 36)
    private String contextId;
    @Column(name = "manifest_code", nullable = false, length = 80)
    private String manifestCode;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(name = "rule_type", nullable = false, length = 40)
    private String ruleType;
    @Column(name = "expression_json", nullable = false, columnDefinition = "TEXT")
    private String expressionJson = "{}";
    @Column(name = "error_message", nullable = false, columnDefinition = "TEXT")
    private String errorMessage;
    @Column(name = "failure_payload_schema", columnDefinition = "TEXT")
    private String failurePayloadSchema;
    private boolean enabled = true;
    @Column(name = "created_at")
    private Instant createdAt;
}

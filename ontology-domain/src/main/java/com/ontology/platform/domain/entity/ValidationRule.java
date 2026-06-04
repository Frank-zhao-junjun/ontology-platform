package com.ontology.platform.domain.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ValidationRule {
    private final String id, contextId, manifestCode, name, ruleType;
    private final String expressionJson, errorMessage, failurePayloadSchema;
    private final boolean enabled;
    private final Instant createdAt;

    @Builder
    public ValidationRule(String id, String contextId, String manifestCode, String name, String ruleType,
                          String expressionJson, String errorMessage, String failurePayloadSchema,
                          Boolean enabled, Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.contextId = contextId;
        this.manifestCode = manifestCode;
        this.name = name;
        this.ruleType = ruleType;
        this.expressionJson = expressionJson != null ? expressionJson : "{}";
        this.errorMessage = errorMessage;
        this.failurePayloadSchema = failurePayloadSchema;
        this.enabled = enabled == null || enabled;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static ValidationRule create(String contextId, String manifestCode, String name, String ruleType,
                                        String expressionJson, String errorMessage, String failurePayloadSchema) {
        return ValidationRule.builder().contextId(contextId).manifestCode(manifestCode).name(name)
                .ruleType(ruleType).expressionJson(expressionJson).errorMessage(errorMessage)
                .failurePayloadSchema(failurePayloadSchema).build();
    }
}

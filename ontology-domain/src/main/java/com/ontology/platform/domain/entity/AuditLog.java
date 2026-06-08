package com.ontology.platform.domain.entity;

import lombok.Getter;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
public class AuditLog {
    private final String id;
    private final String tenantId;
    private final String apiKeyName;
    private final String sandboxId;
    private final String agentRoleName;
    private final String action;
    private final String actionType;
    private final String objectType;
    private final String objectId;
    private final String requestPath;
    private final int responseCode;
    private final String errorMessage;
    private final long executionTimeMs;
    private final Instant timestamp;

    private AuditLog(String id, String tenantId, String apiKeyName, String sandboxId,
                     String agentRoleName, String action, String actionType,
                     String objectType, String objectId, String requestPath,
                     int responseCode, String errorMessage, long executionTimeMs) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.tenantId = tenantId;
        this.apiKeyName = apiKeyName;
        this.sandboxId = sandboxId;
        this.agentRoleName = agentRoleName;
        this.action = action;
        this.actionType = actionType;
        this.objectType = objectType;
        this.objectId = objectId;
        this.requestPath = requestPath;
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
        this.executionTimeMs = executionTimeMs;
        this.timestamp = Instant.now();
    }

    public static AuditLog create(String tenantId, String apiKeyName, String sandboxId,
                                  String agentRoleName, String action, String actionType,
                                  String objectType, String objectId, String requestPath,
                                  int responseCode, String errorMessage, long executionTimeMs) {
        return new AuditLog(null, tenantId, apiKeyName, sandboxId, agentRoleName,
                action, actionType, objectType, objectId, requestPath,
                responseCode, errorMessage, executionTimeMs);
    }

    public static AuditLog rehydrate(String id, String tenantId, String apiKeyName,
                                     String sandboxId, String agentRoleName, String action,
                                     String actionType, String objectType, String objectId,
                                     String requestPath, int responseCode, String errorMessage,
                                     long executionTimeMs, Instant timestamp) {
        AuditLog log = new AuditLog(id, tenantId, apiKeyName, sandboxId, agentRoleName,
                action, actionType, objectType, objectId, requestPath,
                responseCode, errorMessage, executionTimeMs);
        // override timestamp with persisted value
        try {
            var field = AuditLog.class.getDeclaredField("timestamp");
            field.setAccessible(true);
            field.set(log, timestamp);
        } catch (Exception ignore) {}
        return log;
    }
}

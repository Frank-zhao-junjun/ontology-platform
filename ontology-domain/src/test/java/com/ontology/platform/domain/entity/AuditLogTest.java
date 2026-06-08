package com.ontology.platform.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLogTest {

    @Test
    void createShouldGenerateIdAndTimestamp() {
        AuditLog log = AuditLog.create(
                "tenant-1", "api-key-1", "sandbox-1", "Planner",
                "query_ontology", "QUERY", "ProductionOrder",
                "PO-12345", "/mcp/query",
                200, null, 42L
        );

        assertThat(log.getId()).isNotNull();
        assertThat(log.getId()).hasSize(36); // UUID
        assertThat(log.getTenantId()).isEqualTo("tenant-1");
        assertThat(log.getApiKeyName()).isEqualTo("api-key-1");
        assertThat(log.getSandboxId()).isEqualTo("sandbox-1");
        assertThat(log.getAgentRoleName()).isEqualTo("Planner");
        assertThat(log.getAction()).isEqualTo("query_ontology");
        assertThat(log.getActionType()).isEqualTo("QUERY");
        assertThat(log.getObjectType()).isEqualTo("ProductionOrder");
        assertThat(log.getObjectId()).isEqualTo("PO-12345");
        assertThat(log.getRequestPath()).isEqualTo("/mcp/query");
        assertThat(log.getResponseCode()).isEqualTo(200);
        assertThat(log.getErrorMessage()).isNull();
        assertThat(log.getExecutionTimeMs()).isEqualTo(42L);
        assertThat(log.getTimestamp()).isNotNull();
        assertThat(log.getTimestamp()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void createShouldRecord403Forbidden() {
        AuditLog log = AuditLog.create(
                "tenant-1", "api-key-1", "sandbox-1", "Planner",
                "execute_action", "EXECUTE", "ProductionOrder",
                "PO-12345", "/mcp/execute",
                403, "Action not in allowed list", 5L
        );

        assertThat(log.getResponseCode()).isEqualTo(403);
        assertThat(log.getErrorMessage()).isEqualTo("Action not in allowed list");
    }

    @Test
    void createShouldRecord429RateLimit() {
        AuditLog log = AuditLog.create(
                "tenant-1", "api-key-1", "sandbox-1", "Planner",
                "execute_action", "EXECUTE", "ProductionOrder",
                "PO-12345", "/mcp/execute",
                429, "Rate limit exceeded", 1L
        );

        assertThat(log.getResponseCode()).isEqualTo(429);
        assertThat(log.getErrorMessage()).isEqualTo("Rate limit exceeded");
    }

    @Test
    void rehydrateShouldRestorePersistedTimestamp() {
        Instant persistedTimestamp = Instant.parse("2026-06-08T10:30:00Z");
        AuditLog log = AuditLog.rehydrate(
                "audit-uuid-123", "tenant-1", "api-key-1", "sandbox-1",
                "Planner", "query_ontology", "QUERY",
                "ProductionOrder", "PO-12345", "/mcp/query",
                200, null, 42L, persistedTimestamp
        );

        assertThat(log.getId()).isEqualTo("audit-uuid-123");
        assertThat(log.getTimestamp()).isEqualTo(persistedTimestamp);
    }
}

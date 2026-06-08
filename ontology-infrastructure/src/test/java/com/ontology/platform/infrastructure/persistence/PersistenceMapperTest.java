package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.AuditLog;
import com.ontology.platform.domain.entity.DataAccessMethod;
import com.ontology.platform.infrastructure.persistence.entity.AuditLogEntity;
import com.ontology.platform.infrastructure.persistence.entity.DataAccessMethodEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceMapperTest {

    // ── AuditLog mapping ──

    @Test
    void toEntityShouldMapAllAuditLogFields() {
        AuditLog log = AuditLog.create(
                "tenant-1", "api-key-1", "sandbox-1", "Planner",
                "execute_action", "EXECUTE", "ProductionOrder",
                "PO-12345", "/mcp/execute",
                200, null, 15L
        );

        AuditLogEntity entity = PersistenceMapper.toEntity(log);

        assertThat(entity.getId()).isEqualTo(log.getId());
        assertThat(entity.getTenantId()).isEqualTo("tenant-1");
        assertThat(entity.getApiKeyName()).isEqualTo("api-key-1");
        assertThat(entity.getSandboxId()).isEqualTo("sandbox-1");
        assertThat(entity.getAgentRoleName()).isEqualTo("Planner");
        assertThat(entity.getAction()).isEqualTo("execute_action");
        assertThat(entity.getActionType()).isEqualTo("EXECUTE");
        assertThat(entity.getObjectType()).isEqualTo("ProductionOrder");
        assertThat(entity.getObjectId()).isEqualTo("PO-12345");
        assertThat(entity.getRequestPath()).isEqualTo("/mcp/execute");
        assertThat(entity.getResponseCode()).isEqualTo(200);
        assertThat(entity.getErrorMessage()).isNull();
        assertThat(entity.getExecutionTimeMs()).isEqualTo(15L);
        assertThat(entity.getTimestamp()).isNotNull();
    }

    @Test
    void fromEntityShouldRehydrateAuditLog() {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setId("uuid-1");
        entity.setTenantId("tenant-1");
        entity.setApiKeyName("key-1");
        entity.setSandboxId("sb-1");
        entity.setAgentRoleName("Planner");
        entity.setAction("execute_action");
        entity.setActionType("EXECUTE");
        entity.setObjectType("ProductionOrder");
        entity.setObjectId("PO-1");
        entity.setRequestPath("/mcp/execute");
        entity.setResponseCode(403);
        entity.setErrorMessage("Forbidden");
        entity.setExecutionTimeMs(5L);
        Instant ts = Instant.now();
        entity.setTimestamp(ts);

        AuditLog log = PersistenceMapper.fromEntity(entity);

        assertThat(log.getId()).isEqualTo("uuid-1");
        assertThat(log.getResponseCode()).isEqualTo(403);
        assertThat(log.getErrorMessage()).isEqualTo("Forbidden");
    }

    @Test
    void auditLogRoundTripShouldPreserveAllData() {
        AuditLog original = AuditLog.create(
                "tenant-1", "api-key-1", "sandbox-1", "Planner",
                "execute_action", "EXECUTE", "ProductionOrder",
                "PO-12345", "/mcp/execute",
                200, null, 15L
        );

        AuditLogEntity entity = PersistenceMapper.toEntity(original);
        AuditLog restored = PersistenceMapper.fromEntity(entity);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getTenantId()).isEqualTo(original.getTenantId());
        assertThat(restored.getSandboxId()).isEqualTo(original.getSandboxId());
        assertThat(restored.getAgentRoleName()).isEqualTo(original.getAgentRoleName());
        assertThat(restored.getAction()).isEqualTo(original.getAction());
        assertThat(restored.getActionType()).isEqualTo(original.getActionType());
        assertThat(restored.getObjectType()).isEqualTo(original.getObjectType());
        assertThat(restored.getObjectId()).isEqualTo(original.getObjectId());
        assertThat(restored.getResponseCode()).isEqualTo(original.getResponseCode());
        assertThat(restored.getExecutionTimeMs()).isEqualTo(original.getExecutionTimeMs());
    }

    // ── DataAccessMethodEntity contextId mapping ──

    @Test
    void dataAccessMethodToEntityShouldIncludeContextId() {
        DataAccessMethod method = DataAccessMethod.create(
                "ctx-s2", "ot-1", "ds-1", "SQL_QUERY", null, null);
        method.setId("dam-1");

        DataAccessMethodEntity entity = PersistenceMapper.toEntity(method);

        assertThat(entity.getContextId()).isEqualTo("ctx-s2");
        assertThat(entity.getObjectTypeId()).isEqualTo("ot-1");
    }
}

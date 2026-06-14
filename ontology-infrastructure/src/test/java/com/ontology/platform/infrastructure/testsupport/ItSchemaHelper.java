package com.ontology.platform.infrastructure.testsupport;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.testcontainers.containers.PostgreSQLContainer;

public final class ItSchemaHelper {
    private ItSchemaHelper() {}

    public static void initV6GovernanceTables(PostgreSQLContainer<?> c) {
        exec(c, "CREATE TABLE IF NOT EXISTS agent_token (id VARCHAR(36) PRIMARY KEY, agent_id VARCHAR(200) NOT NULL UNIQUE, token_hash VARCHAR(500) NOT NULL, tenant_id VARCHAR(100) NOT NULL, display_name VARCHAR(500), status VARCHAR(20) DEFAULT 'ACTIVE', issued_at TIMESTAMPTZ DEFAULT now(), expires_at TIMESTAMPTZ NOT NULL, last_used_at TIMESTAMPTZ, created_by VARCHAR(100), created_at TIMESTAMPTZ DEFAULT now())");
        exec(c, "CREATE TABLE IF NOT EXISTS agent_role (id VARCHAR(36) PRIMARY KEY, token_id VARCHAR(36) NOT NULL REFERENCES agent_token(id), domain VARCHAR(200) NOT NULL, role VARCHAR(50) NOT NULL, granted_at TIMESTAMPTZ DEFAULT now(), CONSTRAINT uq_token_domain UNIQUE (token_id, domain))");
        exec(c, "CREATE TABLE IF NOT EXISTS role_permission (id VARCHAR(36) PRIMARY KEY, role_id VARCHAR(36) NOT NULL REFERENCES agent_role(id), resource VARCHAR(200) NOT NULL, operations JSONB DEFAULT '[]', domain VARCHAR(200) NOT NULL, created_at TIMESTAMPTZ DEFAULT now())");
        exec(c, "CREATE TABLE IF NOT EXISTS approval_request (id VARCHAR(36) PRIMARY KEY, agent_id VARCHAR(200) NOT NULL, action_id VARCHAR(36), requested_op VARCHAR(50) NOT NULL, status VARCHAR(20) DEFAULT 'PENDING', reason TEXT, requested_at TIMESTAMPTZ DEFAULT now(), resolved_at TIMESTAMPTZ, resolved_by VARCHAR(100))");
    }

    public static void initV3BehaviorTables(PostgreSQLContainer<?> c) {
        exec(c, "CREATE TABLE IF NOT EXISTS action_definition (id VARCHAR(36) PRIMARY KEY, ontology_id VARCHAR(36) NOT NULL, entity_id VARCHAR(255) NOT NULL, name VARCHAR(200) NOT NULL, display_name VARCHAR(500), description TEXT, action_type VARCHAR(50) NOT NULL, input_schema JSONB DEFAULT '{}', output_schema JSONB DEFAULT '{}', pre_rules JSONB DEFAULT '[]', post_rules JSONB DEFAULT '[]', domain VARCHAR(200), risk_level VARCHAR(20) DEFAULT 'READ', is_async BOOLEAN DEFAULT FALSE, timeout_ms INTEGER DEFAULT 30000, created_at TIMESTAMPTZ DEFAULT now(), updated_at TIMESTAMPTZ DEFAULT now(), deleted BOOLEAN DEFAULT FALSE)");
        exec(c, "CREATE TABLE IF NOT EXISTS state_machine (id VARCHAR(36) PRIMARY KEY, ontology_id VARCHAR(36) NOT NULL, entity_id VARCHAR(255) NOT NULL, name VARCHAR(200) NOT NULL, initial_state VARCHAR(100) NOT NULL, states JSONB DEFAULT '[]', created_at TIMESTAMPTZ DEFAULT now(), updated_at TIMESTAMPTZ DEFAULT now(), deleted BOOLEAN DEFAULT FALSE)");
        exec(c, "CREATE TABLE IF NOT EXISTS state_transition (id VARCHAR(36) PRIMARY KEY, state_machine_id VARCHAR(36) NOT NULL REFERENCES state_machine(id), from_state VARCHAR(100) NOT NULL, to_state VARCHAR(100) NOT NULL, trigger_name VARCHAR(200) NOT NULL, guard_condition VARCHAR(500), created_at TIMESTAMPTZ DEFAULT now())");
    }

    public static void initV4EventTables(PostgreSQLContainer<?> c) {
        exec(c, "CREATE TABLE IF NOT EXISTS domain_event (id VARCHAR(36) PRIMARY KEY, ontology_id VARCHAR(36) NOT NULL, entity_id VARCHAR(255) NOT NULL, name VARCHAR(200) NOT NULL, display_name VARCHAR(500), description TEXT, event_type VARCHAR(50) NOT NULL, severity VARCHAR(20) DEFAULT 'INFO', payload_schema JSONB DEFAULT '{}', source VARCHAR(200), created_at TIMESTAMPTZ DEFAULT now(), updated_at TIMESTAMPTZ DEFAULT now(), deleted BOOLEAN DEFAULT FALSE)");
        exec(c, "CREATE TABLE IF NOT EXISTS causality (id VARCHAR(36) PRIMARY KEY, ontology_id VARCHAR(36) NOT NULL, cause_event_id VARCHAR(36) NOT NULL REFERENCES domain_event(id), effect_event_id VARCHAR(36) NOT NULL REFERENCES domain_event(id), description TEXT, delay_ms INTEGER DEFAULT 0, condition VARCHAR(500), created_at TIMESTAMPTZ DEFAULT now(), CONSTRAINT uq_causality UNIQUE (cause_event_id, effect_event_id))");
    }

    public static void initV5EpcTables(PostgreSQLContainer<?> c) {
        exec(c, "CREATE TABLE IF NOT EXISTS epc_step (id VARCHAR(36) PRIMARY KEY, ontology_id VARCHAR(36) NOT NULL, flow_name VARCHAR(200) NOT NULL, step_order INTEGER NOT NULL, trigger_event_id VARCHAR(36), action_id VARCHAR(36), conditions JSONB DEFAULT '[]', guards JSONB DEFAULT '[]', timeout_ms INTEGER DEFAULT 60000, created_at TIMESTAMPTZ DEFAULT now(), updated_at TIMESTAMPTZ DEFAULT now(), CONSTRAINT uq_epc_flow_step UNIQUE (flow_name, step_order))");
    }

    public static void initV7UploadImportTables(PostgreSQLContainer<?> c) {
        exec(c, "CREATE TABLE IF NOT EXISTS upload_task (id VARCHAR(36) PRIMARY KEY, original_file_name VARCHAR(500) NOT NULL, file_size BIGINT NOT NULL, file_type VARCHAR(50) NOT NULL, chunk_size INT NOT NULL DEFAULT 5242880, total_chunks INT NOT NULL DEFAULT 1, target_type VARCHAR(100), ontology_id VARCHAR(36), object_type_name VARCHAR(200), user_id VARCHAR(100), tenant_id VARCHAR(100) DEFAULT 'default', status VARCHAR(20) DEFAULT 'PENDING', uploaded_chunks JSONB DEFAULT '[]', stored_file_path VARCHAR(1000), file_md5 VARCHAR(64), expires_at TIMESTAMPTZ, created_at TIMESTAMPTZ DEFAULT now(), updated_at TIMESTAMPTZ DEFAULT now())");
        exec(c, "CREATE TABLE IF NOT EXISTS import_task (id VARCHAR(36) PRIMARY KEY, upload_id VARCHAR(36), ontology_id VARCHAR(36), object_type_name VARCHAR(200), object_type_id VARCHAR(36), merge_strategy VARCHAR(20) DEFAULT 'UPSERT', error_handling VARCHAR(20) DEFAULT 'SKIP', user_id VARCHAR(100), tenant_id VARCHAR(100) DEFAULT 'default', status VARCHAR(20) DEFAULT 'PENDING', total_rows BIGINT DEFAULT 0, processed_rows BIGINT DEFAULT 0, success_rows BIGINT DEFAULT 0, failed_rows BIGINT DEFAULT 0, errors JSONB DEFAULT '[]', created_at TIMESTAMPTZ DEFAULT now(), completed_at TIMESTAMPTZ, estimated_completion TIMESTAMPTZ)");
    }

    private static void exec(PostgreSQLContainer<?> c, String sql) {
        try (Connection conn = DriverManager.getConnection(c.getJdbcUrl(), c.getUsername(), c.getPassword());
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("Schema init failed: " + e.getMessage(), e);
        }
    }
}

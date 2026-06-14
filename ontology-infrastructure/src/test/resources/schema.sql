-- Test schema for H2 database
CREATE TABLE IF NOT EXISTS ontology (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001',
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    description TEXT,
    version VARCHAR(20) NOT NULL DEFAULT '0.1.0',
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    published_at TIMESTAMP WITH TIME ZONE,
    object_type_count INT DEFAULT 0,
    action_type_count INT DEFAULT 0,
    created_by VARCHAR(36),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ontology_tenant ON ontology(tenant_id);
CREATE INDEX IF NOT EXISTS idx_ontology_status ON ontology(status);
CREATE INDEX IF NOT EXISTS idx_ontology_name ON ontology(name);

-- V3: action_definition, state_machine, state_transition
CREATE TABLE IF NOT EXISTS action_definition (
    id VARCHAR(36) PRIMARY KEY,
    ontology_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200),
    entity_id VARCHAR(36),
    domain VARCHAR(100),
    action_type VARCHAR(50),
    risk_level VARCHAR(20) DEFAULT 'READ',
    pre_rules TEXT,
    state_machine_id VARCHAR(36),
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS state_machine (
    id VARCHAR(36) PRIMARY KEY,
    ontology_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    entity_id VARCHAR(36),
    initial_state VARCHAR(100),
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS state_transition (
    id VARCHAR(36) PRIMARY KEY,
    state_machine_id VARCHAR(36) NOT NULL,
    from_state VARCHAR(100) NOT NULL,
    to_state VARCHAR(100) NOT NULL,
    event VARCHAR(100),
    guard_condition TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- V4: domain_event, causality
CREATE TABLE IF NOT EXISTS domain_event (
    id VARCHAR(36) PRIMARY KEY,
    ontology_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200),
    entity_id VARCHAR(36),
    event_type VARCHAR(50),
    severity VARCHAR(20),
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS causality (
    id VARCHAR(36) PRIMARY KEY,
    ontology_id VARCHAR(36) NOT NULL,
    cause_event_id VARCHAR(36) NOT NULL,
    effect_event_id VARCHAR(36) NOT NULL,
    delay_ms BIGINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- V5: epc_step
CREATE TABLE IF NOT EXISTS epc_step (
    id VARCHAR(36) PRIMARY KEY,
    ontology_id VARCHAR(36) NOT NULL,
    flow_name VARCHAR(100) NOT NULL,
    step_order INT NOT NULL,
    trigger_event VARCHAR(100),
    action_name VARCHAR(100),
    conditions TEXT,
    guards TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- V6: governance
CREATE TABLE IF NOT EXISTS agent_role (
    id VARCHAR(36) PRIMARY KEY,
    token_id VARCHAR(36) NOT NULL,
    domain VARCHAR(100) NOT NULL,
    role VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS approval_request (
    id VARCHAR(36) PRIMARY KEY,
    agent_id VARCHAR(100) NOT NULL,
    action_id VARCHAR(100),
    requested_op VARCHAR(50),
    status VARCHAR(20) DEFAULT 'PENDING',
    resolved_by VARCHAR(100),
    reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS role_permission (
    id VARCHAR(36) PRIMARY KEY,
    role_id VARCHAR(36) NOT NULL,
    resource VARCHAR(200) NOT NULL,
    operations VARCHAR(500),
    domain VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- V7: upload/import
CREATE TABLE IF NOT EXISTS upload_task (
    id VARCHAR(36) PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT DEFAULT 0,
    file_type VARCHAR(50),
    target_type VARCHAR(50),
    ontology_id VARCHAR(36),
    object_type_name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'INIT',
    uploaded_chunks TEXT,
    total_chunks INT DEFAULT 0,
    user_id VARCHAR(100),
    tenant_id VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS import_task (
    id VARCHAR(36) PRIMARY KEY,
    upload_id VARCHAR(36),
    ontology_id VARCHAR(36),
    object_type_name VARCHAR(100),
    object_type_id VARCHAR(36),
    merge_strategy VARCHAR(20) DEFAULT 'INSERT',
    error_handling VARCHAR(20) DEFAULT 'SKIP',
    user_id VARCHAR(100),
    tenant_id VARCHAR(100),
    status VARCHAR(20) DEFAULT 'PENDING',
    total_rows BIGINT DEFAULT 0,
    processed_rows BIGINT DEFAULT 0,
    success_rows BIGINT DEFAULT 0,
    failed_rows BIGINT DEFAULT 0,
    errors TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    estimated_completion TIMESTAMP WITH TIME ZONE
);

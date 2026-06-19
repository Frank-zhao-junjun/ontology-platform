-- V13__create_phase3c_tables.sql
-- Phase 3c: Semantic & Lifecycle tables

CREATE TABLE semantic_relation (
    id VARCHAR(200) PRIMARY KEY,
    source_term_id VARCHAR(200) NOT NULL,
    target_term_id VARCHAR(200) NOT NULL,
    relation_type VARCHAR(100) NOT NULL,  -- synonym, hyponym, related, etc.
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_sr_source ON semantic_relation(source_term_id);
CREATE INDEX idx_sr_target ON semantic_relation(target_term_id);

CREATE TABLE intent_slot (
    id VARCHAR(200) PRIMARY KEY,
    intent_id VARCHAR(200) NOT NULL,
    name VARCHAR(500) NOT NULL,
    slot_type VARCHAR(100),  -- entity, value, date, etc.
    required BOOLEAN DEFAULT false,
    examples JSONB DEFAULT '[]',
    created_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT fk_slot_intent FOREIGN KEY (intent_id) REFERENCES agent_intent(id)
);
CREATE INDEX idx_slot_intent ON intent_slot(intent_id);

CREATE TABLE agent_policy_semantic (
    id VARCHAR(200) PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    role_id VARCHAR(200),
    intent_patterns JSONB DEFAULT '[]',  -- allowed intent id patterns
    allow_actions JSONB DEFAULT '[]',
    deny_actions JSONB DEFAULT '[]',
    require_confirm JSONB DEFAULT '[]',  -- action ids needing confirmation
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE error_recovery (
    id VARCHAR(200) PRIMARY KEY,
    action_id VARCHAR(200) NOT NULL,
    error_pattern VARCHAR(500),
    recovery_strategy VARCHAR(200) NOT NULL,  -- retry, rollback, notify, fallback
    max_retries INTEGER DEFAULT 3,
    fallback_action_id VARCHAR(200),
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_er_action ON error_recovery(action_id);

CREATE TABLE semantic_field_mapping (
    id VARCHAR(200) PRIMARY KEY,
    entity_id VARCHAR(200) NOT NULL,
    field_name_en VARCHAR(500),
    business_term_id VARCHAR(200),
    mapping_type VARCHAR(100) DEFAULT 'direct',  -- direct, derived, computed
    transform_rule TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_sfm_entity ON semantic_field_mapping(entity_id);

CREATE TABLE entity_lifecycle_snapshot (
    id VARCHAR(200) PRIMARY KEY,
    entity_id VARCHAR(200) NOT NULL,
    ontology_id VARCHAR(200),
    lifecycle_data JSONB NOT NULL,
    snapshot_version VARCHAR(50) DEFAULT '1.0',
    created_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_els_entity ON entity_lifecycle_snapshot(entity_id);

-- V12: Phase 3b tables (organization, metrics, process, metadata, semantic)

-- ========== Organization ==========
CREATE TABLE department (
    id VARCHAR(200) PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    name_en VARCHAR(500),
    description TEXT,
    parent_department_id VARCHAR(200),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE position_entry (
    id VARCHAR(200) PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    name_en VARCHAR(500),
    description TEXT,
    department_id VARCHAR(200),
    responsibilities JSONB DEFAULT '[]',
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_position_dept ON position_entry(department_id);

-- ========== Business Metrics ==========
CREATE TABLE business_metric (
    id VARCHAR(200) PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    name_en VARCHAR(500),
    description TEXT,
    formula TEXT,
    data_source_ref VARCHAR(200),
    period VARCHAR(50),
    target_entity VARCHAR(200),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- ========== Orchestration / Process ==========
CREATE TABLE orchestration (
    id VARCHAR(200) PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    description TEXT,
    entry_points JSONB DEFAULT '[]',
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE process_step (
    id VARCHAR(200) PRIMARY KEY,
    orchestration_id VARCHAR(200) NOT NULL,
    name VARCHAR(500) NOT NULL,
    step_type VARCHAR(100),
    description TEXT,
    sort_order INTEGER DEFAULT 0,
    config JSONB,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT fk_step_orch FOREIGN KEY (orchestration_id) REFERENCES orchestration(id)
);
CREATE INDEX idx_step_orch ON process_step(orchestration_id, sort_order);

-- ========== Metadata Template ==========
CREATE TABLE metadata_template (
    id VARCHAR(200) PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    name_en VARCHAR(500),
    description TEXT,
    domain VARCHAR(200),
    template_type VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- ========== Semantic ==========
CREATE TABLE business_term (
    id VARCHAR(200) PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    name_en VARCHAR(500),
    definition TEXT,
    synonyms JSONB DEFAULT '[]',
    ontology_id VARCHAR(200),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE agent_intent (
    id VARCHAR(200) PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    description TEXT,
    trigger_phrases JSONB DEFAULT '[]',
    action_id VARCHAR(200),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_intent_action ON agent_intent(action_id);

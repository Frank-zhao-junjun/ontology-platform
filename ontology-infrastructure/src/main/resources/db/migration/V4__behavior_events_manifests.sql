-- US-B01 / US-B03 / US-E01 / US-A01 Round 3

CREATE TABLE IF NOT EXISTS validation_rules (
    id                      VARCHAR(36) PRIMARY KEY,
    context_id              VARCHAR(36) NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    manifest_code           VARCHAR(80) NOT NULL,
    name                    VARCHAR(200) NOT NULL,
    rule_type               VARCHAR(40) NOT NULL,
    expression_json         TEXT NOT NULL DEFAULT '{}',
    error_message           TEXT NOT NULL,
    failure_payload_schema  TEXT,
    enabled                 BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(context_id, manifest_code)
);

CREATE TABLE IF NOT EXISTS ontology_actions (
    id                      VARCHAR(36) PRIMARY KEY,
    context_id              VARCHAR(36) NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    manifest_code           VARCHAR(80) NOT NULL,
    name                    VARCHAR(200) NOT NULL,
    name_en                 VARCHAR(200),
    description             TEXT,
    aggregate_root_id       VARCHAR(36) NOT NULL REFERENCES aggregate_roots(id) ON DELETE CASCADE,
    invocation_mode         VARCHAR(20) NOT NULL DEFAULT 'BOTH',
    parameters_json         TEXT NOT NULL DEFAULT '[]',
    publishes_event_ids_json TEXT NOT NULL DEFAULT '[]',
    allowed_state_from_json TEXT NOT NULL DEFAULT '[]',
    business_scenario_ids_json TEXT NOT NULL DEFAULT '[]',
    mcp_tool_name           VARCHAR(120),
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(context_id, manifest_code)
);

CREATE TABLE IF NOT EXISTS action_rule_links (
    action_id               VARCHAR(36) NOT NULL REFERENCES ontology_actions(id) ON DELETE CASCADE,
    rule_id                 VARCHAR(36) NOT NULL REFERENCES validation_rules(id) ON DELETE CASCADE,
    sort_order              INT NOT NULL DEFAULT 0,
    PRIMARY KEY (action_id, rule_id)
);

CREATE TABLE IF NOT EXISTS domain_events (
    id                      VARCHAR(36) PRIMARY KEY,
    context_id              VARCHAR(36) NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    manifest_code           VARCHAR(80) NOT NULL,
    name                    VARCHAR(200) NOT NULL,
    name_en                 VARCHAR(200),
    aggregate_root_id       VARCHAR(36) NOT NULL REFERENCES aggregate_roots(id) ON DELETE CASCADE,
    trigger_action_id       VARCHAR(36) REFERENCES ontology_actions(id) ON DELETE SET NULL,
    payload_schema_json     TEXT NOT NULL DEFAULT '{}',
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(context_id, manifest_code)
);

CREATE TABLE IF NOT EXISTS published_manifests (
    id                      VARCHAR(36) PRIMARY KEY,
    context_id              VARCHAR(36) NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    ontology_id             VARCHAR(100) NOT NULL,
    version                 VARCHAR(50) NOT NULL,
    api_version             VARCHAR(40) NOT NULL DEFAULT 'ontology.platform/v1',
    status                  VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    snapshot_json           TEXT NOT NULL,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(context_id, version)
);

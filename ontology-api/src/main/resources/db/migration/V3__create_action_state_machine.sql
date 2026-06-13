-- V3: Action definition, State machine, State transition
-- US: P04

CREATE TABLE IF NOT EXISTS action_definition (
    id             UUID PRIMARY KEY,
    ontology_id    UUID NOT NULL,
    entity_id      VARCHAR(255) NOT NULL,
    name           VARCHAR(200) NOT NULL,
    display_name   VARCHAR(500),
    description    TEXT,
    action_type    VARCHAR(50)  NOT NULL,
    input_schema   JSONB        DEFAULT '{}',
    output_schema  JSONB        DEFAULT '{}',
    pre_rules      JSONB        DEFAULT '[]',
    post_rules     JSONB        DEFAULT '[]',
    domain         VARCHAR(200),
    risk_level     VARCHAR(20)  DEFAULT 'READ',
    is_async       BOOLEAN      DEFAULT FALSE,
    timeout_ms     INTEGER      DEFAULT 30000,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted        BOOLEAN      DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS state_machine (
    id             UUID PRIMARY KEY,
    ontology_id    UUID NOT NULL,
    entity_id      VARCHAR(255) NOT NULL,
    name           VARCHAR(200) NOT NULL,
    initial_state  VARCHAR(100) NOT NULL,
    states         JSONB        NOT NULL DEFAULT '[]',
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted        BOOLEAN      DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS state_transition (
    id                UUID PRIMARY KEY,
    state_machine_id  UUID NOT NULL REFERENCES state_machine(id),
    from_state        VARCHAR(100) NOT NULL,
    to_state          VARCHAR(100) NOT NULL,
    trigger           VARCHAR(200) NOT NULL,
    guard_condition   VARCHAR(500),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_action_entity ON action_definition(entity_id);
CREATE INDEX IF NOT EXISTS idx_action_domain ON action_definition(domain);
CREATE INDEX IF NOT EXISTS idx_state_machine_entity ON state_machine(entity_id);
CREATE INDEX IF NOT EXISTS idx_transition_sm ON state_transition(state_machine_id);

-- V4: Domain event & Causality
-- US: P05

CREATE TABLE IF NOT EXISTS domain_event (
    id              UUID PRIMARY KEY,
    ontology_id     UUID NOT NULL,
    entity_id       VARCHAR(255) NOT NULL,
    name            VARCHAR(200) NOT NULL,
    display_name    VARCHAR(500),
    description     TEXT,
    event_type      VARCHAR(50)  NOT NULL,
    severity        VARCHAR(20)  DEFAULT 'INFO',
    payload_schema  JSONB        DEFAULT '{}',
    source          VARCHAR(200),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted         BOOLEAN      DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS causality (
    id               UUID PRIMARY KEY,
    ontology_id      UUID NOT NULL,
    cause_event_id   UUID NOT NULL REFERENCES domain_event(id),
    effect_event_id  UUID NOT NULL REFERENCES domain_event(id),
    description      TEXT,
    delay_ms         INTEGER DEFAULT 0,
    condition        VARCHAR(500),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_causality UNIQUE (cause_event_id, effect_event_id)
);

CREATE INDEX IF NOT EXISTS idx_event_entity ON domain_event(entity_id);
CREATE INDEX IF NOT EXISTS idx_event_type ON domain_event(event_type);
CREATE INDEX IF NOT EXISTS idx_causality_cause ON causality(cause_event_id);

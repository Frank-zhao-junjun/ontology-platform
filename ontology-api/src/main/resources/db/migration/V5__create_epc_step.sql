-- V5: EPC process step
-- US: P06

CREATE TABLE IF NOT EXISTS epc_step (
    id               UUID PRIMARY KEY,
    ontology_id      UUID NOT NULL,
    flow_name        VARCHAR(200) NOT NULL,
    step_order       INTEGER NOT NULL,
    trigger_event_id UUID REFERENCES domain_event(id),
    action_id        UUID REFERENCES action_definition(id),
    conditions       JSONB DEFAULT '[]',
    guards           JSONB DEFAULT '[]',
    timeout_ms       INTEGER DEFAULT 60000,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_epc_flow_step UNIQUE (flow_name, step_order)
);

CREATE INDEX IF NOT EXISTS idx_epc_flow ON epc_step(flow_name);
CREATE INDEX IF NOT EXISTS idx_epc_trigger ON epc_step(trigger_event_id);

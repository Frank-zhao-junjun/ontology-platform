-- V10: State Machines (US-S06)
CREATE TABLE state_machines (
    id UUID PRIMARY KEY,
    context_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    name_en VARCHAR(200),
    object_type_id UUID NOT NULL,
    status_field VARCHAR(100) NOT NULL DEFAULT 'status',
    states_json TEXT NOT NULL DEFAULT '[]',
    transitions_json TEXT NOT NULL DEFAULT '[]',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    UNIQUE (context_id, object_type_id)
);

CREATE INDEX idx_state_machines_context ON state_machines(context_id);
CREATE INDEX idx_state_machines_object_type ON state_machines(object_type_id);

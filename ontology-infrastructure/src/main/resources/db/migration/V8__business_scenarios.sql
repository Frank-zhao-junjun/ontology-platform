-- V8: Business Scenarios (US-S02)
CREATE TABLE business_scenarios (
    id UUID PRIMARY KEY,
    context_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(80) NOT NULL,
    name_en VARCHAR(200),
    description TEXT,
    applicable_object_type_ids_json TEXT NOT NULL DEFAULT '[]',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (context_id, code)
);

CREATE INDEX idx_business_scenarios_context ON business_scenarios(context_id);

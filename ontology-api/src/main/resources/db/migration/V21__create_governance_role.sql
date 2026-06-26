-- V21: governance_role table + link position_entry to roles
-- P1#7: Position → Role → Permission chain

CREATE TABLE IF NOT EXISTS governance_role (
    id VARCHAR(200) PRIMARY KEY,
    ontology_id VARCHAR(200) NOT NULL,
    name VARCHAR(500) NOT NULL,
    description TEXT,
    permissions JSONB DEFAULT '[]',
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Bridge: position → role
ALTER TABLE position_entry ADD COLUMN IF NOT EXISTS role_id VARCHAR(200);

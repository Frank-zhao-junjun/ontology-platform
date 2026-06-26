-- V20: Create business_scenario table for Phase 3 §3a — P0#3 businessScenarioId support
CREATE TABLE IF NOT EXISTS business_scenario (
    id           VARCHAR(200) PRIMARY KEY,
    ontology_id  VARCHAR(200) NOT NULL,
    name         VARCHAR(200),
    name_en      VARCHAR(200),
    description  TEXT,
    project_id   VARCHAR(200),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_business_scenario_ontology ON business_scenario(ontology_id);
CREATE INDEX IF NOT EXISTS idx_business_scenario_project ON business_scenario(project_id);

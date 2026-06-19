-- V15: Add ontology_id to Phase 3b tables for ontology scoping

ALTER TABLE department ADD COLUMN IF NOT EXISTS ontology_id VARCHAR(200);
ALTER TABLE position_entry ADD COLUMN IF NOT EXISTS ontology_id VARCHAR(200);
ALTER TABLE business_metric ADD COLUMN IF NOT EXISTS ontology_id VARCHAR(200);
ALTER TABLE orchestration ADD COLUMN IF NOT EXISTS ontology_id VARCHAR(200);
ALTER TABLE process_step ADD COLUMN IF NOT EXISTS ontology_id VARCHAR(200);
ALTER TABLE metadata_template ADD COLUMN IF NOT EXISTS ontology_id VARCHAR(200);
ALTER TABLE agent_intent ADD COLUMN IF NOT EXISTS ontology_id VARCHAR(200);

CREATE INDEX IF NOT EXISTS idx_department_ontology ON department(ontology_id);
CREATE INDEX IF NOT EXISTS idx_position_ontology ON position_entry(ontology_id);
CREATE INDEX IF NOT EXISTS idx_business_metric_ontology ON business_metric(ontology_id);
CREATE INDEX IF NOT EXISTS idx_orchestration_ontology ON orchestration(ontology_id);
CREATE INDEX IF NOT EXISTS idx_process_step_ontology ON process_step(ontology_id);
CREATE INDEX IF NOT EXISTS idx_metadata_template_ontology ON metadata_template(ontology_id);
CREATE INDEX IF NOT EXISTS idx_agent_intent_ontology ON agent_intent(ontology_id);

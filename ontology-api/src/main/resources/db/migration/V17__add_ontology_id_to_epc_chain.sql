-- V17: Add ontology_id to epc_chain for ontology-scoped coverage queries

ALTER TABLE epc_chain ADD COLUMN IF NOT EXISTS ontology_id VARCHAR(200);

CREATE INDEX IF NOT EXISTS idx_epc_chain_ontology ON epc_chain(ontology_id);

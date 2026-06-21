-- V16: Phase 3c Sprint 2 — extend agent_intent with semantic metadata

ALTER TABLE agent_intent ADD COLUMN IF NOT EXISTS category VARCHAR(100);
ALTER TABLE agent_intent ADD COLUMN IF NOT EXISTS target_entity_id VARCHAR(200);
ALTER TABLE agent_intent ADD COLUMN IF NOT EXISTS priority INTEGER;
ALTER TABLE agent_intent ADD COLUMN IF NOT EXISTS requires_confirmation BOOLEAN DEFAULT false;

CREATE INDEX IF NOT EXISTS idx_agent_intent_target_entity ON agent_intent(target_entity_id);

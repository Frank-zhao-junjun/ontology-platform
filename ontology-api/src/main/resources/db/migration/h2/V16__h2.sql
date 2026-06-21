ALTER TABLE agent_intent ADD COLUMN category VARCHAR(100);
ALTER TABLE agent_intent ADD COLUMN target_entity_id VARCHAR(200);
ALTER TABLE agent_intent ADD COLUMN priority INTEGER;
ALTER TABLE agent_intent ADD COLUMN requires_confirmation BOOLEAN DEFAULT false;

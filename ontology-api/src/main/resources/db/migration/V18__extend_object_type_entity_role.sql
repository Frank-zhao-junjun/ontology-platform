-- V18: Extend object_type with entity role, parent aggregate, business scenario, and attributes JSONB
-- Corresponds to Phase 3 Spec §3a — P0#2 entity role mapping + P0#3 businessScenarioId support

ALTER TABLE object_type
  ADD COLUMN IF NOT EXISTS entity_role VARCHAR(50) DEFAULT 'aggregate_root',
  ADD COLUMN IF NOT EXISTS parent_aggregate_id VARCHAR(200),
  ADD COLUMN IF NOT EXISTS business_scenario_id VARCHAR(200),
  ADD COLUMN IF NOT EXISTS sub_domain VARCHAR(200),
  ADD COLUMN IF NOT EXISTS attributes_jsonb JSONB DEFAULT '{}';

-- Drop old constraint if it exists from a previous partial migration
ALTER TABLE object_type DROP CONSTRAINT IF EXISTS ck_object_type_entity_role;

-- Enforce valid entity role values
ALTER TABLE object_type ADD CONSTRAINT ck_object_type_entity_role
  CHECK (entity_role IN ('aggregate_root', 'child_entity'));

-- Indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_object_type_entity_role ON object_type(entity_role);
CREATE INDEX IF NOT EXISTS idx_object_type_business_scenario ON object_type(business_scenario_id);
CREATE INDEX IF NOT EXISTS idx_object_type_parent_aggregate ON object_type(parent_aggregate_id);
CREATE INDEX IF NOT EXISTS idx_object_type_sub_domain ON object_type(sub_domain);

-- Update v2 exchange import fixture rows: set entity_role for existing rows based on parent_id heuristics
-- Entities without parent_id are likely aggregate roots; entities with parent_id are child entities
UPDATE object_type
  SET entity_role = CASE WHEN parent_id IS NULL THEN 'aggregate_root' ELSE 'child_entity' END
  WHERE entity_role = 'aggregate_root' AND parent_id IS NOT NULL;

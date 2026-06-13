-- =============================================
-- 本体建模平台 - EPC 编排步骤表
-- V5__create_epc_step.sql
-- 覆盖 US: P06
-- =============================================

CREATE TABLE epc_step (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ontology_id UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    flow_name VARCHAR(200) NOT NULL,
    step_order INTEGER NOT NULL,
    trigger_event_id UUID REFERENCES domain_event(id) ON DELETE SET NULL,
    action_id UUID REFERENCES action_definition(id) ON DELETE SET NULL,
    conditions JSONB DEFAULT '[]',
    guards JSONB DEFAULT '[]',
    timeout_ms INTEGER NOT NULL DEFAULT 60000,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_epc_flow_step UNIQUE (flow_name, step_order)
);

CREATE INDEX idx_epc_step_ontology ON epc_step(ontology_id);
CREATE INDEX idx_epc_step_flow ON epc_step(flow_name);
CREATE INDEX idx_epc_step_order ON epc_step(flow_name, step_order);
CREATE INDEX idx_epc_step_event ON epc_step(trigger_event_id);
CREATE INDEX idx_epc_step_action ON epc_step(action_id);
CREATE INDEX idx_epc_step_conditions ON epc_step USING GIN (conditions);
CREATE INDEX idx_epc_step_guards ON epc_step USING GIN (guards);

-- 自动更新触发器
CREATE TRIGGER update_epc_step_updated_at BEFORE UPDATE ON epc_step
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 注释
COMMENT ON TABLE epc_step IS 'EPC 编排步骤表 (P06) — 事件驱动过程链';

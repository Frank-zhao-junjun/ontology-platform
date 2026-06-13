-- =============================================
-- 本体建模平台 - 行为定义与状态机表
-- V3__create_action_state_machine.sql
-- 覆盖 US: P04
-- =============================================

-- 行为/动作定义表
CREATE TABLE action_definition (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ontology_id UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    entity_id VARCHAR(255) NOT NULL,
    name VARCHAR(200) NOT NULL,
    display_name VARCHAR(500),
    description TEXT,
    action_type VARCHAR(50) NOT NULL,
    input_schema JSONB DEFAULT '{}',
    output_schema JSONB DEFAULT '{}',
    pre_rules JSONB DEFAULT '[]',
    post_rules JSONB DEFAULT '[]',
    domain VARCHAR(200),
    risk_level VARCHAR(20) NOT NULL DEFAULT 'READ',
    is_async BOOLEAN NOT NULL DEFAULT FALSE,
    timeout_ms INTEGER NOT NULL DEFAULT 30000,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_action_ontology_name UNIQUE (ontology_id, name)
);

CREATE INDEX idx_action_definition_ontology ON action_definition(ontology_id);
CREATE INDEX idx_action_definition_entity ON action_definition(entity_id);
CREATE INDEX idx_action_definition_type ON action_definition(action_type);
CREATE INDEX idx_action_definition_risk ON action_definition(risk_level);
CREATE INDEX idx_action_definition_domain ON action_definition(domain);
CREATE INDEX idx_action_definition_input ON action_definition USING GIN (input_schema);

-- 状态机定义表
CREATE TABLE state_machine (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ontology_id UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    entity_id VARCHAR(255) NOT NULL,
    name VARCHAR(200) NOT NULL,
    initial_state VARCHAR(100) NOT NULL,
    states JSONB NOT NULL DEFAULT '[]',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_sm_ontology_name UNIQUE (ontology_id, name)
);

CREATE INDEX idx_state_machine_ontology ON state_machine(ontology_id);
CREATE INDEX idx_state_machine_entity ON state_machine(entity_id);
CREATE INDEX idx_state_machine_states ON state_machine USING GIN (states);

-- 状态转换表
CREATE TABLE state_transition (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    state_machine_id UUID NOT NULL REFERENCES state_machine(id) ON DELETE CASCADE,
    from_state VARCHAR(100) NOT NULL,
    to_state VARCHAR(100) NOT NULL,
    trigger VARCHAR(200) NOT NULL,
    guard_condition VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_sm_transition UNIQUE (state_machine_id, from_state, to_state)
);

CREATE INDEX idx_state_transition_sm ON state_transition(state_machine_id);
CREATE INDEX idx_state_transition_from ON state_transition(from_state);
CREATE INDEX idx_state_transition_to ON state_transition(to_state);

-- 自动更新触发器
CREATE TRIGGER update_action_definition_updated_at BEFORE UPDATE ON action_definition
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_state_machine_updated_at BEFORE UPDATE ON state_machine
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 注释
COMMENT ON TABLE action_definition IS '行为/动作定义表 (P04)';
COMMENT ON TABLE state_machine IS '状态机定义表 (P04)';
COMMENT ON TABLE state_transition IS '状态转换表 (P04)';

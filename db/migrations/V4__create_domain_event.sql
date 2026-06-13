-- =============================================
-- 本体建模平台 - 领域事件与因果表
-- V4__create_domain_event.sql
-- 覆盖 US: P05
-- =============================================

-- 领域事件定义表
CREATE TABLE domain_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ontology_id UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    entity_id VARCHAR(255) NOT NULL,
    name VARCHAR(200) NOT NULL,
    display_name VARCHAR(500),
    description TEXT,
    event_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO',
    payload_schema JSONB DEFAULT '{}',
    source VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_event_ontology_name UNIQUE (ontology_id, name)
);

CREATE INDEX idx_domain_event_ontology ON domain_event(ontology_id);
CREATE INDEX idx_domain_event_entity ON domain_event(entity_id);
CREATE INDEX idx_domain_event_type ON domain_event(event_type);
CREATE INDEX idx_domain_event_severity ON domain_event(severity);
CREATE INDEX idx_domain_event_payload ON domain_event USING GIN (payload_schema);

-- 事件因果关系表
CREATE TABLE causality (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ontology_id UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    cause_event_id UUID NOT NULL REFERENCES domain_event(id) ON DELETE CASCADE,
    effect_event_id UUID NOT NULL REFERENCES domain_event(id) ON DELETE CASCADE,
    description TEXT,
    delay_ms INTEGER NOT NULL DEFAULT 0,
    condition VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_causality UNIQUE (cause_event_id, effect_event_id)
);

CREATE INDEX idx_causality_ontology ON causality(ontology_id);
CREATE INDEX idx_causality_cause ON causality(cause_event_id);
CREATE INDEX idx_causality_effect ON causality(effect_event_id);

-- 自动更新触发器
CREATE TRIGGER update_domain_event_updated_at BEFORE UPDATE ON domain_event
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 注释
COMMENT ON TABLE domain_event IS '领域事件定义表 (P05)';
COMMENT ON TABLE causality IS '事件因果关系表 (P05)';

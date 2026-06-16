-- V9: Validation rules, guardrails, policies, probes
-- US: 规则约束维度导入 (项目1 维3)
--
-- 覆盖: validations, guardrails, policies, probes

-- ==================== 校验规则 ====================
CREATE TABLE IF NOT EXISTS validation_rule (
    id              UUID PRIMARY KEY,
    ontology_id     UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    entity_id       VARCHAR(255),                         -- 关联 object_type.name
    field_name      VARCHAR(200),                         -- 关联 property_definition.name
    rule_type       VARCHAR(50)  NOT NULL,                -- REQUIRED / UNIQUE / RANGE / REGEX / CUSTOM / EXPRESSION
    rule_name       VARCHAR(200) NOT NULL,
    description     TEXT,
    severity        VARCHAR(20)  DEFAULT 'ERROR',         -- ERROR / WARNING / INFO
    expression      TEXT,                                 -- OCL / SpEL / 正则
    error_message   VARCHAR(500),
    enabled         BOOLEAN      DEFAULT TRUE,
    sort_order      INT          DEFAULT 0,
    extended_data   JSONB        DEFAULT '{}',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted         BOOLEAN      DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_vr_ontology ON validation_rule(ontology_id);
CREATE INDEX IF NOT EXISTS idx_vr_entity ON validation_rule(entity_id);
CREATE INDEX IF NOT EXISTS idx_vr_type ON validation_rule(rule_type);

-- ==================== 护栏规则 ====================
CREATE TABLE IF NOT EXISTS guardrail_rule (
    id              UUID PRIMARY KEY,
    ontology_id     UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    rule_name       VARCHAR(200) NOT NULL,
    description     TEXT,
    condition_expr  TEXT         NOT NULL,                 -- 触发条件表达式
    action_type     VARCHAR(50)  NOT NULL,                 -- BLOCK / WARN / ALLOW / REDIRECT / LOG
    action_config   JSONB        DEFAULT '{}',             -- 动作参数
    priority        INT          DEFAULT 0,
    enabled         BOOLEAN      DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted         BOOLEAN      DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_gr_ontology ON guardrail_rule(ontology_id);

-- ==================== 策略 ====================
CREATE TABLE IF NOT EXISTS policy_rule (
    id              UUID PRIMARY KEY,
    ontology_id     UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    policy_name     VARCHAR(200) NOT NULL,
    description     TEXT,
    policy_type     VARCHAR(50)  DEFAULT 'ACCESS',         -- ACCESS / RATE_LIMIT / DATA_MASK / AUDIT
    rules           JSONB        NOT NULL DEFAULT '[]',    -- 策略规则列表
    effect          VARCHAR(20)  DEFAULT 'ALLOW',          -- ALLOW / DENY / MASK
    priority        INT          DEFAULT 0,
    enabled         BOOLEAN      DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted         BOOLEAN      DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_pr_ontology ON policy_rule(ontology_id);

-- ==================== 探针定义 ====================
CREATE TABLE IF NOT EXISTS probe_definition (
    id                UUID PRIMARY KEY,
    ontology_id       UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    probe_name        VARCHAR(200) NOT NULL,
    description       TEXT,
    target            VARCHAR(500) NOT NULL,                -- 探测目标
    probe_type        VARCHAR(50)  DEFAULT 'HTTP',          -- HTTP / SQL / CUSTOM / HEALTH
    frequency_sec     INT          DEFAULT 300,             -- 探测频率(秒)
    timeout_ms        INT          DEFAULT 5000,
    alert_condition   VARCHAR(500),                         -- 告警条件表达式
    alert_severity    VARCHAR(20)  DEFAULT 'WARNING',
    enabled           BOOLEAN      DEFAULT TRUE,
    config            JSONB        DEFAULT '{}',
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted           BOOLEAN      DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_pd_ontology ON probe_definition(ontology_id);

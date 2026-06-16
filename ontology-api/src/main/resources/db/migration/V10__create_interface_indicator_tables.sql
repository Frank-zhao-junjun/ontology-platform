-- V10: Interface definitions — APIs, queries, compute, notifications, reports
-- US: 外部接口维度导入 (项目1 维5)

-- ==================== API 定义 ====================
CREATE TABLE IF NOT EXISTS api_definition (
    id              UUID PRIMARY KEY,
    ontology_id     UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    api_name        VARCHAR(200) NOT NULL,
    description     TEXT,
    url             VARCHAR(500),
    http_method     VARCHAR(10)  DEFAULT 'GET',            -- GET / POST / PUT / DELETE / PATCH
    request_schema  JSONB        DEFAULT '{}',              -- 请求参数定义
    response_schema JSONB        DEFAULT '{}',              -- 响应结构定义
    auth_type       VARCHAR(50)  DEFAULT 'NONE',            -- NONE / BASIC / OAUTH2 / API_KEY / JWT
    rate_limit      INT          DEFAULT 0,                 -- 0 = unlimited
    timeout_ms      INT          DEFAULT 30000,
    enabled         BOOLEAN      DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted         BOOLEAN      DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_ad_ontology ON api_definition(ontology_id);

-- ==================== 查询定义 ====================
CREATE TABLE IF NOT EXISTS query_definition (
    id              UUID PRIMARY KEY,
    ontology_id     UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    query_name      VARCHAR(200) NOT NULL,
    description     TEXT,
    query_type      VARCHAR(50)  DEFAULT 'CUSTOM',          -- GRAPHQL / SQL / REST / SPARQL / CUSTOM
    query_template  TEXT         NOT NULL,                   -- 查询模板
    parameters      JSONB        DEFAULT '[]',               -- 参数定义 [{name, type, required, default}]
    result_schema   JSONB        DEFAULT '{}',               -- 结果结构
    timeout_ms      INT          DEFAULT 30000,
    enabled         BOOLEAN      DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted         BOOLEAN      DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_qd_ontology ON query_definition(ontology_id);

-- ==================== 计算定义 ====================
CREATE TABLE IF NOT EXISTS compute_definition (
    id              UUID PRIMARY KEY,
    ontology_id     UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    compute_name    VARCHAR(200) NOT NULL,
    description     TEXT,
    input_schema    JSONB        DEFAULT '{}',
    formula         TEXT         NOT NULL,                   -- 计算公式 / 脚本
    output_type     VARCHAR(50)  DEFAULT 'NUMBER',
    output_schema   JSONB        DEFAULT '{}',
    timeout_ms      INT          DEFAULT 30000,
    enabled         BOOLEAN      DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted         BOOLEAN      DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_cd_ontology ON compute_definition(ontology_id);

-- ==================== 通知定义 ====================
CREATE TABLE IF NOT EXISTS notification_definition (
    id              UUID PRIMARY KEY,
    ontology_id     UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    notif_name      VARCHAR(200) NOT NULL,
    description     TEXT,
    channel         VARCHAR(50)  NOT NULL,                  -- EMAIL / SMS / WEBHOOK / DINGTALK / FEISHU / WECHAT
    template        TEXT,                                    -- 通知模板
    recipients      JSONB        DEFAULT '[]',               -- 收件人列表
    trigger_event   VARCHAR(200),                            -- 触发事件名
    enabled         BOOLEAN      DEFAULT TRUE,
    config          JSONB        DEFAULT '{}',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted         BOOLEAN      DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_nd_ontology ON notification_definition(ontology_id);

-- ==================== 报表定义 ====================
CREATE TABLE IF NOT EXISTS report_definition (
    id              UUID PRIMARY KEY,
    ontology_id     UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    report_name     VARCHAR(200) NOT NULL,
    description     TEXT,
    report_format   VARCHAR(20)  DEFAULT 'TABLE',           -- TABLE / CHART / PIVOT / CUSTOM
    fields          JSONB        DEFAULT '[]',               -- 字段定义
    data_source     VARCHAR(200),
    query_id        UUID REFERENCES query_definition(id),
    schedule_cron   VARCHAR(100),                            -- 定时生成 cron 表达式
    enabled         BOOLEAN      DEFAULT TRUE,
    config          JSONB        DEFAULT '{}',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted         BOOLEAN      DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_rd_ontology ON report_definition(ontology_id);

-- ==================== 指标定义 (扩展现有行为模块) ====================
-- 指标在项目1中属于维2_动态行为，当前无对应表
CREATE TABLE IF NOT EXISTS indicator_definition (
    id                  UUID PRIMARY KEY,
    ontology_id         UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    indicator_name      VARCHAR(200) NOT NULL,
    description         TEXT,
    formula             TEXT,                                -- 计算公式
    target_value        VARCHAR(100),                        -- 目标值
    unit                VARCHAR(50),                         -- 单位
    warning_threshold   VARCHAR(200),                        -- 告警阈值表达式
    critical_threshold  VARCHAR(200),                        -- 严重阈值表达式
    aggregation_type    VARCHAR(50)  DEFAULT 'COUNT',         -- COUNT / SUM / AVG / MAX / MIN / CUSTOM
    frequency_sec       INT          DEFAULT 3600,           -- 计算频率
    enabled             BOOLEAN      DEFAULT TRUE,
    extended_data       JSONB        DEFAULT '{}',
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted             BOOLEAN      DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_id_ontology ON indicator_definition(ontology_id);

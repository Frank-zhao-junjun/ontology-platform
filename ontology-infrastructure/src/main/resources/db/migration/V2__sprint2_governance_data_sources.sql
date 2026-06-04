-- Sprint 2: 数据源 + 角色权限 + Agent 沙箱（对齐 TDD v2.0 §2.2.8 / §2.5）
-- 依赖 bounded_contexts / object_types 由 V1 或既有库提供；本脚本仅增 Sprint 2 表。

CREATE TABLE IF NOT EXISTS data_sources (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    code            VARCHAR(50) NOT NULL UNIQUE,
    source_type     VARCHAR(20) NOT NULL,
    connection_config JSONB NOT NULL DEFAULT '{}',
    credential_ref  VARCHAR(200),
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS data_access_methods (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    object_type_id  UUID NOT NULL,
    data_source_id  UUID NOT NULL REFERENCES data_sources(id),
    method_type     VARCHAR(20) NOT NULL,
    access_config   JSONB NOT NULL DEFAULT '{}',
    cache_ttl_sec   INT DEFAULT 300,
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(object_type_id, data_source_id, method_type)
);

CREATE TABLE IF NOT EXISTS roles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context_id      UUID,
    name            VARCHAR(50) NOT NULL,
    code            VARCHAR(30) NOT NULL,
    description     TEXT,
    is_global       BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(context_id, code)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id         UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    object_type_id  UUID NOT NULL,
    perm_read       BOOLEAN DEFAULT FALSE,
    perm_write      BOOLEAN DEFAULT FALSE,
    perm_delete     BOOLEAN DEFAULT FALSE,
    perm_execute    BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(role_id, object_type_id)
);

CREATE TABLE IF NOT EXISTS agent_sandboxes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    manifest_version_id UUID,
    agent_role_id   UUID REFERENCES roles(id),
    allowed_tools   JSONB NOT NULL DEFAULT '[]',
    allowed_aggregate_roots JSONB NOT NULL DEFAULT '[]',
    allowed_behaviors JSONB NOT NULL DEFAULT '[]',
    max_ops_per_second INT DEFAULT 10,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW()
);

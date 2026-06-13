-- =============================================
-- 本体建模平台 - 治理层表
-- V6__create_governance_tables.sql
-- 覆盖 US: P08
-- =============================================

-- Agent 令牌表
CREATE TABLE agent_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id VARCHAR(200) NOT NULL,
    token_hash VARCHAR(500) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    display_name VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    issued_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    last_used_at TIMESTAMPTZ,
    created_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_agent_token_agent_id UNIQUE (agent_id)
);

CREATE INDEX idx_agent_token_tenant ON agent_token(tenant_id);
CREATE INDEX idx_agent_token_status ON agent_token(status);
CREATE INDEX idx_agent_token_expires ON agent_token(expires_at);

-- Agent 角色表
CREATE TABLE agent_role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token_id UUID NOT NULL REFERENCES agent_token(id) ON DELETE CASCADE,
    domain VARCHAR(200) NOT NULL,
    role VARCHAR(50) NOT NULL,
    granted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_token_domain UNIQUE (token_id, domain)
);

CREATE INDEX idx_agent_role_token ON agent_role(token_id);
CREATE INDEX idx_agent_role_domain ON agent_role(domain);

-- 角色权限表
CREATE TABLE role_permission (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID NOT NULL REFERENCES agent_role(id) ON DELETE CASCADE,
    resource VARCHAR(200) NOT NULL,
    operations JSONB NOT NULL DEFAULT '[]',
    domain VARCHAR(200) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_role_permission_role ON role_permission(role_id);
CREATE INDEX idx_role_permission_resource ON role_permission(resource);
CREATE INDEX idx_role_permission_domain ON role_permission(domain);
CREATE INDEX idx_role_permission_ops ON role_permission USING GIN (operations);

-- 审批请求表
CREATE TABLE approval_request (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id VARCHAR(200) NOT NULL,
    action_id UUID REFERENCES action_definition(id) ON DELETE SET NULL,
    requested_op VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason TEXT,
    requested_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at TIMESTAMPTZ,
    resolved_by VARCHAR(100)
);

CREATE INDEX idx_approval_agent ON approval_request(agent_id);
CREATE INDEX idx_approval_status ON approval_request(status);
CREATE INDEX idx_approval_action ON approval_request(action_id);
CREATE INDEX idx_approval_requested ON approval_request(requested_at);

-- 注释
COMMENT ON TABLE agent_token IS 'Agent 令牌表 (P08) — JWT 签发与吊销';
COMMENT ON TABLE agent_role IS 'Agent 角色表 (P08) — domain+role 绑定';
COMMENT ON TABLE role_permission IS '角色权限表 (P08) — 资源操作授权';
COMMENT ON TABLE approval_request IS '审批请求表 (P08) — 高风险操作审批流';

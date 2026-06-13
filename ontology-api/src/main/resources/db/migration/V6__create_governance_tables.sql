-- V6: Governance — Agent token, role, permission, approval
-- US: P08

CREATE TABLE IF NOT EXISTS agent_token (
    id            UUID PRIMARY KEY,
    agent_id      VARCHAR(200) NOT NULL UNIQUE,
    token_hash    VARCHAR(500) NOT NULL,
    tenant_id     VARCHAR(100) NOT NULL,
    display_name  VARCHAR(500),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    issued_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at    TIMESTAMPTZ  NOT NULL,
    last_used_at  TIMESTAMPTZ,
    created_by    VARCHAR(100),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS agent_role (
    id          UUID PRIMARY KEY,
    token_id    UUID NOT NULL REFERENCES agent_token(id),
    domain      VARCHAR(200) NOT NULL,
    role        VARCHAR(50)  NOT NULL,
    granted_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_token_domain UNIQUE (token_id, domain)
);

CREATE TABLE IF NOT EXISTS role_permission (
    id          UUID PRIMARY KEY,
    role_id     UUID NOT NULL REFERENCES agent_role(id),
    resource    VARCHAR(200) NOT NULL,
    operations  JSONB NOT NULL DEFAULT '[]',
    domain      VARCHAR(200) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS approval_request (
    id            UUID PRIMARY KEY,
    agent_id      VARCHAR(200) NOT NULL,
    action_id     UUID REFERENCES action_definition(id),
    requested_op  VARCHAR(50) NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason        TEXT,
    requested_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at   TIMESTAMPTZ,
    resolved_by   VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_token_agent ON agent_token(agent_id);
CREATE INDEX IF NOT EXISTS idx_token_tenant ON agent_token(tenant_id);
CREATE INDEX IF NOT EXISTS idx_role_token ON agent_role(token_id);
CREATE INDEX IF NOT EXISTS idx_role_domain ON agent_role(domain);
CREATE INDEX IF NOT EXISTS idx_approval_agent ON approval_request(agent_id);

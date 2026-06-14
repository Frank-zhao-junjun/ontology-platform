-- V8: Idempotency record table (Phase 2a - F02)
CREATE TABLE idempotency_record (
    idempotency_key VARCHAR(128) PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    agent_id VARCHAR(200),
    http_method VARCHAR(10) NOT NULL,
    request_path VARCHAR(500) NOT NULL,
    response_status INTEGER,
    response_body JSONB,
    created_at TIMESTAMPTZ DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_idempotency_expires ON idempotency_record(expires_at);

-- Phase 2b tables
CREATE TABLE job_record (
    id UUID PRIMARY KEY,
    job_type VARCHAR(100) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    agent_id VARCHAR(200),
    idempotency_key VARCHAR(128),
    status VARCHAR(20) DEFAULT 'QUEUED',
    payload JSONB NOT NULL,
    result JSONB,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    next_retry_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now(),
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    CONSTRAINT ck_job_status CHECK (status IN ('QUEUED','RUNNING','COMPLETED','FAILED','CANCELLED'))
);
CREATE INDEX idx_job_status ON job_record(status, created_at);
CREATE INDEX idx_job_tenant ON job_record(tenant_id, created_at);

CREATE TABLE webhook_subscription (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    agent_id VARCHAR(200),
    callback_url VARCHAR(1000) NOT NULL,
    event_types JSONB NOT NULL DEFAULT '["job.completed","job.failed"]',
    secret VARCHAR(256) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE rate_limit_config (
    id UUID PRIMARY KEY,
    scope_type VARCHAR(20) NOT NULL,
    scope_value VARCHAR(200) NOT NULL,
    window_seconds INTEGER DEFAULT 60,
    max_requests INTEGER DEFAULT 100,
    burst_size INTEGER DEFAULT 20,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT uq_rate_limit UNIQUE (scope_type, scope_value)
);

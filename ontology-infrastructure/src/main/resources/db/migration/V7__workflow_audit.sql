-- V7: 建模工作流审计 (US-G05)
-- workflow_state_log: 状态流转审计日志
-- review_comments: 审核批注（可选，demo阶段不强制双人审核）

CREATE TABLE IF NOT EXISTS workflow_state_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context_id      UUID NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    from_state      VARCHAR(20) NOT NULL,
    to_state        VARCHAR(20) NOT NULL,
    operated_by     VARCHAR(100) NOT NULL,
    operated_at     TIMESTAMP DEFAULT NOW(),
    comment         TEXT
);

CREATE INDEX idx_wsl_context ON workflow_state_log(context_id);
CREATE INDEX idx_wsl_operated_at ON workflow_state_log(context_id, operated_at DESC);

CREATE TABLE IF NOT EXISTS review_comments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context_id      UUID NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    target_type     VARCHAR(30) NOT NULL,
    target_id       UUID NOT NULL,
    reviewer        VARCHAR(100) NOT NULL,
    resolution      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    content         TEXT NOT NULL,
    created_at      TIMESTAMP DEFAULT NOW(),
    resolved_at     TIMESTAMP
);

CREATE INDEX idx_rc_context ON review_comments(context_id);
CREATE INDEX idx_rc_target ON review_comments(target_type, target_id);

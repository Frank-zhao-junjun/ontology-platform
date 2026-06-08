ALTER TABLE domain_events ADD COLUMN IF NOT EXISTS event_type VARCHAR(30) DEFAULT 'DOMAIN_EVENT';

CREATE TABLE IF NOT EXISTS event_routes (
    id VARCHAR(36) PRIMARY KEY,
    context_id VARCHAR(36) NOT NULL,
    manifest_code VARCHAR(80) NOT NULL,
    source_event_id VARCHAR(36) NOT NULL,
    route_targets_json TEXT NOT NULL DEFAULT '[]',
    filter_conditions_json TEXT DEFAULT '[]',
    created_at TIMESTAMP,
    CONSTRAINT uk_event_routes_ctx_code UNIQUE (context_id, manifest_code)
);

CREATE TABLE IF NOT EXISTS event_handlers (
    id VARCHAR(36) PRIMARY KEY,
    context_id VARCHAR(36) NOT NULL,
    manifest_code VARCHAR(80) NOT NULL,
    event_id VARCHAR(36) NOT NULL,
    handler_behavior_id VARCHAR(36) NOT NULL,
    scenario_id VARCHAR(36),
    precondition_state VARCHAR(80),
    priority INT NOT NULL DEFAULT 100,
    execution_mode VARCHAR(10) DEFAULT 'SYNC',
    created_at TIMESTAMP,
    CONSTRAINT uk_event_handlers_ctx_code UNIQUE (context_id, manifest_code)
);

CREATE INDEX IF NOT EXISTS idx_event_routes_source ON event_routes(source_event_id);
CREATE INDEX IF NOT EXISTS idx_event_routes_context ON event_routes(context_id);
CREATE INDEX IF NOT EXISTS idx_event_handlers_event ON event_handlers(event_id);
CREATE INDEX IF NOT EXISTS idx_event_handlers_behavior ON event_handlers(handler_behavior_id);
CREATE INDEX IF NOT EXISTS idx_event_handlers_context ON event_handlers(context_id);

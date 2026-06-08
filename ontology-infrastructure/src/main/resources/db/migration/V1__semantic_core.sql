-- Sprint 1 语义层核心表（TDD v2.0 §2.2.1–2.2.5）

CREATE TABLE IF NOT EXISTS bounded_contexts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    code            VARCHAR(50) NOT NULL UNIQUE,
    description     TEXT,
    domain_tag      VARCHAR(50),
    ontology_id     UUID NOT NULL UNIQUE,
    workflow_state  VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by      VARCHAR(100),
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aggregate_roots (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context_id      UUID NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    code            VARCHAR(50) NOT NULL,
    description     TEXT,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(context_id, code)
);

CREATE TABLE IF NOT EXISTS object_types (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context_id      UUID NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    aggregate_root_id UUID REFERENCES aggregate_roots(id) ON DELETE SET NULL,
    parent_object_id UUID REFERENCES object_types(id),
    name            VARCHAR(100) NOT NULL,
    code            VARCHAR(50) NOT NULL,
    object_kind     VARCHAR(20) NOT NULL DEFAULT 'ENTITY',
    description     TEXT,
    attributes      JSONB NOT NULL DEFAULT '[]',
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(context_id, code)
);

CREATE TABLE IF NOT EXISTS relationships (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context_id      UUID NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    code            VARCHAR(50) NOT NULL,
    source_object_id UUID NOT NULL REFERENCES object_types(id),
    target_object_id UUID NOT NULL REFERENCES object_types(id),
    cardinality     VARCHAR(10) NOT NULL DEFAULT '1:N',
    relation_kind   VARCHAR(20) NOT NULL DEFAULT 'REFERENCE',
    is_cross_context BOOLEAN DEFAULT FALSE,
    target_context_id UUID REFERENCES bounded_contexts(id),
    created_at      TIMESTAMP DEFAULT NOW()
);

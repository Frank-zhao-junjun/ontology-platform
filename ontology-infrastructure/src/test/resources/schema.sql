-- Test schema for H2 database
CREATE TABLE IF NOT EXISTS ontology (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001',
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    description TEXT,
    version VARCHAR(20) NOT NULL DEFAULT '0.1.0',
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    published_at TIMESTAMP WITH TIME ZONE,
    object_type_count INT DEFAULT 0,
    action_type_count INT DEFAULT 0,
    created_by VARCHAR(36),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ontology_tenant ON ontology(tenant_id);
CREATE INDEX IF NOT EXISTS idx_ontology_status ON ontology(status);
CREATE INDEX IF NOT EXISTS idx_ontology_name ON ontology(name);

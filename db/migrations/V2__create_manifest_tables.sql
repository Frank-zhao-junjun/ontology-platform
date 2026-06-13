-- =============================================
-- 本体建模平台 - Manifest 导入与版本表
-- V2__create_manifest_tables.sql
-- 覆盖 US: P01, P03
-- =============================================

-- Manifest 导入记录表
CREATE TABLE manifest_import (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ontology_id UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    external_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL DEFAULT 'default',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    api_version VARCHAR(50) NOT NULL,
    manifest_version VARCHAR(50) NOT NULL,
    source_format VARCHAR(10) NOT NULL,
    raw_content JSONB NOT NULL,
    imported_counts JSONB DEFAULT '{}',
    validation_errors JSONB DEFAULT '[]',
    created_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ,
    CONSTRAINT uq_external_version UNIQUE (external_id, manifest_version)
);

CREATE INDEX idx_manifest_import_ontology ON manifest_import(ontology_id);
CREATE INDEX idx_manifest_import_tenant ON manifest_import(tenant_id);
CREATE INDEX idx_manifest_import_status ON manifest_import(status);
CREATE INDEX idx_manifest_import_external ON manifest_import(external_id);
CREATE INDEX idx_manifest_import_raw ON manifest_import USING GIN (raw_content);

-- Manifest 版本快照表
CREATE TABLE manifest_version (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ontology_id UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    import_id UUID REFERENCES manifest_import(id) ON DELETE SET NULL,
    version VARCHAR(50) NOT NULL,
    manifest_json JSONB NOT NULL,
    change_summary JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_manifest_version_ontology ON manifest_version(ontology_id);
CREATE INDEX idx_manifest_version_import ON manifest_version(import_id);
CREATE INDEX idx_manifest_version_version ON manifest_version(version);
CREATE INDEX idx_manifest_version_json ON manifest_version USING GIN (manifest_json);

-- 自动更新 updated_at
CREATE TRIGGER update_manifest_import_updated_at BEFORE UPDATE ON manifest_import
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 注释
COMMENT ON TABLE manifest_import IS 'Manifest 导入记录表 (P01)';
COMMENT ON TABLE manifest_version IS 'Manifest 版本快照表 (P03)';

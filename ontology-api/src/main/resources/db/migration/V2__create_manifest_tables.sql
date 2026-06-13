-- V2: Manifest import & version tracking
-- US: P01, P03

CREATE TABLE IF NOT EXISTS manifest_import (
    id               UUID PRIMARY KEY,
    ontology_id      UUID NOT NULL,
    external_id      VARCHAR(255) NOT NULL,
    tenant_id        VARCHAR(100) NOT NULL DEFAULT 'default',
    status           VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    api_version      VARCHAR(50)  NOT NULL,
    manifest_version VARCHAR(50)  NOT NULL,
    source_format    VARCHAR(10)  NOT NULL,
    raw_content      JSONB        NOT NULL,
    imported_counts  JSONB        NOT NULL DEFAULT '{}',
    validation_errors JSONB       DEFAULT '[]',
    created_by       VARCHAR(100),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at     TIMESTAMPTZ,
    CONSTRAINT uq_external_version UNIQUE (external_id, manifest_version)
);

CREATE TABLE IF NOT EXISTS manifest_version (
    id              UUID PRIMARY KEY,
    ontology_id     UUID NOT NULL,
    import_id       UUID REFERENCES manifest_import(id),
    version         VARCHAR(50)  NOT NULL,
    manifest_json   JSONB        NOT NULL,
    change_summary  JSONB        DEFAULT '{}',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_manifest_import_ontology ON manifest_import(ontology_id);
CREATE INDEX IF NOT EXISTS idx_manifest_import_status   ON manifest_import(status);
CREATE INDEX IF NOT EXISTS idx_manifest_version_ontology ON manifest_version(ontology_id);

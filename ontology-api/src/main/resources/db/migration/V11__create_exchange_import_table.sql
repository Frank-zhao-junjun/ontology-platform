-- V9: Exchange import table (Phase 3a - exchange import drafts)
CREATE TABLE exchange_import (
    id VARCHAR(200) PRIMARY KEY,
    metadata_id VARCHAR(200),
    metadata_name VARCHAR(500),
    metadata_version VARCHAR(100),
    metadata_source VARCHAR(100),
    metadata_status VARCHAR(50) DEFAULT 'draft',
    project_id VARCHAR(200),
    project_name VARCHAR(500),
    raw_document JSONB NOT NULL,              -- 完整原始文档
    validation_status VARCHAR(20) DEFAULT 'pending',  -- pending|validating|passed|failed
    validation_report JSONB,                   -- 校验报告
    imported_at TIMESTAMPTZ DEFAULT now(),
    published_at TIMESTAMPTZ,
    created_by VARCHAR(200),
    updated_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_exchange_status ON exchange_import(validation_status, imported_at);
CREATE INDEX idx_exchange_project ON exchange_import(project_id);

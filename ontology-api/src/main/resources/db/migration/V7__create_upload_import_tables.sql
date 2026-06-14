-- V7: Upload Task + Import Task tables
-- Phase 1: Replaces InMemoryUploadTaskRepository / InMemoryImportTaskRepository

CREATE TABLE upload_task (
    id VARCHAR(36) PRIMARY KEY,
    original_file_name VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    chunk_size INT NOT NULL DEFAULT 5242880,
    total_chunks INT NOT NULL DEFAULT 1,
    target_type VARCHAR(100),
    ontology_id VARCHAR(36),
    object_type_name VARCHAR(200),
    user_id VARCHAR(100),
    tenant_id VARCHAR(100) DEFAULT 'default',
    status VARCHAR(20) DEFAULT 'PENDING',
    uploaded_chunks JSONB DEFAULT '[]',
    stored_file_path VARCHAR(1000),
    file_md5 VARCHAR(64),
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE import_task (
    id VARCHAR(36) PRIMARY KEY,
    upload_id VARCHAR(36),
    ontology_id VARCHAR(36),
    object_type_name VARCHAR(200),
    object_type_id VARCHAR(36),
    merge_strategy VARCHAR(20) DEFAULT 'UPSERT',
    error_handling VARCHAR(20) DEFAULT 'SKIP',
    user_id VARCHAR(100),
    tenant_id VARCHAR(100) DEFAULT 'default',
    status VARCHAR(20) DEFAULT 'PENDING',
    total_rows BIGINT DEFAULT 0,
    processed_rows BIGINT DEFAULT 0,
    success_rows BIGINT DEFAULT 0,
    failed_rows BIGINT DEFAULT 0,
    errors JSONB DEFAULT '[]',
    created_at TIMESTAMPTZ DEFAULT now(),
    completed_at TIMESTAMPTZ,
    estimated_completion TIMESTAMPTZ
);

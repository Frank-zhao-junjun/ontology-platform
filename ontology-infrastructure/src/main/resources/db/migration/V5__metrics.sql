-- US-B05: Metrics definition table
CREATE TABLE IF NOT EXISTS metrics (
    id                       VARCHAR(36) PRIMARY KEY,
    context_id               VARCHAR(36) NOT NULL REFERENCES bounded_contexts(id) ON DELETE CASCADE,
    manifest_code            VARCHAR(80) NOT NULL,
    name                     VARCHAR(200) NOT NULL,
    name_en                  VARCHAR(200),
    formula                  TEXT NOT NULL,
    data_source_ref_json     TEXT NOT NULL DEFAULT '[]',
    aggregation_dimensions_json TEXT DEFAULT '[]',
    period                   VARCHAR(100),
    created_at               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(context_id, manifest_code)
);

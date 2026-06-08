-- V9: Value Objects (US-S05)
-- 全局可复用值对象，跨上下文引用
CREATE TABLE value_objects (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(80) NOT NULL,
    name_en VARCHAR(200),
    description TEXT,
    properties_json TEXT NOT NULL DEFAULT '[]',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    UNIQUE (code)
);

CREATE INDEX idx_value_objects_code ON value_objects(code);

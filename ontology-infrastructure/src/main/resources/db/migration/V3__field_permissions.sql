-- US-G02 字段级权限

CREATE TABLE IF NOT EXISTS field_permissions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id         UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    object_type_id  UUID NOT NULL,
    field_name      VARCHAR(100) NOT NULL,
    is_visible      BOOLEAN DEFAULT TRUE,
    is_editable     BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(role_id, object_type_id, field_name)
);

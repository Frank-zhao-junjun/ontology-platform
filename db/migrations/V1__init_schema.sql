-- =============================================
-- 本体建模平台 - 初始化数据库架构
-- V1__init_schema.sql
-- =============================================

-- 创建扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- =============================================
-- 1. 本体定义表
-- =============================================
CREATE TABLE ontology (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'::uuid,
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    description TEXT,
    version VARCHAR(20) NOT NULL DEFAULT '0.1.0',
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    published_at TIMESTAMP WITH TIME ZONE,
    object_type_count INT DEFAULT 0,
    action_type_count INT DEFAULT 0,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_ontology_tenant_name UNIQUE (tenant_id, name)
);

CREATE INDEX idx_ontology_tenant ON ontology(tenant_id);
CREATE INDEX idx_ontology_status ON ontology(status);
CREATE INDEX idx_ontology_created_at ON ontology(created_at);

-- =============================================
-- 2. 对象类型表
-- =============================================
CREATE TABLE object_type (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ontology_id UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    description TEXT,
    primary_key VARCHAR(100) NOT NULL,
    parent_id UUID REFERENCES object_type(id),
    instance_count INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_object_type_ontology_name UNIQUE (ontology_id, name)
);

CREATE INDEX idx_object_type_ontology ON object_type(ontology_id);
CREATE INDEX idx_object_type_parent ON object_type(parent_id);
CREATE INDEX idx_object_type_name ON object_type(name);

-- =============================================
-- 3. 属性定义表
-- =============================================
CREATE TABLE property_definition (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    object_type_id UUID NOT NULL REFERENCES object_type(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    description TEXT,
    data_type VARCHAR(50) NOT NULL,
    is_computed BOOLEAN DEFAULT FALSE,
    is_required BOOLEAN DEFAULT FALSE,
    is_unique BOOLEAN DEFAULT FALSE,
    is_searchable BOOLEAN DEFAULT TRUE,
    is_sortable BOOLEAN DEFAULT TRUE,
    default_value JSONB,
    sort_order INT DEFAULT 0,
    extended_data JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_property_object_type_name UNIQUE (object_type_id, name)
);

CREATE INDEX idx_property_object_type ON property_definition(object_type_id);
CREATE INDEX idx_property_sort_order ON property_definition(object_type_id, sort_order);
CREATE INDEX idx_property_extended_data ON property_definition USING GIN (extended_data);

-- =============================================
-- 4. 关系定义表
-- =============================================
CREATE TABLE relation_definition (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ontology_id UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    source_type_id UUID NOT NULL REFERENCES object_type(id) ON DELETE CASCADE,
    target_type_id UUID NOT NULL REFERENCES object_type(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    description TEXT,
    cardinality VARCHAR(10) NOT NULL DEFAULT '1:N',
    reverse_name VARCHAR(100),
    reverse_display_name VARCHAR(200),
    extended_data JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_relation_ontology_name UNIQUE (ontology_id, name)
);

CREATE INDEX idx_relation_ontology ON relation_definition(ontology_id);
CREATE INDEX idx_relation_source ON relation_definition(source_type_id);
CREATE INDEX idx_relation_target ON relation_definition(target_type_id);

-- =============================================
-- 5. 关系属性表
-- =============================================
CREATE TABLE relation_property (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    relation_id UUID NOT NULL REFERENCES relation_definition(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200),
    data_type VARCHAR(50) NOT NULL,
    is_required BOOLEAN DEFAULT FALSE,
    default_value JSONB,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_relation_property_relation ON relation_property(relation_id);

-- =============================================
-- 6. 对象实例表
-- =============================================
CREATE TABLE object_instance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ontology_id UUID NOT NULL REFERENCES ontology(id) ON DELETE CASCADE,
    object_type_id UUID NOT NULL REFERENCES object_type(id) ON DELETE CASCADE,
    primary_key_value VARCHAR(255) NOT NULL,
    core_data JSONB NOT NULL DEFAULT '{}',
    extended_data JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_object_instance_type_pk UNIQUE (object_type_id, primary_key_value)
);

CREATE INDEX idx_object_instance_ontology ON object_instance(ontology_id);
CREATE INDEX idx_object_instance_type ON object_instance(object_type_id);
CREATE INDEX idx_object_instance_pk ON object_instance(primary_key_value);
CREATE INDEX idx_object_instance_core_data ON object_instance USING GIN (core_data);
CREATE INDEX idx_object_instance_updated ON object_instance(updated_at);

-- =============================================
-- 7. 审计日志表
-- =============================================
CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'::uuid,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    api_key_id UUID,
    api_key_name VARCHAR(100),
    action VARCHAR(100) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    object_type VARCHAR(100),
    object_id VARCHAR(255),
    object_name VARCHAR(200),
    request_id VARCHAR(100),
    request_method VARCHAR(10),
    request_path VARCHAR(500),
    request_params JSONB,
    request_body JSONB,
    response_status VARCHAR(20),
    response_code INT,
    response_data JSONB,
    error_message TEXT,
    execution_time_ms INT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_timestamp ON audit_log(timestamp);
CREATE INDEX idx_audit_tenant ON audit_log(tenant_id);
CREATE INDEX idx_audit_api_key ON audit_log(api_key_id);
CREATE INDEX idx_audit_object ON audit_log(object_type, object_id);
CREATE INDEX idx_audit_action ON audit_log(action);
CREATE INDEX idx_audit_request_id ON audit_log(request_id);

-- =============================================
-- 创建更新时间戳触发器
-- =============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_ontology_updated_at BEFORE UPDATE ON ontology
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_object_type_updated_at BEFORE UPDATE ON object_type
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_property_updated_at BEFORE UPDATE ON property_definition
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_relation_updated_at BEFORE UPDATE ON relation_definition
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_object_instance_updated_at BEFORE UPDATE ON object_instance
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- 注释
-- =============================================
COMMENT ON TABLE ontology IS '本体定义表';
COMMENT ON TABLE object_type IS '对象类型表';
COMMENT ON TABLE property_definition IS '属性定义表';
COMMENT ON TABLE relation_definition IS '关系定义表';
COMMENT ON TABLE relation_property IS '关系属性表';
COMMENT ON TABLE object_instance IS '对象实例表';
COMMENT ON TABLE audit_log IS '审计日志表';

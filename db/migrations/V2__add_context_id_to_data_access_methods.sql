-- =============================================
-- V2: 为 data_access_methods 表添加 context_id 字段
-- 修复 US-S08 数据获取方式上下文信息丢失问题
-- =============================================

ALTER TABLE data_access_methods
    ADD COLUMN IF NOT EXISTS context_id VARCHAR(36);

COMMENT ON COLUMN data_access_methods.context_id IS '限界上下文 ID（修复 Sprint 2 gap）';

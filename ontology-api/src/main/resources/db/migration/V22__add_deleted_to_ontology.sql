-- =============================================
-- V22: 为本体定义表添加软删除标记
-- =============================================
ALTER TABLE ontology ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;

-- 为已存在的记录设置默认值
UPDATE ontology SET deleted = FALSE WHERE deleted IS NULL;

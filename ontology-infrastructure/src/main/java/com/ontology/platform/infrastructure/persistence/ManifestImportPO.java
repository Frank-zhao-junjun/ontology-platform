package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.time.Instant;

/**
 * 清单导入持久化对象
 * Manifest Import Persistence Object
 * 映射 V2 manifest_import 表
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("manifest_import")
public class ManifestImportPO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 本体ID
     */
    @TableField("ontology_id")
    private String ontologyId;

    /**
     * 外部ID（唯一标识之一）
     */
    @TableField("external_id")
    private String externalId;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private String tenantId;

    /**
     * 状态：DRAFT / PUBLISHED / ARCHIVED 等
     */
    @TableField("status")
    private String status;

    /**
     * API版本
     */
    @TableField("api_version")
    private String apiVersion;

    /**
     * 清单版本（与external_id组成唯一约束）
     */
    @TableField("manifest_version")
    private String manifestVersion;

    /**
     * 源格式
     */
    @TableField("source_format")
    private String sourceFormat;

    /**
     * 原始内容（JSONB）
     */
    @TableField(value = "raw_content", typeHandler = JacksonTypeHandler.class)
    private String rawContent;

    /**
     * 导入计数（JSONB）
     */
    @TableField(value = "imported_counts", typeHandler = JacksonTypeHandler.class)
    private String importedCounts;

    /**
     * 校验错误列表（JSONB）
     */
    @TableField(value = "validation_errors", typeHandler = JacksonTypeHandler.class)
    private String validationErrors;

    /**
     * 创建人
     */
    @TableField("created_by")
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private Instant createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private Instant updatedAt;

    /**
     * 发布时间
     */
    @TableField("published_at")
    private Instant publishedAt;
}

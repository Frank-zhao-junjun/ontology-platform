package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("exchange_import")
public class ExchangeImportPO {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("metadata_id")
    private String metadataId;

    @TableField("metadata_name")
    private String metadataName;

    @TableField("metadata_version")
    private String metadataVersion;

    @TableField("metadata_source")
    private String metadataSource;

    @TableField("metadata_status")
    private String metadataStatus;

    @TableField("project_id")
    private String projectId;

    @TableField("project_name")
    private String projectName;

    @TableField("raw_document")
    private String rawDocument;

    @TableField("validation_status")
    private String validationStatus;

    @TableField("validation_report")
    private String validationReport;

    @TableField("imported_at")
    private Instant importedAt;

    @TableField("published_at")
    private Instant publishedAt;

    @TableField("created_by")
    private String createdBy;

    @TableField("updated_at")
    private Instant updatedAt;
}

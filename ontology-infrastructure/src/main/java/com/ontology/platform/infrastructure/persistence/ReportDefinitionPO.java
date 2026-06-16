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
@TableName("report_definition")
public class ReportDefinitionPO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("ontology_id")
    private String ontologyId;
    @TableField("report_name")
    private String reportName;
    @TableField("description")
    private String description;
    @TableField("report_format")
    private String reportFormat;
    @TableField("fields")
    private String fields;
    @TableField("data_source")
    private String dataSource;
    @TableField("query_id")
    private String queryId;
    @TableField("schedule_cron")
    private String scheduleCron;
    @TableField("enabled")
    private Boolean enabled;
    @TableField("config")
    private String config;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;

    @TableField("deleted")
    private Boolean deleted;
}

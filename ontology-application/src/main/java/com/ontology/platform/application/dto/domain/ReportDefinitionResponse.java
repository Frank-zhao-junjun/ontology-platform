package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "ReportDefinition响应")
public class ReportDefinitionResponse {
    @Schema(description = "ID")
    private String id;
    @Schema(description = "报表名称")
    private String reportName;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "报表格式")
    private String reportFormat;
    @Schema(description = "字段")
    private String fields;
    @Schema(description = "数据源")
    private String dataSource;
    @Schema(description = "查询ID")
    private String queryId;
    @Schema(description = "定时cron")
    private String scheduleCron;
    @Schema(description = "是否启用")
    private Boolean enabled;
    @Schema(description = "配置")
    private String config;
    @Schema(description = "创建时间")
    private Instant createdAt;
    @Schema(description = "更新时间")
    private Instant updatedAt;
}

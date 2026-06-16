package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "ValidationRule响应")
public class ValidationRuleResponse {
    @Schema(description = "ID")
    private String id;
    @Schema(description = "实体ID")
    private String entityId;
    @Schema(description = "字段名")
    private String fieldName;
    @Schema(description = "规则类型")
    private String ruleType;
    @Schema(description = "规则名称")
    private String ruleName;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "严重级别")
    private String severity;
    @Schema(description = "表达式")
    private String expression;
    @Schema(description = "错误消息")
    private String errorMessage;
    @Schema(description = "是否启用")
    private Boolean enabled;
    @Schema(description = "排序")
    private Integer sortOrder;
    @Schema(description = "扩展数据")
    private String extendedData;
    @Schema(description = "创建时间")
    private Instant createdAt;
    @Schema(description = "更新时间")
    private Instant updatedAt;
}

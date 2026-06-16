package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建ValidationRule请求")
public class CreateValidationRuleRequest {
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
}

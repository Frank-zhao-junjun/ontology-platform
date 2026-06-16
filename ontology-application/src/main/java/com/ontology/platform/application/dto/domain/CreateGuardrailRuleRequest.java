package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建GuardrailRule请求")
public class CreateGuardrailRuleRequest {
    @Schema(description = "规则名称")
    private String ruleName;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "条件表达式")
    private String conditionExpr;
    @Schema(description = "动作类型")
    private String actionType;
    @Schema(description = "动作配置")
    private String actionConfig;
    @Schema(description = "优先级")
    private Integer priority;
    @Schema(description = "是否启用")
    private Boolean enabled;
}

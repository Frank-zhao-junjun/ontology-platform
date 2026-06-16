package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "GuardrailRule响应")
public class GuardrailRuleResponse {
    @Schema(description = "ID")
    private String id;
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
    @Schema(description = "创建时间")
    private Instant createdAt;
    @Schema(description = "更新时间")
    private Instant updatedAt;
}

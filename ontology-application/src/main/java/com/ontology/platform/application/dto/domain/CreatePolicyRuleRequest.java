package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建PolicyRule请求")
public class CreatePolicyRuleRequest {
    @Schema(description = "策略名称")
    private String policyName;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "策略类型")
    private String policyType;
    @Schema(description = "规则定义")
    private String rules;
    @Schema(description = "效果")
    private String effect;
    @Schema(description = "优先级")
    private Integer priority;
    @Schema(description = "是否启用")
    private Boolean enabled;
}

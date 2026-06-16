package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PolicyRule响应")
public class PolicyRuleResponse {
    @Schema(description = "ID")
    private String id;
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
    @Schema(description = "创建时间")
    private Instant createdAt;
    @Schema(description = "更新时间")
    private Instant updatedAt;
}

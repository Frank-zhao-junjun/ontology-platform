package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建AgentPolicySemantic请求")
public class CreateAgentPolicySemanticRequest {
    @Schema(description = "name") private String name;
    @Schema(description = "role_id") private String roleId;
    @Schema(description = "intent_patterns") private String intentPatterns;
    @Schema(description = "allow_actions") private String allowActions;
    @Schema(description = "deny_actions") private String denyActions;
    @Schema(description = "require_confirm") private String requireConfirm;
    @Schema(description = "is_active") private Boolean isActive;
}

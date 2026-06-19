package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "AgentPolicySemantic响应")
public class AgentPolicySemanticResponse {
    @Schema(description = "ID") private String id;
    @Schema(description = "name") private String name;
    @Schema(description = "role_id") private String roleId;
    @Schema(description = "intent_patterns") private String intentPatterns;
    @Schema(description = "allow_actions") private String allowActions;
    @Schema(description = "deny_actions") private String denyActions;
    @Schema(description = "require_confirm") private String requireConfirm;
    @Schema(description = "is_active") private Boolean isActive;
    @Schema(description = "创建时间") private Instant createdAt;
    @Schema(description = "更新时间") private Instant updatedAt;
}

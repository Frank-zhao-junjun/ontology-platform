package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建ErrorRecovery请求")
public class CreateErrorRecoveryRequest {
    @Schema(description = "action_id") private String actionId;
    @Schema(description = "error_pattern") private String errorPattern;
    @Schema(description = "recovery_strategy") private String recoveryStrategy;
    @Schema(description = "max_retries") private Integer maxRetries;
    @Schema(description = "fallback_action_id") private String fallbackActionId;
    @Schema(description = "description") private String description;
}

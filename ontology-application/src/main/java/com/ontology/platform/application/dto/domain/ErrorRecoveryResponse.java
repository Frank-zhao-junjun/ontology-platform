package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "ErrorRecovery响应")
public class ErrorRecoveryResponse {
    @Schema(description = "ID") private String id;
    @Schema(description = "action_id") private String actionId;
    @Schema(description = "error_pattern") private String errorPattern;
    @Schema(description = "recovery_strategy") private String recoveryStrategy;
    @Schema(description = "max_retries") private Integer maxRetries;
    @Schema(description = "fallback_action_id") private String fallbackActionId;
    @Schema(description = "description") private String description;
    @Schema(description = "创建时间") private Instant createdAt;
}

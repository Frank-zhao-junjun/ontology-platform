package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "AgentIntent响应")
public class AgentIntentResponse {
    @Schema(description = "ID") private String id;
    @Schema(description = "name") private String name;
    @Schema(description = "description") private String description;
    @Schema(description = "category") private String category;
    @Schema(description = "target_entity_id") private String targetEntityId;
    @Schema(description = "trigger_phrases") private String triggerPhrases;
    @Schema(description = "action_id") private String actionId;
    @Schema(description = "priority") private Integer priority;
    @Schema(description = "requires_confirmation") private Boolean requiresConfirmation;
    @Schema(description = "创建时间") private Instant createdAt;
    @Schema(description = "更新时间") private Instant updatedAt;
}

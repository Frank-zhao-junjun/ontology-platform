package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建AgentIntent请求")
public class CreateAgentIntentRequest {
    @Schema(description = "name") private String name;
    @Schema(description = "description") private String description;
    @Schema(description = "category: QUERY/CREATE/UPDATE/DELETE/ANALYZE/NAVIGATE/EXECUTE") private String category;
    @Schema(description = "target_entity_id") private String targetEntityId;
    @Schema(description = "trigger_phrases JSON array") private String triggerPhrases;
    @Schema(description = "action_id") private String actionId;
    @Schema(description = "priority") private Integer priority;
    @Schema(description = "requires_confirmation") private Boolean requiresConfirmation;
}

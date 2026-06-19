package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建IntentSlot请求")
public class CreateIntentSlotRequest {
    @Schema(description = "intent_id") private String intentId;
    @Schema(description = "name") private String name;
    @Schema(description = "slot_type") private String slotType;
    @Schema(description = "required") private Boolean required;
    @Schema(description = "examples") private String examples;
}

package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "IntentSlot响应")
public class IntentSlotResponse {
    @Schema(description = "ID") private String id;
    @Schema(description = "intent_id") private String intentId;
    @Schema(description = "name") private String name;
    @Schema(description = "slot_type") private String slotType;
    @Schema(description = "required") private Boolean required;
    @Schema(description = "examples") private String examples;
    @Schema(description = "创建时间") private Instant createdAt;
}

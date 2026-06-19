package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "ProcessStep响应")
public class ProcessStepResponse {
    @Schema(description = "ID") private String id;
    @Schema(description = "orchestration_id")
    private String orchestrationId;
    @Schema(description = "name")
    private String name;
    @Schema(description = "step_type")
    private String stepType;
    @Schema(description = "description")
    private String description;
    @Schema(description = "sort_order")
    private Integer sortOrder;
    @Schema(description = "config")
    private String config;
    @Schema(description = "创建时间") private Instant createdAt;
    @Schema(description = "更新时间") private Instant updatedAt;
}

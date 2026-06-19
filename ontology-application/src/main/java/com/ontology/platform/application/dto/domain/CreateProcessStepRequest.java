package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建ProcessStep请求")
public class CreateProcessStepRequest {
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
}

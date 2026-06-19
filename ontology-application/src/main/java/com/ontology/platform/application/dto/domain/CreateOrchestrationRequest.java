package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建Orchestration请求")
public class CreateOrchestrationRequest {
    @Schema(description = "name")
    private String name;
    @Schema(description = "description")
    private String description;
    @Schema(description = "entry_points")
    private String entryPoints;
}

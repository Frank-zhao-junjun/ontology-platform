package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建PositionEntry请求")
public class CreatePositionEntryRequest {
    @Schema(description = "name")
    private String name;
    @Schema(description = "name_en")
    private String nameEn;
    @Schema(description = "description")
    private String description;
    @Schema(description = "department_id")
    private String departmentId;
    @Schema(description = "responsibilities")
    private String responsibilities;
}

package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建BusinessMetric请求")
public class CreateBusinessMetricRequest {
    @Schema(description = "name")
    private String name;
    @Schema(description = "name_en")
    private String nameEn;
    @Schema(description = "description")
    private String description;
    @Schema(description = "formula")
    private String formula;
    @Schema(description = "data_source_ref")
    private String dataSourceRef;
    @Schema(description = "period")
    private String period;
    @Schema(description = "target_entity")
    private String targetEntity;
}

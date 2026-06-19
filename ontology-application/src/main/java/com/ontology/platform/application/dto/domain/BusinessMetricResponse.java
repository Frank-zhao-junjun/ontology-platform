package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "BusinessMetric响应")
public class BusinessMetricResponse {
    @Schema(description = "ID") private String id;
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
    @Schema(description = "创建时间") private Instant createdAt;
    @Schema(description = "更新时间") private Instant updatedAt;
}

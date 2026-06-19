package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "SemanticFieldMapping响应")
public class SemanticFieldMappingResponse {
    @Schema(description = "ID") private String id;
    @Schema(description = "entity_id") private String entityId;
    @Schema(description = "field_name_en") private String fieldNameEn;
    @Schema(description = "business_term_id") private String businessTermId;
    @Schema(description = "mapping_type") private String mappingType;
    @Schema(description = "transform_rule") private String transformRule;
    @Schema(description = "创建时间") private Instant createdAt;
}

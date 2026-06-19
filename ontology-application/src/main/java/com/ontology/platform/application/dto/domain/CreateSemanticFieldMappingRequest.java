package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建SemanticFieldMapping请求")
public class CreateSemanticFieldMappingRequest {
    @Schema(description = "entity_id") private String entityId;
    @Schema(description = "field_name_en") private String fieldNameEn;
    @Schema(description = "business_term_id") private String businessTermId;
    @Schema(description = "mapping_type") private String mappingType;
    @Schema(description = "transform_rule") private String transformRule;
}

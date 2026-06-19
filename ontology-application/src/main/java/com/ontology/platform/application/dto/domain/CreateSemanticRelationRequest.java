package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建SemanticRelation请求")
public class CreateSemanticRelationRequest {
    @Schema(description = "source_term_id") private String sourceTermId;
    @Schema(description = "target_term_id") private String targetTermId;
    @Schema(description = "relation_type") private String relationType;
    @Schema(description = "description") private String description;
}

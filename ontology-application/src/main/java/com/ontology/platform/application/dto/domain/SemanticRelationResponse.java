package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "SemanticRelation响应")
public class SemanticRelationResponse {
    @Schema(description = "ID") private String id;
    @Schema(description = "source_term_id") private String sourceTermId;
    @Schema(description = "target_term_id") private String targetTermId;
    @Schema(description = "relation_type") private String relationType;
    @Schema(description = "description") private String description;
    @Schema(description = "创建时间") private Instant createdAt;
}

package com.ontology.platform.domain.dto.semantic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemanticRelationResult {
    private String id;
    private String sourceTermId;
    private String targetTermId;
    private String relationType;
    private String description;
}

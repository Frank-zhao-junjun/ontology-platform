package com.ontology.platform.domain.dto.semantic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemanticLayerResponse {
    private String ontologyId;
    @Builder.Default
    private List<IntentResult> intents = new ArrayList<>();
    @Builder.Default
    private List<BusinessTermResult> businessTerms = new ArrayList<>();
    @Builder.Default
    private List<SemanticRelationResult> semanticRelations = new ArrayList<>();
}

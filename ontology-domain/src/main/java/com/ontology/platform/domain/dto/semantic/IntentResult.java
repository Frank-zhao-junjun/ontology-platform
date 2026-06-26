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
public class IntentResult {
    private String id;
    private String name;
    private String description;
    private String category;
    private String targetEntityId;
    private List<String> triggerPhrases;
    private String actionId;
    @Builder.Default
    private List<IntentSlotResult> slots = new ArrayList<>();
    private int matchScore;
    /** How the match was derived (trigger_phrase, business_term, semantic_relation, intent_name) */
    @Builder.Default
    private List<String> derivationChain = new ArrayList<>();
}

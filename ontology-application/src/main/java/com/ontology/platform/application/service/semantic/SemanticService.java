package com.ontology.platform.application.service.semantic;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.dto.semantic.*;
import com.ontology.platform.infrastructure.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticService {

    private final AgentIntentPOMapper agentIntentMapper;
    private final IntentSlotPOMapper intentSlotMapper;
    private final BusinessTermPOMapper businessTermMapper;
    private final SemanticRelationPOMapper semanticRelationMapper;
    private final ObjectMapper objectMapper;

    public IntentResult resolveIntent(String ontologyId, String phrase) {
        if (phrase == null || phrase.isBlank()) {
            return null;
        }
        List<AgentIntentPO> intents = loadIntents(ontologyId);
        if (intents.isEmpty()) {
            return null;
        }

        String trimmedPhrase = phrase.trim().toLowerCase();
        IntentResult bestMatch = null;
        int bestMatchScore = 0;

        for (AgentIntentPO po : intents) {
            List<String> triggerPhrases = parseStringList(po.getTriggerPhrases());
            for (String trigger : triggerPhrases) {
                if (trigger == null || trigger.isBlank()) {
                    continue;
                }
                int score = matchScore(trimmedPhrase, trigger.trim().toLowerCase());
                if (score > bestMatchScore) {
                    bestMatchScore = score;
                    bestMatch = toIntentResult(po, triggerPhrases, bestMatchScore);
                }
            }
        }

        if (bestMatch != null) {
            log.info("Resolved intent: {} (actionId={}) for phrase={}, ontologyId={}",
                    bestMatch.getId(), bestMatch.getActionId(), phrase, ontologyId);
        }
        return bestMatch;
    }

    public SemanticLayerResponse getSemanticLayer(String ontologyId) {
        SemanticLayerResponse response = SemanticLayerResponse.builder()
                .ontologyId(ontologyId)
                .build();

        List<AgentIntentPO> intents = loadIntents(ontologyId);
        response.setIntents(intents.stream()
                .map(po -> toIntentResult(po, parseStringList(po.getTriggerPhrases()), 0))
                .collect(Collectors.toList()));

        List<BusinessTermPO> terms = businessTermMapper.selectByOntologyId(ontologyId);
        response.setBusinessTerms(terms.stream().map(this::toBusinessTermResult).collect(Collectors.toList()));

        Set<String> termIds = terms.stream().map(BusinessTermPO::getId).collect(Collectors.toSet());
        response.setSemanticRelations(loadRelationsForTerms(termIds));

        return response;
    }

    private List<AgentIntentPO> loadIntents(String ontologyId) {
        if (ontologyId != null && !ontologyId.isBlank()) {
            List<AgentIntentPO> scoped = agentIntentMapper.selectByOntologyId(ontologyId);
            if (scoped != null && !scoped.isEmpty()) {
                return scoped;
            }
        }
        List<AgentIntentPO> all = agentIntentMapper.selectList(null);
        return all != null ? all : List.of();
    }

    private List<SemanticRelationResult> loadRelationsForTerms(Set<String> termIds) {
        if (termIds.isEmpty()) {
            return List.of();
        }
        List<SemanticRelationPO> all = semanticRelationMapper.selectList(null);
        if (all == null) {
            return List.of();
        }
        return all.stream()
                .filter(rel -> termIds.contains(rel.getSourceTermId()) || termIds.contains(rel.getTargetTermId()))
                .map(this::toSemanticRelationResult)
                .collect(Collectors.toList());
    }

    private IntentResult toIntentResult(AgentIntentPO po, List<String> triggerPhrases, int score) {
        return IntentResult.builder()
                .id(po.getId())
                .name(po.getName())
                .description(po.getDescription())
                .category(po.getCategory())
                .targetEntityId(po.getTargetEntityId())
                .triggerPhrases(triggerPhrases)
                .actionId(po.getActionId())
                .slots(loadSlots(po.getId()))
                .matchScore(score)
                .build();
    }

    private List<IntentSlotResult> loadSlots(String intentId) {
        List<IntentSlotPO> slots = intentSlotMapper.selectList(
                new QueryWrapper<IntentSlotPO>().eq("intent_id", intentId));
        if (slots == null) {
            return List.of();
        }
        return slots.stream().map(po -> IntentSlotResult.builder()
                .id(po.getId())
                .name(po.getName())
                .slotType(po.getSlotType())
                .required(po.getRequired())
                .examples(parseStringList(po.getExamples()))
                .build()).collect(Collectors.toList());
    }

    private BusinessTermResult toBusinessTermResult(BusinessTermPO po) {
        return BusinessTermResult.builder()
                .id(po.getId())
                .name(po.getName())
                .nameEn(po.getNameEn())
                .definition(po.getDefinition())
                .synonyms(parseStringList(po.getSynonyms()))
                .build();
    }

    private SemanticRelationResult toSemanticRelationResult(SemanticRelationPO po) {
        return SemanticRelationResult.builder()
                .id(po.getId())
                .sourceTermId(po.getSourceTermId())
                .targetTermId(po.getTargetTermId())
                .relationType(po.getRelationType())
                .description(po.getDescription())
                .build();
    }

    private int matchScore(String phrase, String trigger) {
        if (phrase.equals(trigger)) {
            return 10;
        }
        if (phrase.contains(trigger)) {
            return 5;
        }
        if (trigger.contains(phrase)) {
            return 3;
        }
        return 0;
    }

    private List<String> parseStringList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON list: {}", json);
            return new ArrayList<>();
        }
    }
}

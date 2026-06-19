package com.ontology.platform.application.service.semantic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.dto.semantic.IntentResult;
import com.ontology.platform.infrastructure.persistence.AgentIntentPO;
import com.ontology.platform.infrastructure.persistence.AgentIntentPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for semantic operations including intent resolution.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticService {

    private final AgentIntentPOMapper agentIntentMapper;
    private final ObjectMapper objectMapper;

    /**
     * Resolve an intent by matching the input phrase against stored intents' triggerPhrases.
     * Uses simple string matching (contains/equals) for triggerPhrase matching. No NLP involved.
     *
     * @param phrase the user input phrase (e.g. "安排生产")
     * @return the best matched IntentResult, or null if no match found
     */
    public IntentResult resolveIntent(String phrase) {
        if (phrase == null || phrase.isBlank()) {
            log.debug("resolveIntent called with null or blank phrase");
            return null;
        }

        String trimmedPhrase = phrase.trim().toLowerCase();

        // Load all intents
        List<AgentIntentPO> allIntents = agentIntentMapper.selectList(null);
        if (allIntents == null || allIntents.isEmpty()) {
            log.debug("No intents found in database");
            return null;
        }

        IntentResult bestMatch = null;
        int bestMatchScore = 0;

        for (AgentIntentPO po : allIntents) {
            List<String> triggerPhrases = parseTriggerPhrases(po.getTriggerPhrases());
            if (triggerPhrases == null || triggerPhrases.isEmpty()) {
                continue;
            }

            for (String trigger : triggerPhrases) {
                if (trigger == null || trigger.isBlank()) {
                    continue;
                }
                String trimmedTrigger = trigger.trim().toLowerCase();

                int score = matchScore(trimmedPhrase, trimmedTrigger);
                if (score > bestMatchScore) {
                    bestMatchScore = score;
                    bestMatch = IntentResult.builder()
                            .id(po.getId())
                            .name(po.getName())
                            .description(po.getDescription())
                            .triggerPhrases(triggerPhrases)
                            .actionId(po.getActionId())
                            .build();
                }
            }
        }

        if (bestMatch == null) {
            log.debug("No matching intent found for phrase: {}", phrase);
        } else {
            log.info("Resolved intent: {} (id={}) for phrase: {}, score={}",
                    bestMatch.getName(), bestMatch.getId(), phrase, bestMatchScore);
        }

        return bestMatch;
    }

    /**
     * Calculate a match score between the input phrase and a trigger phrase.
     * - Exact match: 10 points
     * - Contains match: 5 points
     * - Phrase starts with trigger: 3 points
     * - Otherwise: 0
     */
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

    /**
     * Parse JSONB trigger_phrases string into a list of strings.
     */
    private List<String> parseTriggerPhrases(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse trigger_phrases JSON: {}", json, e);
            return new ArrayList<>();
        }
    }
}

package com.ontology.platform.application.service.exchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.infrastructure.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Persists Phase 3c semantic layer models on exchange publish.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExchangePhase3cPublisher {

    private final AgentIntentPOMapper agentIntentMapper;
    private final IntentSlotPOMapper intentSlotMapper;
    private final BusinessTermPOMapper businessTermMapper;
    private final SemanticRelationPOMapper semanticRelationMapper;
    private final AgentPolicySemanticPOMapper agentPolicySemanticMapper;
    private final ErrorRecoveryPOMapper errorRecoveryMapper;
    private final SemanticFieldMappingPOMapper semanticFieldMappingMapper;
    private final ObjectMapper objectMapper;

    public Map<String, Integer> publish(String ontologyId, OntologyExchangeDocument doc, String rawJson) {
        Map<String, Integer> counts = new HashMap<>();
        OntologyExchangeDocument.AgentSemanticLayer layer = extractLayer(doc, rawJson);
        if (layer == null) {
            return counts;
        }

        String effectiveOntologyId = resolveOntologyId(ontologyId, doc);
        counts.put("intents", persistIntents(effectiveOntologyId, layer.getIntents()));
        counts.put("intentSlots", persistIntentSlotsFromLayer(layer.getIntents()));
        counts.put("businessTerms", persistBusinessTerms(effectiveOntologyId, layer.getBusinessTerms()));
        counts.put("semanticRelations", persistSemanticRelations(layer.getSemanticRelations()));
        counts.put("agentPolicies", persistAgentPolicies(layer.getAgentPolicies()));
        counts.put("errorRecoveries", persistErrorRecoveries(layer.getErrorRecoveries()));
        counts.put("fieldMappings", persistFieldMappings(layer.getFieldMappings()));

        log.info("Phase 3c publish complete: ontologyId={}, counts={}", effectiveOntologyId, counts);
        return counts;
    }

    private String resolveOntologyId(String ontologyId, OntologyExchangeDocument doc) {
        if (ontologyId != null && !ontologyId.isBlank()) {
            return ontologyId;
        }
        if (doc != null && doc.getMetadata() != null && doc.getMetadata().getId() != null) {
            return doc.getMetadata().getId();
        }
        if (doc != null && doc.getSpec() != null && doc.getSpec().getProject() != null) {
            return doc.getSpec().getProject().getId();
        }
        return ontologyId;
    }

    private OntologyExchangeDocument.AgentSemanticLayer extractLayer(
            OntologyExchangeDocument doc, String rawJson) {
        if (doc != null && doc.getSpec() != null && doc.getSpec().getProject() != null) {
            OntologyExchangeDocument.AgentSemanticLayer layer =
                    doc.getSpec().getProject().getAgentSemanticLayer();
            if (layer != null && hasSemanticContent(layer)) {
                return layer;
            }
        }
        if (rawJson == null || rawJson.isBlank()) {
            return null;
        }
        try {
            JsonNode layerNode = objectMapper.readTree(rawJson)
                    .path("spec").path("project").path("agentSemanticLayer");
            if (layerNode.isMissingNode() || layerNode.isNull()) {
                return null;
            }
            return objectMapper.treeToValue(layerNode, OntologyExchangeDocument.AgentSemanticLayer.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to extract agentSemanticLayer from raw JSON: {}", e.getMessage());
            return null;
        }
    }

    private boolean hasSemanticContent(OntologyExchangeDocument.AgentSemanticLayer layer) {
        return (layer.getIntents() != null && !layer.getIntents().isEmpty())
                || (layer.getBusinessTerms() != null && !layer.getBusinessTerms().isEmpty())
                || (layer.getSemanticRelations() != null && !layer.getSemanticRelations().isEmpty());
    }

    private int persistIntents(String ontologyId, List<OntologyExchangeDocument.Intent> intents) {
        if (intents == null || intents.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (OntologyExchangeDocument.Intent intent : intents) {
            if (intent.getId() == null || intent.getId().isBlank()) {
                continue;
            }
            Instant now = Instant.now();
            agentIntentMapper.insert(AgentIntentPO.builder()
                    .id(intent.getId())
                    .ontologyId(ontologyId)
                    .name(intent.getName())
                    .description(intent.getDescription())
                    .triggerPhrases(toJson(intent.getTriggerPhrases()))
                    .actionId(intent.getActionId())
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
            count++;
        }
        return count;
    }

    private int persistIntentSlotsFromLayer(List<OntologyExchangeDocument.Intent> intents) {
        if (intents == null || intents.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (OntologyExchangeDocument.Intent intent : intents) {
            count += persistIntentSlots(intent);
        }
        return count;
    }

    private int persistIntentSlots(OntologyExchangeDocument.Intent intent) {
        if (intent.getSlotFilling() == null || intent.getSlotFilling().getSlots() == null) {
            return 0;
        }
        Set<String> required = new HashSet<>(
                intent.getSlotFilling().getRequiredSlots() != null
                        ? intent.getSlotFilling().getRequiredSlots() : List.of());
        int count = 0;
        for (OntologyExchangeDocument.IntentSlot slot : intent.getSlotFilling().getSlots()) {
            if (slot.getId() == null || slot.getId().isBlank()) {
                continue;
            }
            String slotName = slot.getDisplayName() != null ? slot.getDisplayName()
                    : (slot.getName() != null ? slot.getName() : slot.getParamName());
            intentSlotMapper.insert(IntentSlotPO.builder()
                    .id(slot.getId())
                    .intentId(intent.getId())
                    .name(slotName)
                    .slotType(slot.getSlotType() != null ? slot.getSlotType() : "string")
                    .required(required.contains(slot.getId()) || Boolean.TRUE.equals(slot.getRequired()))
                    .examples(toJson(slot.getExamples()))
                    .createdAt(Instant.now())
                    .build());
            count++;
        }
        return count;
    }

    private int persistBusinessTerms(String ontologyId,
                                     List<OntologyExchangeDocument.BusinessTerm> terms) {
        if (terms == null || terms.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (OntologyExchangeDocument.BusinessTerm term : terms) {
            if (term.getId() == null || term.getId().isBlank()) {
                continue;
            }
            Instant now = Instant.now();
            businessTermMapper.insert(BusinessTermPO.builder()
                    .id(term.getId())
                    .ontologyId(ontologyId)
                    .name(term.getName())
                    .nameEn(term.getNameEn())
                    .definition(term.getDefinition())
                    .synonyms(toJson(term.getSynonyms()))
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
            count++;
        }
        return count;
    }

    private int persistSemanticRelations(List<OntologyExchangeDocument.SemanticRelation> relations) {
        if (relations == null || relations.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (OntologyExchangeDocument.SemanticRelation rel : relations) {
            if (rel.getSourceTermId() == null || rel.getTargetTermId() == null) {
                continue;
            }
            semanticRelationMapper.insert(SemanticRelationPO.builder()
                    .id(rel.getId())
                    .sourceTermId(rel.getSourceTermId())
                    .targetTermId(rel.getTargetTermId())
                    .relationType(rel.getRelationType())
                    .description(rel.getDescription())
                    .createdAt(Instant.now())
                    .build());
            count++;
        }
        return count;
    }

    private int persistAgentPolicies(List<OntologyExchangeDocument.SemanticAgentPolicy> policies) {
        if (policies == null || policies.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (OntologyExchangeDocument.SemanticAgentPolicy policy : policies) {
            Instant now = Instant.now();
            agentPolicySemanticMapper.insert(AgentPolicySemanticPO.builder()
                    .id(policy.getId())
                    .name(policy.getId())
                    .roleId(policy.getRoleId())
                    .intentPatterns("[]")
                    .allowActions(toJson(policy.getAllowedMcpTools()))
                    .denyActions("[]")
                    .requireConfirm("[]")
                    .isActive(policy.getDefaultDeny() == null || !policy.getDefaultDeny())
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
            count++;
        }
        return count;
    }

    private int persistErrorRecoveries(List<OntologyExchangeDocument.SemanticErrorRecovery> recoveries) {
        if (recoveries == null || recoveries.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (OntologyExchangeDocument.SemanticErrorRecovery er : recoveries) {
            if (er.getActionId() == null || er.getRecoveryStrategy() == null) {
                continue;
            }
            errorRecoveryMapper.insert(ErrorRecoveryPO.builder()
                    .id(er.getId())
                    .actionId(er.getActionId())
                    .errorPattern(er.getErrorPattern())
                    .recoveryStrategy(er.getRecoveryStrategy())
                    .maxRetries(er.getMaxRetries())
                    .fallbackActionId(er.getFallbackActionId())
                    .description(er.getDescription())
                    .createdAt(Instant.now())
                    .build());
            count++;
        }
        return count;
    }

    private int persistFieldMappings(List<OntologyExchangeDocument.SemanticFieldMapping> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (OntologyExchangeDocument.SemanticFieldMapping mapping : mappings) {
            if (mapping.getEntityId() == null) {
                continue;
            }
            semanticFieldMappingMapper.insert(SemanticFieldMappingPO.builder()
                    .id(mapping.getId())
                    .entityId(mapping.getEntityId())
                    .fieldNameEn(mapping.getFieldNameEn())
                    .businessTermId(mapping.getBusinessTermId())
                    .mappingType(mapping.getMappingType() != null ? mapping.getMappingType() : "direct")
                    .transformRule(mapping.getTransformRule())
                    .createdAt(Instant.now())
                    .build());
            count++;
        }
        return count;
    }

    private String toJson(Object value) {
        if (value == null) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}

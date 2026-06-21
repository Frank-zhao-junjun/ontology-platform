package com.ontology.platform.application.service.exchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.infrastructure.persistence.EntityLifecycleSnapshotPO;
import com.ontology.platform.infrastructure.persistence.EntityLifecycleSnapshotPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Persists entity lifecycle snapshots on exchange publish (Phase 3c).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExchangePhase3cLifecyclePublisher {

    private final EntityLifecycleSnapshotPOMapper lifecycleMapper;
    private final ObjectMapper objectMapper;

    public Map<String, Integer> publish(String ontologyId, OntologyExchangeDocument doc, String rawJson) {
        Map<String, Integer> counts = new HashMap<>();
        if (doc == null && (rawJson == null || rawJson.isBlank())) {
            return counts;
        }

        String effectiveOntologyId = resolveOntologyId(ontologyId, doc);
        Map<String, OntologyExchangeDocument.EntityLifecycleEntry> entries = extractEntries(doc, rawJson);
        if (entries.isEmpty()) {
            return counts;
        }

        int count = 0;
        for (var entry : entries.entrySet()) {
            String entityId = entry.getKey();
            OntologyExchangeDocument.EntityLifecycleEntry lifecycle = entry.getValue();
            if (entityId == null || entityId.isBlank()) {
                continue;
            }
            lifecycleMapper.insert(EntityLifecycleSnapshotPO.builder()
                    .id(entityId + "@" + effectiveOntologyId)
                    .entityId(entityId)
                    .ontologyId(effectiveOntologyId)
                    .lifecycleData(toJson(lifecycle))
                    .snapshotVersion("1.0")
                    .createdAt(Instant.now())
                    .build());
            count++;
        }

        counts.put("lifecycleSnapshots", count);
        log.info("Phase 3c lifecycle publish complete: ontologyId={}, count={}", effectiveOntologyId, count);
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

    private Map<String, OntologyExchangeDocument.EntityLifecycleEntry> extractEntries(
            OntologyExchangeDocument doc, String rawJson) {
        if (doc != null && doc.getSpec() != null && doc.getSpec().getLifecycle() != null
                && doc.getSpec().getLifecycle().getByEntityId() != null
                && !doc.getSpec().getLifecycle().getByEntityId().isEmpty()) {
            return doc.getSpec().getLifecycle().getByEntityId();
        }
        if (doc != null && doc.getSpec() != null && doc.getSpec().getProject() != null) {
            return compileFromBehaviorModel(doc.getSpec().getProject());
        }
        if (rawJson == null || rawJson.isBlank()) {
            return Map.of();
        }
        try {
            JsonNode lifecycleNode = objectMapper.readTree(rawJson).path("spec").path("lifecycle");
            if (lifecycleNode.isMissingNode() || lifecycleNode.isNull()) {
                return Map.of();
            }
            OntologyExchangeDocument.LifecycleSpec spec =
                    objectMapper.treeToValue(lifecycleNode, OntologyExchangeDocument.LifecycleSpec.class);
            return spec != null && spec.getByEntityId() != null ? spec.getByEntityId() : Map.of();
        } catch (JsonProcessingException e) {
            log.warn("Failed to extract lifecycle from raw JSON: {}", e.getMessage());
            return Map.of();
        }
    }

    private Map<String, OntologyExchangeDocument.EntityLifecycleEntry> compileFromBehaviorModel(
            OntologyExchangeDocument.OntologyProject project) {
        if (project.getBehaviorModel() == null
                || project.getBehaviorModel().getStateMachines() == null) {
            return Map.of();
        }

        Map<String, String> entityNameEn = new HashMap<>();
        if (project.getDataModel() != null && project.getDataModel().getEntities() != null) {
            project.getDataModel().getEntities().forEach(e -> {
                if (e.getId() != null) {
                    entityNameEn.put(e.getId(), e.getNameEn());
                }
            });
        }

        List<OntologyExchangeDocument.Action> actions = project.getBehaviorModel().getActions() != null
                ? project.getBehaviorModel().getActions() : List.of();

        Map<String, OntologyExchangeDocument.EntityLifecycleEntry> result = new HashMap<>();
        for (OntologyExchangeDocument.StateMachine sm : project.getBehaviorModel().getStateMachines()) {
            if (sm.getEntity() == null || sm.getEntity().isBlank()) {
                continue;
            }
            String entityId = sm.getEntity();
            List<OntologyExchangeDocument.Action> entityActions = actions.stream()
                    .filter(a -> entityId.equals(a.getTargetEntityId()))
                    .collect(Collectors.toList());

            result.put(entityId, OntologyExchangeDocument.EntityLifecycleEntry.builder()
                    .entityId(entityId)
                    .entityNameEn(entityNameEn.get(entityId))
                    .statusField(sm.getStatusField())
                    .stateMachine(sm)
                    .actionsByState(Map.of("_all", entityActions))
                    .build());
        }
        return result;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}

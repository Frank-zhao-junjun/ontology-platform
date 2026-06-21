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
 * Persists Phase 3d EPC graph models on exchange publish.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExchangePhase3dPublisher {

    private final EpcChainPOMapper epcChainMapper;
    private final EpcNodePOMapper epcNodeMapper;
    private final EpcEdgePOMapper epcEdgeMapper;
    private final EpcModelRefPOMapper epcModelRefMapper;
    private final EpcProfilePOMapper epcProfileMapper;
    private final ObjectMapper objectMapper;

    public Map<String, Integer> publish(String ontologyId, OntologyExchangeDocument doc, String rawJson) {
        Map<String, Integer> counts = new HashMap<>();
        OntologyExchangeDocument.EpcModel epcModel = extractEpcModel(doc, rawJson);
        if (epcModel == null || !hasEpcContent(epcModel)) {
            return counts;
        }

        String effectiveOntologyId = resolveOntologyId(ontologyId, doc);
        int chains = 0;
        int nodes = 0;
        int edges = 0;
        int modelRefs = 0;
        int profiles = 0;

        if (epcModel.getChains() != null) {
            for (OntologyExchangeDocument.EpcChain chain : epcModel.getChains()) {
                if (chain.getId() == null || chain.getId().isBlank()) {
                    continue;
                }
                Instant now = Instant.now();
                epcChainMapper.insert(EpcChainPO.builder()
                        .id(chain.getId())
                        .ontologyId(effectiveOntologyId)
                        .name(chain.getName() != null ? chain.getName() : chain.getId())
                        .aggregateRootId(chain.getAggregateRootId())
                        .description(chain.getDescription())
                        .chainType(chain.getChainType() != null ? chain.getChainType() : "production")
                        .isActive(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build());
                chains++;

                Set<String> seenRefs = new HashSet<>();
                if (chain.getNodes() != null) {
                    for (OntologyExchangeDocument.EpcNode node : chain.getNodes()) {
                        if (node.getId() == null || node.getId().isBlank()) {
                            continue;
                        }
                        epcNodeMapper.insert(EpcNodePO.builder()
                                .id(node.getId())
                                .chainId(chain.getId())
                                .nodeType(node.getNodeType() != null ? node.getNodeType() : "function")
                                .name(node.getName() != null ? node.getName() : node.getId())
                                .description(node.getDescription())
                                .refType(node.getRefType())
                                .refId(node.getRefId())
                                .sortOrder(node.getSortOrder() != null ? node.getSortOrder() : 0)
                                .createdAt(now)
                                .build());
                        nodes++;
                        modelRefs += persistModelRef(chain.getId(), node, seenRefs, now);
                    }
                }

                if (chain.getEdges() != null) {
                    for (OntologyExchangeDocument.EpcEdge edge : chain.getEdges()) {
                        if (edge.getId() == null || edge.getId().isBlank()) {
                            continue;
                        }
                        epcEdgeMapper.insert(EpcEdgePO.builder()
                                .id(edge.getId())
                                .chainId(chain.getId())
                                .sourceNodeId(edge.getSourceNodeId())
                                .targetNodeId(edge.getTargetNodeId())
                                .edgeType(edge.getEdgeType() != null ? edge.getEdgeType() : "control_flow")
                                .label(edge.getLabel())
                                .conditionExpr(edge.getConditionExpr())
                                .sortOrder(edge.getSortOrder() != null ? edge.getSortOrder() : 0)
                                .createdAt(now)
                                .build());
                        edges++;
                    }
                }
            }
        }

        if (epcModel.getProfiles() != null) {
            for (OntologyExchangeDocument.EpcProfile profile : epcModel.getProfiles()) {
                if (profile.getId() == null || profile.getId().isBlank()) {
                    continue;
                }
                epcProfileMapper.insert(EpcProfilePO.builder()
                        .id(profile.getId())
                        .chainId(profile.getChainId())
                        .profileData(profile.getProfileData() != null ? profile.getProfileData() : "{}")
                        .profileVersion(profile.getProfileVersion() != null ? profile.getProfileVersion() : "1.0")
                        .isActive(true)
                        .createdAt(Instant.now())
                        .build());
                profiles++;
            }
        }

        counts.put("chains", chains);
        counts.put("nodes", nodes);
        counts.put("edges", edges);
        counts.put("modelRefs", modelRefs);
        counts.put("profiles", profiles);
        log.info("Phase 3d publish complete: ontologyId={}, counts={}", effectiveOntologyId, counts);
        return counts;
    }

    private int persistModelRef(String chainId, OntologyExchangeDocument.EpcNode node,
                                Set<String> seenRefs, Instant now) {
        if (node.getRefType() == null || node.getRefType().isBlank()
                || node.getRefId() == null || node.getRefId().isBlank()) {
            return 0;
        }
        String key = node.getRefType() + ":" + node.getRefId();
        if (!seenRefs.add(key)) {
            return 0;
        }
        String refId = chainId + ":" + key;
        epcModelRefMapper.insert(EpcModelRefPO.builder()
                .id(refId)
                .chainId(chainId)
                .modelType(node.getRefType())
                .modelId(node.getRefId())
                .refMetadata("{}")
                .createdAt(now)
                .build());
        return 1;
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

    private OntologyExchangeDocument.EpcModel extractEpcModel(
            OntologyExchangeDocument doc, String rawJson) {
        if (doc != null && doc.getSpec() != null && doc.getSpec().getProject() != null) {
            OntologyExchangeDocument.EpcModel model = doc.getSpec().getProject().getEpcModel();
            if (model != null && hasEpcContent(model)) {
                return model;
            }
        }
        if (rawJson == null || rawJson.isBlank()) {
            return null;
        }
        try {
            JsonNode modelNode = objectMapper.readTree(rawJson)
                    .path("spec").path("project").path("epcModel");
            if (modelNode.isMissingNode() || modelNode.isNull()) {
                return null;
            }
            return objectMapper.treeToValue(modelNode, OntologyExchangeDocument.EpcModel.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to extract epcModel from raw JSON: {}", e.getMessage());
            return null;
        }
    }

    private boolean hasEpcContent(OntologyExchangeDocument.EpcModel model) {
        return (model.getChains() != null && !model.getChains().isEmpty())
                || (model.getProfiles() != null && !model.getProfiles().isEmpty());
    }
}

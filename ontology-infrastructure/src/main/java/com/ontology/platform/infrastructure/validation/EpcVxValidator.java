package com.ontology.platform.infrastructure.validation;

import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.service.validation.ValidationContext;
import com.ontology.platform.domain.service.validation.ValidationIssue;
import com.ontology.platform.domain.service.validation.ValidationPlugin;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * EPC Cross Validator — VX-01..15 for Phase 3d epcModel graph cross-references.
 */
@Component
public class EpcVxValidator implements ValidationPlugin {

    private static final Set<String> VALID_REF_TYPES = Set.of(
            "state", "action", "event", "entity", "rule");

    @Override
    public String pluginCode() {
        return "VX";
    }

    @Override
    public String pluginName() {
        return "EPC Cross Validator";
    }

    @Override
    public List<ValidationIssue> validate(ValidationContext ctx) {
        List<ValidationIssue> issues = new ArrayList<>();
        OntologyExchangeDocument doc = ctx.getDocument();
        if (doc == null || doc.getSpec() == null || doc.getSpec().getProject() == null) {
            return issues;
        }

        var project = doc.getSpec().getProject();
        OntologyExchangeDocument.EpcModel epcModel = project.getEpcModel();
        if (epcModel == null || epcModel.getChains() == null || epcModel.getChains().isEmpty()) {
            return issues;
        }

        Set<String> entityIds = collectEntityIds(project);
        Set<String> stateIds = collectStateIds(project);
        Set<String> actionIds = collectActionIds(project);
        Set<String> eventIds = collectEventIds(project);
        Set<String> ruleIds = collectRuleIds(project);
        Set<String> chainIds = new HashSet<>();

        for (OntologyExchangeDocument.EpcChain chain : epcModel.getChains()) {
            validateChain(chain, entityIds, stateIds, actionIds, eventIds, ruleIds, chainIds, issues);
        }

        if (epcModel.getProfiles() != null) {
            for (OntologyExchangeDocument.EpcProfile profile : epcModel.getProfiles()) {
                if (profile.getChainId() != null && !profile.getChainId().isBlank()
                        && !chainIds.contains(profile.getChainId())) {
                    issues.add(issue("VX-15", "error", "epcProfile", profile.getId(), "chainId",
                            "epcProfile chainId must reference an existing epcChain: " + profile.getChainId()));
                }
            }
        }

        return issues;
    }

    private void validateChain(OntologyExchangeDocument.EpcChain chain,
                               Set<String> entityIds,
                               Set<String> stateIds,
                               Set<String> actionIds,
                               Set<String> eventIds,
                               Set<String> ruleIds,
                               Set<String> chainIds,
                               List<ValidationIssue> issues) {
        if (chain.getId() == null || chain.getId().isBlank()) {
            issues.add(issue("VX-01", "error", "epcChain", null, "id",
                    "epcChain id must not be empty"));
            return;
        }
        if (!chainIds.add(chain.getId())) {
            issues.add(issue("VX-15", "error", "epcChain", chain.getId(), "id",
                    "Duplicate epcChain id: " + chain.getId()));
        }

        if (chain.getName() == null || chain.getName().isBlank()) {
            issues.add(issue("VX-02", "warning", "epcChain", chain.getId(), "name",
                    "epcChain name should not be empty"));
        }
        if (chain.getAggregateRootId() == null || chain.getAggregateRootId().isBlank()) {
            issues.add(issue("VX-03", "error", "epcChain", chain.getId(), "aggregateRootId",
                    "epcChain aggregateRootId must not be empty"));
        } else if (!entityIds.contains(chain.getAggregateRootId())) {
            issues.add(issue("VX-04", "error", "epcChain", chain.getId(), "aggregateRootId",
                    "aggregateRootId must reference an existing entity: " + chain.getAggregateRootId()));
        }

        if (chain.getNodes() == null || chain.getNodes().isEmpty()) {
            issues.add(issue("VX-05", "error", "epcChain", chain.getId(), "nodes",
                    "epcChain must have at least one node"));
            return;
        }

        Set<String> nodeIds = new HashSet<>();
        for (OntologyExchangeDocument.EpcNode node : chain.getNodes()) {
            validateNode(node, chain.getId(), entityIds, stateIds, actionIds, eventIds, ruleIds, nodeIds, issues);
        }

        Set<String> connectedNodes = new HashSet<>();
        if (chain.getEdges() != null) {
            for (OntologyExchangeDocument.EpcEdge edge : chain.getEdges()) {
                validateEdge(edge, chain.getId(), nodeIds, connectedNodes, issues);
            }
        }

        if (chain.getEdges() != null && !chain.getEdges().isEmpty()) {
            for (String nodeId : nodeIds) {
                if (!connectedNodes.contains(nodeId)) {
                    issues.add(issue("VX-14", "warning", "epcNode", nodeId, "id",
                            "epcNode is not connected by any edge in chain " + chain.getId()));
                }
            }
        }
    }

    private void validateNode(OntologyExchangeDocument.EpcNode node,
                              String chainId,
                              Set<String> entityIds,
                              Set<String> stateIds,
                              Set<String> actionIds,
                              Set<String> eventIds,
                              Set<String> ruleIds,
                              Set<String> nodeIds,
                              List<ValidationIssue> issues) {
        if (node.getId() == null || node.getId().isBlank()) {
            issues.add(issue("VX-06", "error", "epcNode", null, "id",
                    "epcNode id must not be empty"));
            return;
        }
        if (!nodeIds.add(node.getId())) {
            issues.add(issue("VX-10", "error", "epcNode", node.getId(), "id",
                    "Duplicate epcNode id in chain " + chainId + ": " + node.getId()));
        }

        boolean hasRefType = node.getRefType() != null && !node.getRefType().isBlank();
        boolean hasRefId = node.getRefId() != null && !node.getRefId().isBlank();
        if (hasRefType != hasRefId) {
            issues.add(issue("VX-07", "error", "epcNode", node.getId(), "refType",
                    "epcNode refType and refId must both be set or both be empty"));
            return;
        }
        if (!hasRefType) {
            return;
        }

        if (!VALID_REF_TYPES.contains(node.getRefType())) {
            issues.add(issue("VX-08", "error", "epcNode", node.getId(), "refType",
                    "Invalid refType: " + node.getRefType() + " (allowed: state, action, event, entity, rule)"));
            return;
        }

        if (!refExists(node.getRefType(), node.getRefId(), entityIds, stateIds, actionIds, eventIds, ruleIds)) {
            issues.add(issue("VX-09", "error", "epcNode", node.getId(), "refId",
                    "refId not found in " + node.getRefType() + " model: " + node.getRefId()));
        }
    }

    private void validateEdge(OntologyExchangeDocument.EpcEdge edge,
                              String chainId,
                              Set<String> nodeIds,
                              Set<String> connectedNodes,
                              List<ValidationIssue> issues) {
        if (edge.getId() == null || edge.getId().isBlank()) {
            issues.add(issue("VX-11", "error", "epcEdge", null, "id",
                    "epcEdge id must not be empty"));
        }
        if (edge.getSourceNodeId() != null && !nodeIds.contains(edge.getSourceNodeId())) {
            issues.add(issue("VX-12", "error", "epcEdge", edge.getId(), "sourceNodeId",
                    "sourceNodeId not found in chain " + chainId + ": " + edge.getSourceNodeId()));
        } else if (edge.getSourceNodeId() != null) {
            connectedNodes.add(edge.getSourceNodeId());
        }
        if (edge.getTargetNodeId() != null && !nodeIds.contains(edge.getTargetNodeId())) {
            issues.add(issue("VX-13", "error", "epcEdge", edge.getId(), "targetNodeId",
                    "targetNodeId not found in chain " + chainId + ": " + edge.getTargetNodeId()));
        } else if (edge.getTargetNodeId() != null) {
            connectedNodes.add(edge.getTargetNodeId());
        }
    }

    private boolean refExists(String refType, String refId,
                              Set<String> entityIds,
                              Set<String> stateIds,
                              Set<String> actionIds,
                              Set<String> eventIds,
                              Set<String> ruleIds) {
        return switch (refType) {
            case "entity" -> entityIds.contains(refId);
            case "state" -> stateIds.contains(refId);
            case "action" -> actionIds.contains(refId);
            case "event" -> eventIds.contains(refId);
            case "rule" -> ruleIds.contains(refId);
            default -> false;
        };
    }

    private Set<String> collectEntityIds(OntologyExchangeDocument.OntologyProject project) {
        Set<String> ids = new HashSet<>();
        if (project.getDataModel() != null && project.getDataModel().getEntities() != null) {
            project.getDataModel().getEntities().forEach(e -> {
                if (e.getId() != null) {
                    ids.add(e.getId());
                }
            });
        }
        return ids;
    }

    private Set<String> collectStateIds(OntologyExchangeDocument.OntologyProject project) {
        Set<String> ids = new HashSet<>();
        if (project.getBehaviorModel() != null && project.getBehaviorModel().getStateMachines() != null) {
            for (var sm : project.getBehaviorModel().getStateMachines()) {
                if (sm.getStates() != null) {
                    sm.getStates().forEach(s -> {
                        if (s.getId() != null) {
                            ids.add(s.getId());
                        }
                    });
                }
            }
        }
        return ids;
    }

    private Set<String> collectActionIds(OntologyExchangeDocument.OntologyProject project) {
        Set<String> ids = new HashSet<>();
        if (project.getBehaviorModel() != null && project.getBehaviorModel().getActions() != null) {
            project.getBehaviorModel().getActions().forEach(a -> {
                if (a.getId() != null) {
                    ids.add(a.getId());
                }
            });
        }
        return ids;
    }

    private Set<String> collectEventIds(OntologyExchangeDocument.OntologyProject project) {
        Set<String> ids = new HashSet<>();
        if (project.getEventModel() != null && project.getEventModel().getEvents() != null) {
            project.getEventModel().getEvents().forEach(e -> {
                if (e.getId() != null) {
                    ids.add(e.getId());
                }
            });
        }
        return ids;
    }

    private Set<String> collectRuleIds(OntologyExchangeDocument.OntologyProject project) {
        Set<String> ids = new HashSet<>();
        if (project.getRuleModel() != null && project.getRuleModel().getRules() != null) {
            project.getRuleModel().getRules().forEach(r -> {
                if (r.getId() != null) {
                    ids.add(r.getId());
                }
            });
        }
        return ids;
    }

    private ValidationIssue issue(String code, String severity, String elementType,
                                  String elementId, String field, String message) {
        return ValidationIssue.builder()
                .code(code)
                .severity(severity)
                .elementType(elementType)
                .elementId(elementId)
                .field(field)
                .message(message)
                .build();
    }
}

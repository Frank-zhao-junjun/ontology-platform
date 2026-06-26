package com.ontology.platform.infrastructure.validation;

import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument.*;
import com.ontology.platform.domain.service.validation.ValidationContext;
import com.ontology.platform.domain.service.validation.ValidationIssue;
import com.ontology.platform.domain.service.validation.ValidationPlugin;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * EPC Event Validator — VE-01..17 rules.
 *
 * <p>Validates entity structure and EPC chain/node/edge integrity in the v2 exchange document.</p>
 */
@Component
public class EpcVeValidator implements ValidationPlugin {

    @Override
    public String pluginCode() { return "VE"; }

    @Override
    public String pluginName() { return "EPC Event Validator"; }

    @Override
    public List<ValidationIssue> validate(ValidationContext ctx) {
        List<ValidationIssue> issues = new ArrayList<>();
        OntologyExchangeDocument doc = ctx.getDocument();
        if (doc == null || doc.getSpec() == null || doc.getSpec().getProject() == null) {
            issues.add(ValidationIssue.builder()
                    .code("VE-00").severity("error").elementType("document")
                    .message("Document, spec, or project is null").build());
            return issues;
        }

        var project = doc.getSpec().getProject();
        var dataModel = project.getDataModel();
        if (dataModel == null || dataModel.getEntities() == null) return issues;

        // Build lookup sets for cross-reference validation
        Set<String> entityIds = dataModel.getEntities().stream()
                .map(Entity::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> actionIds = collectActionIds(project);
        Set<String> stateMachineIds = collectStateMachineIds(project);
        Set<String> eventDefIds = collectEventDefIds(project);
        Set<String> ruleIds = collectRuleIds(project);

        for (var entity : dataModel.getEntities()) {
            validateEntity(issues, entity);
        }

        // EPC chain-level validations (VE-10..17)
        EpcModel epcModel = project.getEpcModel();
        if (epcModel != null && epcModel.getChains() != null) {
            for (EpcChain chain : epcModel.getChains()) {
                validateChain(issues, chain, entityIds, actionIds, eventDefIds, ruleIds, stateMachineIds, project);
            }
        }

        return issues;
    }

    // ═══════════════════════════════════════════════════════════════
    // Entity-level rules (VE-01..06)
    // ═══════════════════════════════════════════════════════════════

    private void validateEntity(List<ValidationIssue> issues, Entity entity) {
        // VE-01: Entity ID must not be empty
        if (entity.getId() == null || entity.getId().isBlank()) {
            issues.add(ValidationIssue.builder()
                    .code("VE-01").severity("error").elementType("entity")
                    .message("Entity ID must not be empty").build());
            return;
        }

        // VE-02: Entity name must not be empty
        if (entity.getName() == null || entity.getName().isBlank()) {
            issues.add(ValidationIssue.builder()
                    .code("VE-02").severity("error").elementType("entity")
                    .elementId(entity.getId()).field("name")
                    .message("Entity name must not be empty").build());
        }

        // VE-03: Child entity must specify parentAggregateId
        if ("child_entity".equals(entity.getEntityRole()) && entity.getParentAggregateId() == null) {
            issues.add(ValidationIssue.builder()
                    .code("VE-03").severity("error").elementType("entity")
                    .elementId(entity.getId()).field("parentAggregateId")
                    .message("Child entity must specify parentAggregateId").build());
        }

        // VE-04: Business scenario should be set
        if (entity.getBusinessScenarioId() == null || entity.getBusinessScenarioId().isBlank()) {
            issues.add(ValidationIssue.builder()
                    .code("VE-04").severity("warning").elementType("entity")
                    .elementId(entity.getId()).field("businessScenarioId")
                    .message("Entity should belong to a business scenario").build());
        }

        // Attributes validation (VE-05)
        if (entity.getAttributes() != null) {
            for (var attr : entity.getAttributes()) {
                if (attr.getId() == null || attr.getId().isBlank()) {
                    issues.add(ValidationIssue.builder()
                            .code("VE-05").severity("error").elementType("attribute")
                            .elementId(entity.getId()).message("Attribute ID must not be empty").build());
                }
                if (attr.getName() == null || attr.getName().isBlank()) {
                    issues.add(ValidationIssue.builder()
                            .code("VE-05").severity("error").elementType("attribute")
                            .elementId(entity.getId()).message("Attribute name must not be empty").build());
                }
            }
        }

        // Relations validation (VE-06)
        if (entity.getRelations() != null) {
            for (var rel : entity.getRelations()) {
                if (rel.getTargetEntity() == null || rel.getTargetEntity().isBlank()) {
                    issues.add(ValidationIssue.builder()
                            .code("VE-06").severity("error").elementType("relation")
                            .elementId(entity.getId()).field("targetEntity")
                            .message("Relation targetEntity must not be empty").build());
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // EPC Chain-level rules (VE-07..17)
    // ═══════════════════════════════════════════════════════════════

    private void validateChain(List<ValidationIssue> issues, EpcChain chain, Set<String> entityIds,
                                Set<String> actionIds, Set<String> eventDefIds, Set<String> ruleIds,
                                Set<String> stateMachineIds, OntologyProject project) {
        String chainId = chain.getId() != null ? chain.getId() : "(unknown)";

        if (chain.getNodes() == null || chain.getNodes().isEmpty()) {
            return; // VX-05 handles missing nodes
        }

        List<EpcNode> nodes = chain.getNodes();
        List<EpcEdge> edges = chain.getEdges() != null ? chain.getEdges() : Collections.emptyList();

        // Build node id lookup
        Set<String> nodeIds = nodes.stream().map(EpcNode::getId).filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // First and last nodes
        EpcNode firstNode = nodes.get(0);
        EpcNode lastNode = nodes.get(nodes.size() - 1);

        // VE-10: Chain must start and end with Event node
        if (!"event".equalsIgnoreCase(firstNode.getNodeType())) {
            issues.add(ValidationIssue.builder()
                    .code("VE-10").severity("error").elementType("epcChain")
                    .elementId(chainId).field("nodes[0].nodeType")
                    .message("EPC chain must start with an Event node, got: " + firstNode.getNodeType()).build());
        }
        if (!"event".equalsIgnoreCase(lastNode.getNodeType())) {
            issues.add(ValidationIssue.builder()
                    .code("VE-10").severity("error").elementType("epcChain")
                    .elementId(chainId).field("nodes[last].nodeType")
                    .message("EPC chain must end with an Event node, got: " + lastNode.getNodeType()).build());
        }

        // VE-11: Event/Function alternation (connectors excepted)
        validateAlternation(issues, chainId, nodes);

        // VE-12: Connector branching rules
        validateConnectorBranches(issues, chainId, nodes, edges, nodeIds);

        // Per-node validations
        for (EpcNode node : nodes) {
            String nodeType = node.getNodeType() != null ? node.getNodeType().toLowerCase() : "";
            String nodeId = node.getId();

            switch (nodeType) {
                case "function":
                    // VE-07: Function should reference at least 1 data model element
                    if (node.getRefType() == null || node.getRefId() == null) {
                        issues.add(ValidationIssue.builder()
                                .code("VE-07").severity("warning").elementType("epcNode")
                                .elementId(nodeId).field("refType/refId")
                                .message("Function node should reference at least one data model element (InfoObject/Attribute)").build());
                    }
                    // VE-15: State-Action consistency
                    if ("action".equalsIgnoreCase(node.getRefType()) && node.getRefId() != null) {
                        validateStateActionConsistency(issues, node, project);
                    }
                    break;

                case "info_object":
                case "infoobject":
                    // VE-08: InfoObject should bind to an Entity
                    if (node.getRefId() != null && !entityIds.contains(node.getRefId())) {
                        issues.add(ValidationIssue.builder()
                                .code("VE-08").severity("warning").elementType("epcNode")
                                .elementId(nodeId).field("refId")
                                .message("InfoObject references unknown entity: " + node.getRefId()).build());
                    }
                    break;

                case "org_unit":
                case "orgunit":
                    // VE-09: OrgUnit should bind to a GovernanceRole
                    if (node.getRefId() != null && !isGovernanceRole(project, node.getRefId())) {
                        issues.add(ValidationIssue.builder()
                                .code("VE-09").severity("warning").elementType("epcNode")
                                .elementId(nodeId).field("refId")
                                .message("OrgUnit should reference a GovernanceRole, got: " + node.getRefId()).build());
                    }
                    break;

                case "event":
                    // VE-16: Transition-Event consistency
                    if ("event".equalsIgnoreCase(node.getRefType()) && node.getRefId() != null) {
                        validateTransitionEventConsistency(issues, node, project, actionIds);
                    }
                    break;
            }

            // VE-13: Lifecycle ref existence
            if ("lifecycle".equalsIgnoreCase(node.getRefType()) && node.getRefId() != null) {
                if (!entityIds.contains(node.getRefId()) && !stateMachineIds.contains(node.getRefId())) {
                    issues.add(ValidationIssue.builder()
                            .code("VE-13").severity("error").elementType("epcNode")
                            .elementId(nodeId).field("refId")
                            .message("Lifecycle reference not found: " + node.getRefId()).build());
                }
            }

            // VE-14: Semantic ref existence
            if ("semantic".equalsIgnoreCase(node.getRefType()) && node.getRefId() != null) {
                if (!isSemanticElement(project, node.getRefId())) {
                    issues.add(ValidationIssue.builder()
                            .code("VE-14").severity("error").elementType("epcNode")
                            .elementId(nodeId).field("refId")
                            .message("Semantic reference not found: " + node.getRefId()).build());
                }
            }
        }

        // VE-17: Connector guard condition executability
        for (EpcEdge edge : edges) {
            if ("connector".equalsIgnoreCase(edge.getEdgeType()) && edge.getConditionExpr() != null
                    && edge.getConditionExpr().startsWith("rule:")) {
                String refRuleId = edge.getConditionExpr().substring(5);
                if (!ruleIds.contains(refRuleId)) {
                    issues.add(ValidationIssue.builder()
                            .code("VE-17").severity("warning").elementType("epcEdge")
                            .elementId(edge.getId()).field("conditionExpr")
                            .message("Guard condition references unknown rule: " + refRuleId).build());
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // VE-11: Event/Function alternation check
    // ═══════════════════════════════════════════════════════════════

    private void validateAlternation(List<ValidationIssue> issues, String chainId, List<EpcNode> nodes) {
        String prevType = null;
        for (int i = 0; i < nodes.size(); i++) {
            String nodeType = nodes.get(i).getNodeType();
            if (nodeType == null) continue;
            String lower = nodeType.toLowerCase();

            // Skip connectors from alternation check
            if ("connector".equals(lower)) continue;

            if (prevType != null) {
                // After Event should be Function, after Function should be Event
                if ("event".equals(prevType) && !"function".equals(lower)) {
                    issues.add(ValidationIssue.builder()
                            .code("VE-11").severity("warning").elementType("epcChain")
                            .elementId(chainId).field("nodes[" + i + "]")
                            .message("Expected Function after Event, got " + nodeType
                                    + " (Event/Function should alternate)").build());
                }
                if ("function".equals(prevType) && !"event".equals(lower) && !"info_object".equals(lower)
                        && !"org_unit".equals(lower)) {
                    issues.add(ValidationIssue.builder()
                            .code("VE-11").severity("warning").elementType("epcChain")
                            .elementId(chainId).field("nodes[" + i + "]")
                            .message("Expected Event after Function, got " + nodeType
                                    + " (Event/Function should alternate)").build());
                }
            }
            if (!"connector".equals(lower)) {
                prevType = lower;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // VE-12: Connector branching — XOR ≥2, AND branches must merge
    // ═══════════════════════════════════════════════════════════════

    private void validateConnectorBranches(List<ValidationIssue> issues, String chainId,
                                            List<EpcNode> nodes, List<EpcEdge> edges, Set<String> nodeIds) {
        for (EpcNode node : nodes) {
            if (!"connector".equalsIgnoreCase(node.getNodeType())) continue;
            if (node.getId() == null) continue;

            // Count outgoing edges
            long outCount = edges.stream()
                    .filter(e -> node.getId().equals(e.getSourceNodeId())).count();
            long inCount = edges.stream()
                    .filter(e -> node.getId().equals(e.getTargetNodeId())).count();

            String connType = node.getRefType() != null ? node.getRefType().toLowerCase() : "xor";

            if (inCount <= 1 && outCount <= 1) {
                continue; // single in/out, no branching issue
            }

            // XOR: outgoing > 1 → at least 2 branches
            if (("xor".equals(connType) || "or".equals(connType)) && outCount < 2) {
                issues.add(ValidationIssue.builder()
                        .code("VE-12").severity("error").elementType("epcNode")
                        .elementId(node.getId()).field("nodeType")
                        .message("XOR/OR connector must have at least 2 outgoing branches, has: " + outCount)
                        .build());
            }

            // AND: branches must merge back
            if ("and".equals(connType) && outCount > 1) {
                // Simple check: at least one merge point
                boolean hasMerge = false;
                for (EpcNode other : nodes) {
                    if ("connector".equalsIgnoreCase(other.getNodeType())
                            && ("xor".equalsIgnoreCase(other.getRefType()) || "or".equalsIgnoreCase(other.getRefType()))
                            && other.getId() != null) {
                        long otherIn = edges.stream()
                                .filter(e -> other.getId().equals(e.getTargetNodeId())).count();
                        if (otherIn > 1) { hasMerge = true; break; }
                    }
                }
                if (!hasMerge) {
                    issues.add(ValidationIssue.builder()
                            .code("VE-12").severity("warning").elementType("epcNode")
                            .elementId(node.getId()).field("nodeType")
                            .message("AND connector branches should merge at an XOR/OR connector").build());
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // VE-15: State-Action consistency
    // ═══════════════════════════════════════════════════════════════

    private void validateStateActionConsistency(List<ValidationIssue> issues, EpcNode node,
                                                 OntologyProject project) {
        BehaviorModel behavior = project.getBehaviorModel();
        if (behavior == null || behavior.getStateMachines() == null) return;

        for (StateMachine sm : behavior.getStateMachines()) {
            if (sm.getStates() == null) continue;
            for (State state : sm.getStates()) {
                if (state.getAvailableActions() != null
                        && state.getAvailableActions().contains(node.getRefId())) {
                    return; // Found a state where this action is available — consistent
                }
            }
        }
        // Action referenced by EPC Function but not in any state's availableActions
        issues.add(ValidationIssue.builder()
                .code("VE-15").severity("warning").elementType("epcNode")
                .elementId(node.getId()).field("refId")
                .message("Action '" + node.getRefId() + "' is not in any state's availableActions").build());
    }

    // ═══════════════════════════════════════════════════════════════
    // VE-16: Transition-Event consistency
    // ═══════════════════════════════════════════════════════════════

    private void validateTransitionEventConsistency(List<ValidationIssue> issues, EpcNode node,
                                                     OntologyProject project, Set<String> actionIds) {
        BehaviorModel behavior = project.getBehaviorModel();
        if (behavior == null || behavior.getStateMachines() == null) return;

        String eventId = node.getRefId();
        boolean foundPublish = false;
        for (StateMachine sm : behavior.getStateMachines()) {
            if (sm.getTransitions() == null) continue;
            for (Transition t : sm.getTransitions()) {
                if (eventId.equals(t.getPublishEventId())) {
                    foundPublish = true;
                    break;
                }
            }
        }
        if (!foundPublish) {
            issues.add(ValidationIssue.builder()
                    .code("VE-16").severity("warning").elementType("epcNode")
                    .elementId(node.getId()).field("refId")
                    .message("Event '" + eventId + "' is not published by any Transition.publishEventId").build());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Lookup helpers
    // ═══════════════════════════════════════════════════════════════

    private Set<String> collectActionIds(OntologyProject project) {
        if (project.getBehaviorModel() == null || project.getBehaviorModel().getActions() == null)
            return Collections.emptySet();
        return project.getBehaviorModel().getActions().stream()
                .map(Action::getId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private Set<String> collectStateMachineIds(OntologyProject project) {
        if (project.getBehaviorModel() == null || project.getBehaviorModel().getStateMachines() == null)
            return Collections.emptySet();
        return project.getBehaviorModel().getStateMachines().stream()
                .map(StateMachine::getId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private Set<String> collectEventDefIds(OntologyProject project) {
        if (project.getEventModel() == null || project.getEventModel().getEvents() == null)
            return Collections.emptySet();
        return project.getEventModel().getEvents().stream()
                .map(EventDefinition::getId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private Set<String> collectRuleIds(OntologyProject project) {
        if (project.getRuleModel() == null || project.getRuleModel().getRules() == null)
            return Collections.emptySet();
        return project.getRuleModel().getRules().stream()
                .map(Rule::getId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private boolean isGovernanceRole(OntologyProject project, String roleId) {
        if (project.getGovernanceModel() == null || project.getGovernanceModel().getRoles() == null)
            return false;
        return project.getGovernanceModel().getRoles().stream()
                .anyMatch(r -> roleId.equals(r.getId()));
    }

    private boolean isSemanticElement(OntologyProject project, String elementId) {
        if (project.getAgentSemanticLayer() == null) return false;
        var layer = project.getAgentSemanticLayer();
        return (layer.getIntents() != null && layer.getIntents().stream().anyMatch(i -> elementId.equals(i.getId())))
            || (layer.getBusinessTerms() != null && layer.getBusinessTerms().stream().anyMatch(t -> elementId.equals(t.getId())))
            || (layer.getSemanticRelations() != null && layer.getSemanticRelations().stream().anyMatch(r -> elementId.equals(r.getId())))
            || (layer.getErrorRecoveries() != null && layer.getErrorRecoveries().stream().anyMatch(e -> elementId.equals(e.getId())))
            || (layer.getFieldMappings() != null && layer.getFieldMappings().stream().anyMatch(f -> elementId.equals(f.getId())));
    }
}

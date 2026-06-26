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
 * Model Validator — VM-* rules (entity reference integrity + EPC coverage).
 *
 * <p>Two families:
 * <ol>
 *   <li><b>VM-01..05</b>: Cross-model reference integrity (entity/action/rule/event/transition refs)</li>
 *   <li><b>VM-D/B/R/E/P/G/M/S</b>: EPC coverage — each model element should appear in an EPC chain</li>
 * </ol>
 */
@Component
public class EpcVmValidator implements ValidationPlugin {

    @Override
    public String pluginCode() { return "VM"; }

    @Override
    public String pluginName() { return "Model Validator"; }

    @Override
    public List<ValidationIssue> validate(ValidationContext ctx) {
        List<ValidationIssue> issues = new ArrayList<>();
        var doc = ctx.getDocument();
        if (doc == null || doc.getSpec() == null || doc.getSpec().getProject() == null) return issues;

        var project = doc.getSpec().getProject();

        Set<String> entityIds = collectEntityIds(project);
        Set<String> aggregateRootIds = collectAggregateRootIds(project);

        // ── Core reference integrity (VM-01..05) ──
        validateReferenceIntegrity(issues, project, entityIds);

        // ── Extended model rules (VM-06..10) ──
        validateModelRules(issues, project, entityIds, aggregateRootIds);

        // ── EPC coverage (VM-D/B/R/E/P/G/M/S) ──
        EpcModel epcModel = project.getEpcModel();
        if (epcModel != null && epcModel.getChains() != null && !epcModel.getChains().isEmpty()) {
            Set<String> epcRefIds = collectEpcRefIds(epcModel);
            validateCoverage(issues, project, epcRefIds, entityIds, aggregateRootIds);
        }

        return issues;
    }

    // ═══════════════════════════════════════════════════════════════
    // Reference integrity (VM-01..05)
    // ═══════════════════════════════════════════════════════════════

    private void validateReferenceIntegrity(List<ValidationIssue> issues, OntologyProject project,
                                             Set<String> entityIds) {
        // VM-01: StateMachine.entity must reference a valid entity
        if (project.getBehaviorModel() != null && project.getBehaviorModel().getStateMachines() != null) {
            for (var sm : project.getBehaviorModel().getStateMachines()) {
                if (sm.getEntity() != null && !entityIds.contains(sm.getEntity())) {
                    issues.add(makeIssue("VM-01", "error", "stateMachine", sm.getId(), "entity",
                            "StateMachine references unknown entity: " + sm.getEntity()));
                }
            }
        }

        // VM-02: Action.targetEntityId must reference a valid entity
        if (project.getBehaviorModel() != null && project.getBehaviorModel().getActions() != null) {
            for (var action : project.getBehaviorModel().getActions()) {
                if (action.getTargetEntityId() != null && !entityIds.contains(action.getTargetEntityId())) {
                    issues.add(makeIssue("VM-02", "error", "action", action.getId(), "targetEntityId",
                            "Action references unknown entity: " + action.getTargetEntityId()));
                }
            }
        }

        // VM-03: Rule.entity must reference a valid entity
        if (project.getRuleModel() != null && project.getRuleModel().getRules() != null) {
            for (var rule : project.getRuleModel().getRules()) {
                if (rule.getEntity() != null && !entityIds.contains(rule.getEntity())) {
                    issues.add(makeIssue("VM-03", "error", "rule", rule.getId(), "entity",
                            "Rule references unknown entity: " + rule.getEntity()));
                }
            }
        }

        // VM-04: Event.entity must reference a valid entity
        if (project.getEventModel() != null && project.getEventModel().getEvents() != null) {
            for (var event : project.getEventModel().getEvents()) {
                if (event.getEntity() != null && !entityIds.contains(event.getEntity())) {
                    issues.add(makeIssue("VM-04", "error", "event", event.getId(), "entity",
                            "Event references unknown entity: " + event.getEntity()));
                }
            }
        }

        // VM-05: Transition from/to states must exist in the same StateMachine
        if (project.getBehaviorModel() != null && project.getBehaviorModel().getStateMachines() != null) {
            for (var sm : project.getBehaviorModel().getStateMachines()) {
                Set<String> stateIds = sm.getStates() != null
                        ? sm.getStates().stream().map(State::getId).filter(Objects::nonNull).collect(Collectors.toSet())
                        : Set.of();
                if (sm.getTransitions() != null) {
                    for (var trans : sm.getTransitions()) {
                        if (trans.getFrom() != null && !stateIds.contains(trans.getFrom())) {
                            issues.add(makeIssue("VM-05", "error", "transition", trans.getId(), "from",
                                    "Transition from state '" + trans.getFrom() + "' not found in StateMachine " + sm.getId()));
                        }
                        if (trans.getTo() != null && !stateIds.contains(trans.getTo())) {
                            issues.add(makeIssue("VM-05", "error", "transition", trans.getId(), "to",
                                    "Transition to state '" + trans.getTo() + "' not found in StateMachine " + sm.getId()));
                        }
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Extended model rules (VM-06..10)
    // ═══════════════════════════════════════════════════════════════

    private void validateModelRules(List<ValidationIssue> issues, OntologyProject project,
                                     Set<String> entityIds, Set<String> aggregateRootIds) {
        DataModel dm = project.getDataModel();
        BehaviorModel bm = project.getBehaviorModel();
        EventModel em = project.getEventModel();

        // VM-06: StateMachine statusField should reference an entity attribute
        if (bm != null && bm.getStateMachines() != null && dm != null && dm.getEntities() != null) {
            for (StateMachine sm : bm.getStateMachines()) {
                if (sm.getStatusField() == null || sm.getEntity() == null) continue;
                boolean fieldExists = dm.getEntities().stream()
                        .filter(e -> e.getId() != null && e.getId().equals(sm.getEntity()))
                        .flatMap(e -> e.getAttributes() != null ? e.getAttributes().stream() : java.util.stream.Stream.empty())
                        .anyMatch(attr -> sm.getStatusField().equals(attr.getName()));
                if (!fieldExists) {
                    issues.add(makeIssue("VM-06", "warning", "stateMachine", sm.getId(), "statusField",
                            "statusField '" + sm.getStatusField() + "' not found among entity attributes"));
                }
            }
        }

        // VM-07: Transition trigger must be one of valid values
        if (bm != null && bm.getStateMachines() != null) {
            Set<String> VALID_TRIGGERS = Set.of("manual", "automatic", "scheduled");
            for (StateMachine sm : bm.getStateMachines()) {
                if (sm.getTransitions() == null) continue;
                for (Transition t : sm.getTransitions()) {
                    String trigger = t.getTrigger();
                    if (trigger != null && !trigger.isBlank() && !VALID_TRIGGERS.contains(trigger.toLowerCase())) {
                        issues.add(makeIssue("VM-07", "warning", "transition", t.getId(), "trigger",
                                "Transition trigger '" + trigger + "' is not a recognized value (manual/automatic/scheduled)"));
                    }
                }
            }
        }

        // VM-08: Actions with the same name on the same entity signal potential ambiguity
        if (bm != null && bm.getActions() != null) {
            Map<String, List<Action>> byEntityAndName = new LinkedHashMap<>();
            for (Action action : bm.getActions()) {
                String key = (action.getTargetEntityId() != null ? action.getTargetEntityId() : "_global")
                        + "::" + (action.getName() != null ? action.getName().toLowerCase() : "");
                byEntityAndName.computeIfAbsent(key, k -> new ArrayList<>()).add(action);
            }
            for (Map.Entry<String, List<Action>> entry : byEntityAndName.entrySet()) {
                if (entry.getValue().size() > 1) {
                    for (Action dup : entry.getValue()) {
                        issues.add(makeIssue("VM-08", "info", "action", dup.getId(), "name",
                                "Multiple actions named '" + dup.getName() + "' target the same entity — potential ambiguity"));
                    }
                }
            }
        }

        // VM-09: Each aggregate_root entity should have at least one lifecycle state machine
        if (dm != null && dm.getEntities() != null) {
            Set<String> entitiesWithSM = new HashSet<>();
            if (bm != null && bm.getStateMachines() != null) {
                for (StateMachine sm : bm.getStateMachines()) {
                    if (sm.getEntity() != null) entitiesWithSM.add(sm.getEntity());
                }
            }
            for (var entity : dm.getEntities()) {
                if ("aggregate_root".equals(entity.getEntityRole())
                        && !entitiesWithSM.contains(entity.getId())) {
                    issues.add(makeIssue("VM-09", "warning", "entity", entity.getId(), "entityRole",
                            "Aggregate root entity has no lifecycle state machine"));
                }
            }
        }

        // VM-10: Event payload fields should reference entity attributes
        if (em != null && em.getEvents() != null && dm != null && dm.getEntities() != null) {
            for (EventDefinition event : em.getEvents()) {
                if (event.getEntity() == null || event.getPayloadFields() == null) continue;
                Set<String> attrNames = dm.getEntities().stream()
                        .filter(e -> event.getEntity().equals(e.getId()))
                        .flatMap(e -> e.getAttributes() != null ? e.getAttributes().stream() : java.util.stream.Stream.empty())
                        .map(Attribute::getName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                for (String field : event.getPayloadFields()) {
                    if (field != null && !field.isBlank() && !attrNames.contains(field)) {
                        issues.add(makeIssue("VM-10", "warning", "event", event.getId(), "payloadFields",
                                "Payload field '" + field + "' does not match any attribute on entity '" + event.getEntity() + "'"));
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // EPC Coverage rules (VM-D/B/R/E/P/G/M/S)
    // ═══════════════════════════════════════════════════════════════

    private void validateCoverage(List<ValidationIssue> issues, OntologyProject project,
                                   Set<String> epcRefIds, Set<String> entityIds, Set<String> aggregateRootIds) {

        // ── Data Model Coverage (VM-D01..03) ──
        // VM-D01: aggregate_root entities should appear in EPC
        if (project.getDataModel() != null && project.getDataModel().getEntities() != null) {
            for (var entity : project.getDataModel().getEntities()) {
                if (entity.getId() == null) continue;
                if ("aggregate_root".equals(entity.getEntityRole()) && !epcRefIds.contains(entity.getId())) {
                    issues.add(makeIssue("VM-D01", "warning", "entity", entity.getId(), null,
                            "Aggregate root not referenced in any EPC chain"));
                }
            }
        }

        // VM-D02: Key attributes (required+unique) coverage
        if (project.getDataModel() != null && project.getDataModel().getEntities() != null) {
            for (var entity : project.getDataModel().getEntities()) {
                if (entity.getAttributes() == null) continue;
                for (var attr : entity.getAttributes()) {
                    if (attr.getId() == null) continue;
                    Boolean required = attr.getRequired();
                    Boolean unique = attr.getUnique();
                    if (Boolean.TRUE.equals(required) || Boolean.TRUE.equals(unique)) {
                        if (!epcRefIds.contains(attr.getId())) {
                            issues.add(makeIssue("VM-D02", "info", "attribute", attr.getId(), null,
                                    "Key attribute '" + attr.getName() + "' (required/unique) not referenced in EPC"));
                        }
                    }
                }
            }
        }

        // VM-D03: Relations should appear in EPC
        if (project.getDataModel() != null && project.getDataModel().getEntities() != null) {
            for (var entity : project.getDataModel().getEntities()) {
                if (entity.getRelations() == null) continue;
                for (var rel : entity.getRelations()) {
                    if (rel.getId() != null && !epcRefIds.contains(rel.getId())) {
                        issues.add(makeIssue("VM-D03", "info", "relation", rel.getId(), null,
                                "Relation '" + rel.getName() + "' not referenced in any EPC chain"));
                    }
                }
            }
        }

        // ── Behavior Model Coverage (VM-B01..05) ──
        if (project.getBehaviorModel() != null) {
            // VM-B01: Actions should appear in EPC
            if (project.getBehaviorModel().getActions() != null) {
                for (var action : project.getBehaviorModel().getActions()) {
                    if (action.getId() != null && !epcRefIds.contains(action.getId())) {
                        issues.add(makeIssue("VM-B01", "warning", "action", action.getId(), null,
                                "Action not referenced in any EPC chain"));
                    }
                }
            }
            // VM-B02: StateMachines should have EPC representation
            if (project.getBehaviorModel().getStateMachines() != null) {
                for (var sm : project.getBehaviorModel().getStateMachines()) {
                    if (sm.getId() != null && !epcRefIds.contains(sm.getId())) {
                        issues.add(makeIssue("VM-B02", "warning", "stateMachine", sm.getId(), null,
                                "StateMachine has no EPC chain representing its lifecycle"));
                    }
                }
            }
            // VM-B03: Transitions should be referenced by EPC
            if (project.getBehaviorModel().getStateMachines() != null) {
                for (var sm : project.getBehaviorModel().getStateMachines()) {
                    if (sm.getTransitions() == null) continue;
                    for (var t : sm.getTransitions()) {
                        if (t.getId() != null && !epcRefIds.contains(t.getId())) {
                            issues.add(makeIssue("VM-B03", "info", "transition", t.getId(), null,
                                    "Transition not referenced in any EPC chain"));
                        }
                    }
                }
            }
        }

        // ── Rule Model Coverage (VM-R01..02) ──
        if (project.getRuleModel() != null && project.getRuleModel().getRules() != null) {
            for (var rule : project.getRuleModel().getRules()) {
                if (rule.getId() == null) continue;
                if (!epcRefIds.contains(rule.getId())) {
                    issues.add(makeIssue("VM-R01", "warning", "rule", rule.getId(), null,
                            "Rule not referenced in any EPC chain"));
                }
                // VM-R02: A rule referenced by EPC should bind to entity+field
                if (epcRefIds.contains(rule.getId())
                        && (rule.getEntity() == null || rule.getField() == null)) {
                    issues.add(makeIssue("VM-R02", "warning", "rule", rule.getId(), null,
                            "Rule referenced in EPC should bind to a concrete entity + field"));
                }
            }
        }

        // ── Event Model Coverage (VM-E01..02) ──
        if (project.getEventModel() != null && project.getEventModel().getEvents() != null) {
            for (var event : project.getEventModel().getEvents()) {
                if (event.getId() != null && !epcRefIds.contains(event.getId())) {
                    issues.add(makeIssue("VM-E01", "warning", "event", event.getId(), null,
                            "EventDefinition not referenced in any EPC chain"));
                }
            }
        }

        // ── Process Model Coverage (VM-P01..02) ──
        if (project.getProcessModel() != null && project.getProcessModel().getOrchestrations() != null) {
            for (var orch : project.getProcessModel().getOrchestrations()) {
                if (orch.getId() != null && !epcRefIds.contains(orch.getId())) {
                    issues.add(makeIssue("VM-P01", "warning", "orchestration", orch.getId(), null,
                            "Orchestration has no corresponding EPC chain"));
                }
                if (orch.getSteps() != null) {
                    for (var step : orch.getSteps()) {
                        if (step.getId() != null && !epcRefIds.contains(step.getId())) {
                            issues.add(makeIssue("VM-P02", "info", "processStep", step.getId(), null,
                                    "Process step not referenced in EPC"));
                        }
                    }
                }
            }
        }

        // ── Governance Model Coverage (VM-G01..02) ──
        if (project.getGovernanceModel() != null && project.getGovernanceModel().getRoles() != null) {
            for (var role : project.getGovernanceModel().getRoles()) {
                if (role.getId() != null && !epcRefIds.contains(role.getId())) {
                    issues.add(makeIssue("VM-G01", "warning", "governanceRole", role.getId(), null,
                            "GovernanceRole not referenced in any EPC OrgUnit"));
                }
            }
        }

        // ── Metrics Model Coverage (VM-M01..02) ──
        if (project.getMetricsModel() != null && project.getMetricsModel().getMetrics() != null) {
            for (var metric : project.getMetricsModel().getMetrics()) {
                if (metric.getId() == null) continue;
                if (!epcRefIds.contains(metric.getId())) {
                    issues.add(makeIssue("VM-M01", "warning", "businessMetric", metric.getId(), null,
                            "BusinessMetric not referenced in any EPC chain"));
                }
                if (epcRefIds.contains(metric.getId()) && metric.getDataSourceRef() == null) {
                    issues.add(makeIssue("VM-M02", "warning", "businessMetric", metric.getId(), null,
                            "Metric referenced in EPC lacks boundActionId or dataSourceRef"));
                }
            }
        }

        // ── DataSource Coverage (VM-S01..02) ──
        if (project.getDataSourcesModel() != null && project.getDataSourcesModel().getSources() != null) {
            for (var ds : project.getDataSourcesModel().getSources()) {
                if (ds.getId() == null) continue;
                if (!epcRefIds.contains(ds.getId())) {
                    issues.add(makeIssue("VM-S01", "warning", "dataSource", ds.getId(), null,
                            "DataSource not referenced in any EPC chain"));
                }
                if (epcRefIds.contains(ds.getId()) && ds.getBoundObjectTypeId() == null) {
                    issues.add(makeIssue("VM-S02", "warning", "dataSource", ds.getId(), null,
                            "DataSource referenced in EPC lacks boundObjectTypeId"));
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════

    private Set<String> collectEntityIds(OntologyProject project) {
        if (project.getDataModel() == null || project.getDataModel().getEntities() == null)
            return Set.of();
        return project.getDataModel().getEntities().stream()
                .map(Entity::getId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private Set<String> collectAggregateRootIds(OntologyProject project) {
        if (project.getDataModel() == null || project.getDataModel().getEntities() == null)
            return Set.of();
        return project.getDataModel().getEntities().stream()
                .filter(e -> "aggregate_root".equals(e.getEntityRole()))
                .map(Entity::getId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /** Collect all refIds from EPC chain nodes and edges. */
    private Set<String> collectEpcRefIds(EpcModel epcModel) {
        Set<String> ids = new HashSet<>();
        if (epcModel.getChains() == null) return ids;
        for (EpcChain chain : epcModel.getChains()) {
            if (chain.getNodes() != null) {
                for (EpcNode node : chain.getNodes()) {
                    if (node.getRefId() != null) ids.add(node.getRefId());
                    if (node.getId() != null) ids.add(node.getId());
                }
            }
            if (chain.getEdges() != null) {
                for (EpcEdge edge : chain.getEdges()) {
                    if (edge.getId() != null) ids.add(edge.getId());
                }
            }
        }
        return ids;
    }

    private ValidationIssue makeIssue(String code, String severity, String elementType,
                                       String elementId, String field, String message) {
        return ValidationIssue.builder()
                .code(code).severity(severity).elementType(elementType)
                .elementId(elementId).field(field).message(message).build();
    }
}

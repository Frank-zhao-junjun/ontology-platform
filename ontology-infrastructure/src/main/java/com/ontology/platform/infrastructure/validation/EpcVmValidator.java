package com.ontology.platform.infrastructure.validation;

import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.service.validation.ValidationContext;
import com.ontology.platform.domain.service.validation.ValidationIssue;
import com.ontology.platform.domain.service.validation.ValidationPlugin;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Model Validator — VM-*  rules.
 *
 * <p>Validates consistency between data model entities, behavior model,
 * rule model, and event model references.</p>
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

        // Collect valid entity IDs
        Set<String> entityIds = Set.of();
        if (project.getDataModel() != null && project.getDataModel().getEntities() != null) {
            entityIds = project.getDataModel().getEntities().stream()
                    .map(OntologyExchangeDocument.Entity::getId)
                    .collect(Collectors.toSet());
        }

        // VM-01: StateMachine.entity must reference a valid entity
        if (project.getBehaviorModel() != null && project.getBehaviorModel().getStateMachines() != null) {
            for (var sm : project.getBehaviorModel().getStateMachines()) {
                if (sm.getEntity() != null && !entityIds.contains(sm.getEntity())) {
                    issues.add(ValidationIssue.builder()
                            .code("VM-01").severity("error").elementType("stateMachine")
                            .elementId(sm.getId()).field("entity")
                            .message("StateMachine references unknown entity: " + sm.getEntity()).build());
                }
            }
        }

        // VM-02: Action.targetEntityId must reference a valid entity
        if (project.getBehaviorModel() != null && project.getBehaviorModel().getActions() != null) {
            for (var action : project.getBehaviorModel().getActions()) {
                if (action.getTargetEntityId() != null && !entityIds.contains(action.getTargetEntityId())) {
                    issues.add(ValidationIssue.builder()
                            .code("VM-02").severity("error").elementType("action")
                            .elementId(action.getId()).field("targetEntityId")
                            .message("Action references unknown entity: " + action.getTargetEntityId()).build());
                }
            }
        }

        // VM-03: Rule.entity must reference a valid entity
        if (project.getRuleModel() != null && project.getRuleModel().getRules() != null) {
            for (var rule : project.getRuleModel().getRules()) {
                if (rule.getEntity() != null && !entityIds.contains(rule.getEntity())) {
                    issues.add(ValidationIssue.builder()
                            .code("VM-03").severity("error").elementType("rule")
                            .elementId(rule.getId()).field("entity")
                            .message("Rule references unknown entity: " + rule.getEntity()).build());
                }
            }
        }

        // VM-04: Event.entity must reference a valid entity
        if (project.getEventModel() != null && project.getEventModel().getEvents() != null) {
            for (var event : project.getEventModel().getEvents()) {
                if (event.getEntity() != null && !entityIds.contains(event.getEntity())) {
                    issues.add(ValidationIssue.builder()
                            .code("VM-04").severity("error").elementType("event")
                            .elementId(event.getId()).field("entity")
                            .message("Event references unknown entity: " + event.getEntity()).build());
                }
            }
        }

        // VM-05: Transition from/to states must exist in the same StateMachine
        if (project.getBehaviorModel() != null && project.getBehaviorModel().getStateMachines() != null) {
            for (var sm : project.getBehaviorModel().getStateMachines()) {
                Set<String> stateIds = sm.getStates() != null
                        ? sm.getStates().stream().map(OntologyExchangeDocument.State::getId).collect(Collectors.toSet())
                        : Set.of();
                if (sm.getTransitions() != null) {
                    for (var trans : sm.getTransitions()) {
                        if (trans.getFrom() != null && !stateIds.contains(trans.getFrom())) {
                            issues.add(ValidationIssue.builder()
                                    .code("VM-05").severity("error").elementType("transition")
                                    .elementId(trans.getId()).field("from")
                                    .message("Transition from state '" + trans.getFrom() + "' not found in StateMachine " + sm.getId()).build());
                        }
                        if (trans.getTo() != null && !stateIds.contains(trans.getTo())) {
                            issues.add(ValidationIssue.builder()
                                    .code("VM-05").severity("error").elementType("transition")
                                    .elementId(trans.getId()).field("to")
                                    .message("Transition to state '" + trans.getTo() + "' not found in StateMachine " + sm.getId()).build());
                        }
                    }
                }
            }
        }

        return issues;
    }
}

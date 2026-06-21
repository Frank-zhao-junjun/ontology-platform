package com.ontology.platform.infrastructure.validation;

import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.service.validation.ValidationContext;
import com.ontology.platform.domain.service.validation.ValidationIssue;
import com.ontology.platform.domain.service.validation.ValidationPlugin;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lifecycle Validator — V-LC-01..15 for Phase 3c behaviorModel / spec.lifecycle.
 */
@Component
public class LifecycleValidator implements ValidationPlugin {

    @Override
    public String pluginCode() {
        return "V-LC";
    }

    @Override
    public String pluginName() {
        return "Lifecycle Validator";
    }

    @Override
    public List<ValidationIssue> validate(ValidationContext ctx) {
        List<ValidationIssue> issues = new ArrayList<>();
        OntologyExchangeDocument doc = ctx.getDocument();
        if (doc == null || doc.getSpec() == null || doc.getSpec().getProject() == null) {
            return issues;
        }

        var project = doc.getSpec().getProject();
        Set<String> entityIds = collectEntityIds(project);
        Set<String> ruleIds = collectRuleIds(project);
        Set<String> actionIds = collectActionIds(project);
        Set<String> eventIds = collectEventIds(project);
        Set<String> statusFields = collectStatusFields(project);

        if (project.getBehaviorModel() != null
                && project.getBehaviorModel().getStateMachines() != null) {
            for (var sm : project.getBehaviorModel().getStateMachines()) {
                validateStateMachine(sm, entityIds, ruleIds, actionIds, eventIds, statusFields, issues);
            }
        }

        if (project.getBehaviorModel() != null && project.getBehaviorModel().getActions() != null) {
            for (var action : project.getBehaviorModel().getActions()) {
                validateAction(action, entityIds, ruleIds, issues);
            }
        }

        if (doc.getSpec().getLifecycle() != null && doc.getSpec().getLifecycle().getByEntityId() != null) {
            validateLifecycleSpec(doc.getSpec().getLifecycle().getByEntityId(), entityIds, issues);
        }

        return issues;
    }

    private void validateStateMachine(OntologyExchangeDocument.StateMachine sm,
                                        Set<String> entityIds,
                                        Set<String> ruleIds,
                                        Set<String> actionIds,
                                        Set<String> eventIds,
                                        Set<String> statusFields,
                                        List<ValidationIssue> issues) {
        if (sm.getId() == null || sm.getId().isBlank()) {
            issues.add(issue("V-LC-01", "error", "stateMachine", null, "id",
                    "StateMachine id must not be empty"));
        }
        if (sm.getEntity() == null || sm.getEntity().isBlank()) {
            issues.add(issue("V-LC-02", "error", "stateMachine", sm.getId(), "entity",
                    "StateMachine entity must not be empty"));
        } else if (!entityIds.contains(sm.getEntity())) {
            issues.add(issue("V-LC-03", "error", "stateMachine", sm.getId(), "entity",
                    "StateMachine entity must reference an existing entity"));
        }

        if (sm.getStatusField() != null && !sm.getStatusField().isBlank()
                && !statusFields.contains(sm.getEntity() + ":" + sm.getStatusField())) {
            issues.add(issue("V-LC-14", "warning", "stateMachine", sm.getId(), "statusField",
                    "statusField not found on entity attributes"));
        }

        if (sm.getStates() == null || sm.getStates().isEmpty()) {
            issues.add(issue("V-LC-04", "error", "stateMachine", sm.getId(), "states",
                    "StateMachine must have at least one state"));
            return;
        }

        Set<String> stateIds = new HashSet<>();
        int initialCount = 0;
        boolean hasFinal = false;
        for (var state : sm.getStates()) {
            if (state.getId() == null || state.getId().isBlank()) {
                issues.add(issue("V-LC-06", "error", "state", null, "id",
                        "State id must not be empty"));
            } else {
                stateIds.add(state.getId());
            }
            if (Boolean.TRUE.equals(state.getIsInitial())) {
                initialCount++;
            }
            if (Boolean.TRUE.equals(state.getIsFinal())) {
                hasFinal = true;
            }
            if (state.getAvailableActions() != null) {
                for (String actionId : state.getAvailableActions()) {
                    if (actionId != null && !actionId.isBlank() && !actionIds.contains(actionId)) {
                        issues.add(issue("V-LC-09", "error", "state", state.getId(), "availableActions",
                                "availableAction not found in behaviorModel.actions: " + actionId));
                    }
                }
            }
        }

        if (initialCount != 1) {
            issues.add(issue("V-LC-05", "error", "stateMachine", sm.getId(), "states",
                    "StateMachine must have exactly one initial state, found " + initialCount));
        }
        if (!hasFinal) {
            issues.add(issue("V-LC-13", "warning", "stateMachine", sm.getId(), "states",
                    "StateMachine has no final state"));
        }

        if (sm.getTransitions() != null) {
            for (var tr : sm.getTransitions()) {
                if (tr.getId() == null || tr.getId().isBlank()) {
                    issues.add(issue("V-LC-08", "error", "transition", null, "id",
                            "Transition id must not be empty"));
                }
                if (tr.getFrom() != null && !stateIds.contains(tr.getFrom())) {
                    issues.add(issue("V-LC-07", "error", "transition", tr.getId(), "from",
                            "Transition from state not found: " + tr.getFrom()));
                }
                if (tr.getTo() != null && !stateIds.contains(tr.getTo())) {
                    issues.add(issue("V-LC-07", "error", "transition", tr.getId(), "to",
                            "Transition to state not found: " + tr.getTo()));
                }
                if (tr.getPreConditions() != null) {
                    for (String ruleId : tr.getPreConditions()) {
                        if (ruleId != null && !ruleId.isBlank() && !ruleIds.contains(ruleId)) {
                            issues.add(issue("V-LC-10", "error", "transition", tr.getId(), "preConditions",
                                    "preCondition rule not found: " + ruleId));
                        }
                    }
                }
                if (tr.getPublishEventId() != null && !tr.getPublishEventId().isBlank()
                        && !eventIds.contains(tr.getPublishEventId())) {
                    issues.add(issue("V-LC-12", "warning", "transition", tr.getId(), "publishEventId",
                            "publishEventId not found in eventModel.events"));
                }
            }
        }
    }

    private void validateAction(OntologyExchangeDocument.Action action,
                                Set<String> entityIds,
                                Set<String> ruleIds,
                                List<ValidationIssue> issues) {
        if (action.getTargetEntityId() != null
                && !action.getTargetEntityId().isBlank()
                && !entityIds.contains(action.getTargetEntityId())) {
            issues.add(issue("V-LC-11", "error", "action", action.getId(), "targetEntityId",
                    "Action targetEntityId must reference an existing entity"));
        }
        if (action.getPreConditions() != null) {
            for (String ruleId : action.getPreConditions()) {
                if (ruleId != null && !ruleId.isBlank() && !ruleIds.contains(ruleId)) {
                    issues.add(issue("V-LC-10", "error", "action", action.getId(), "preConditions",
                            "preCondition rule not found: " + ruleId));
                }
            }
        }
    }

    private void validateLifecycleSpec(Map<String, OntologyExchangeDocument.EntityLifecycleEntry> byEntityId,
                                       Set<String> entityIds,
                                       List<ValidationIssue> issues) {
        for (Map.Entry<String, OntologyExchangeDocument.EntityLifecycleEntry> entry : byEntityId.entrySet()) {
            String key = entry.getKey();
            if (key != null && !key.isBlank() && !entityIds.contains(key)) {
                issues.add(issue("V-LC-15", "error", "lifecycle", key, "byEntityId",
                        "Lifecycle entry entityId not found in dataModel.entities"));
            }
            OntologyExchangeDocument.EntityLifecycleEntry lifecycle = entry.getValue();
            if (lifecycle != null && lifecycle.getEntityId() != null
                    && !lifecycle.getEntityId().isBlank()
                    && !entityIds.contains(lifecycle.getEntityId())) {
                issues.add(issue("V-LC-15", "error", "lifecycle", lifecycle.getEntityId(), "entityId",
                        "Lifecycle entityId not found in dataModel.entities"));
            }
        }
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

    private Set<String> collectStatusFields(OntologyExchangeDocument.OntologyProject project) {
        Set<String> fields = new HashSet<>();
        if (project.getDataModel() == null || project.getDataModel().getEntities() == null) {
            return fields;
        }
        for (var entity : project.getDataModel().getEntities()) {
            if (entity.getId() == null || entity.getAttributes() == null) {
                continue;
            }
            entity.getAttributes().forEach(attr -> {
                if (attr.getNameEn() != null) {
                    fields.add(entity.getId() + ":" + attr.getNameEn());
                }
            });
        }
        return fields;
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

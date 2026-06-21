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
 * Semantic Validator — V-AS-01..15 for Phase 3c agentSemanticLayer.
 */
@Component
public class SemanticValidator implements ValidationPlugin {

    @Override
    public String pluginCode() {
        return "V-AS";
    }

    @Override
    public String pluginName() {
        return "Semantic Layer Validator";
    }

    @Override
    public List<ValidationIssue> validate(ValidationContext ctx) {
        List<ValidationIssue> issues = new ArrayList<>();
        OntologyExchangeDocument doc = ctx.getDocument();
        if (doc == null || doc.getSpec() == null || doc.getSpec().getProject() == null) {
            return issues;
        }

        var project = doc.getSpec().getProject();
        OntologyExchangeDocument.AgentSemanticLayer layer = project.getAgentSemanticLayer();
        if (layer == null) {
            return issues;
        }

        Set<String> entityIds = collectEntityIds(project);
        Set<String> actionIds = collectActionIds(project);
        Set<String> termIds = new HashSet<>();

        if (layer.getBusinessTerms() != null) {
            for (var term : layer.getBusinessTerms()) {
                if (term.getId() == null || term.getId().isBlank()) {
                    issues.add(issue("V-AS-10", "error", "businessTerm", null, "id",
                            "Business term id must not be empty"));
                } else {
                    termIds.add(term.getId());
                }
                if (term.getName() == null || term.getName().isBlank()) {
                    issues.add(issue("V-AS-11", "error", "businessTerm", term.getId(), "name",
                            "Business term name must not be empty"));
                }
            }
        }

        if (layer.getIntents() != null) {
            for (var intent : layer.getIntents()) {
                validateIntent(intent, entityIds, actionIds, issues);
            }
        }

        if (layer.getSemanticRelations() != null) {
            for (var rel : layer.getSemanticRelations()) {
                if (rel.getRelationType() == null || rel.getRelationType().isBlank()) {
                    issues.add(issue("V-AS-13", "error", "semanticRelation", rel.getId(), "relationType",
                            "Semantic relation type must not be empty"));
                }
                if (rel.getSourceTermId() != null && !termIds.contains(rel.getSourceTermId())) {
                    issues.add(issue("V-AS-12", "error", "semanticRelation", rel.getId(), "sourceTermId",
                            "Source term not found in businessTerms"));
                }
                if (rel.getTargetTermId() != null && !termIds.contains(rel.getTargetTermId())) {
                    issues.add(issue("V-AS-12", "error", "semanticRelation", rel.getId(), "targetTermId",
                            "Target term not found in businessTerms"));
                }
            }
        }

        if (layer.getErrorRecoveries() != null) {
            for (var er : layer.getErrorRecoveries()) {
                if (er.getActionId() != null && !actionIds.contains(er.getActionId())) {
                    issues.add(issue("V-AS-14", "warning", "errorRecovery", er.getId(), "actionId",
                            "Error recovery actionId not found in behaviorModel.actions"));
                }
            }
        }

        if (layer.getFieldMappings() != null) {
            for (var mapping : layer.getFieldMappings()) {
                if (mapping.getEntityId() != null && !entityIds.contains(mapping.getEntityId())) {
                    issues.add(issue("V-AS-15", "error", "fieldMapping", mapping.getId(), "entityId",
                            "Field mapping entityId not found in dataModel.entities"));
                }
            }
        }

        return issues;
    }

    private void validateIntent(OntologyExchangeDocument.Intent intent,
                                Set<String> entityIds,
                                Set<String> actionIds,
                                List<ValidationIssue> issues) {
        if (intent.getId() == null || intent.getId().isBlank()) {
            issues.add(issue("V-AS-01", "error", "intent", null, "id",
                    "Intent id must not be empty"));
            return;
        }
        if (intent.getName() == null || intent.getName().isBlank()) {
            issues.add(issue("V-AS-02", "error", "intent", intent.getId(), "name",
                    "Intent name must not be empty"));
        }
        if (intent.getActionId() == null || intent.getActionId().isBlank()) {
            issues.add(issue("V-AS-03", "error", "intent", intent.getId(), "actionId",
                    "Intent actionId must not be empty"));
        } else if (!actionIds.contains(intent.getActionId())) {
            issues.add(issue("V-AS-05", "error", "intent", intent.getId(), "actionId",
                    "Intent actionId must reference an existing action"));
        }
        if (intent.getTriggerPhrases() == null || intent.getTriggerPhrases().isEmpty()) {
            issues.add(issue("V-AS-04", "error", "intent", intent.getId(), "triggerPhrases",
                    "Intent must have at least one trigger phrase"));
        }
        if (intent.getTargetEntityId() != null
                && !intent.getTargetEntityId().isBlank()
                && !entityIds.contains(intent.getTargetEntityId())) {
            issues.add(issue("V-AS-06", "warning", "intent", intent.getId(), "targetEntityId",
                    "Intent targetEntityId not found in dataModel.entities"));
        }

        if (intent.getSlotFilling() == null || intent.getSlotFilling().getSlots() == null) {
            return;
        }

        Set<String> slotIds = new HashSet<>();
        for (var slot : intent.getSlotFilling().getSlots()) {
            if (slot.getId() == null || slot.getId().isBlank()) {
                issues.add(issue("V-AS-07", "error", "intentSlot", null, "id",
                        "Intent slot id must not be empty"));
            } else {
                slotIds.add(slot.getId());
            }
            boolean hasName = (slot.getDisplayName() != null && !slot.getDisplayName().isBlank())
                    || (slot.getName() != null && !slot.getName().isBlank())
                    || (slot.getParamName() != null && !slot.getParamName().isBlank());
            if (!hasName) {
                issues.add(issue("V-AS-08", "error", "intentSlot", slot.getId(), "displayName",
                        "Intent slot must have paramName, displayName, or name"));
            }
        }

        if (intent.getSlotFilling().getRequiredSlots() != null) {
            for (String requiredId : intent.getSlotFilling().getRequiredSlots()) {
                if (requiredId != null && !requiredId.isBlank() && !slotIds.contains(requiredId)) {
                    issues.add(issue("V-AS-09", "error", "intent", intent.getId(), "requiredSlots",
                            "Required slot id not found: " + requiredId));
                }
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

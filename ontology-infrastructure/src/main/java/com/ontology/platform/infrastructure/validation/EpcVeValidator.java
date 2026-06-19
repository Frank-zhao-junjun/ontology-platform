package com.ontology.platform.infrastructure.validation;

import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.service.validation.ValidationContext;
import com.ontology.platform.domain.service.validation.ValidationIssue;
import com.ontology.platform.domain.service.validation.ValidationPlugin;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * EPC Event Validator — VE-01..17 rules.
 *
 * <p>Validates entity structure consistency in the v2 exchange document.</p>
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

        for (var entity : dataModel.getEntities()) {
            // VE-01: Entity ID must not be empty
            if (entity.getId() == null || entity.getId().isBlank()) {
                issues.add(ValidationIssue.builder()
                        .code("VE-01").severity("error").elementType("entity")
                        .message("Entity ID must not be empty").build());
                continue;
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

            // VE-04: Business scenario must be set
            if (entity.getBusinessScenarioId() == null || entity.getBusinessScenarioId().isBlank()) {
                issues.add(ValidationIssue.builder()
                        .code("VE-04").severity("warning").elementType("entity")
                        .elementId(entity.getId()).field("businessScenarioId")
                        .message("Entity should belong to a business scenario").build());
            }

            // Attributes validation
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

            // Relations validation
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
        return issues;
    }
}

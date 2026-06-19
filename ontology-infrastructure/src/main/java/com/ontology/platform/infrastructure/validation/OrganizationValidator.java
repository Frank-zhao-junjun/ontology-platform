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
 * Organization Validator — VM-O / V-XL-O rules for Phase 3b.
 *
 * <p>Validates organizationModel, metricsModel structure in v2 exchange documents.</p>
 */
@Component
public class OrganizationValidator implements ValidationPlugin {

    @Override
    public String pluginCode() { return "VM-O"; }

    @Override
    public String pluginName() { return "Organization Validator"; }

    @Override
    public List<ValidationIssue> validate(ValidationContext ctx) {
        List<ValidationIssue> issues = new ArrayList<>();
        OntologyExchangeDocument doc = ctx.getDocument();
        if (doc == null || doc.getSpec() == null || doc.getSpec().getProject() == null) {
            return issues;
        }

        var project = doc.getSpec().getProject();
        validateOrganization(project.getOrganizationModel(), issues);
        validateMetrics(project.getMetricsModel(), issues);
        return issues;
    }

    private void validateOrganization(OntologyExchangeDocument.OrganizationModel org,
                                    List<ValidationIssue> issues) {
        if (org == null) return;

        Set<String> deptIds = new HashSet<>();
        if (org.getDepartments() != null) {
            for (var dept : org.getDepartments()) {
                // VM-O-01: department id required
                if (dept.getId() == null || dept.getId().isBlank()) {
                    issues.add(ValidationIssue.builder()
                            .code("VM-O-01").severity("error").elementType("department")
                            .message("Department id must not be empty").build());
                } else {
                    deptIds.add(dept.getId());
                }
                // VM-O-02: department name required
                if (dept.getName() == null || dept.getName().isBlank()) {
                    issues.add(ValidationIssue.builder()
                            .code("VM-O-02").severity("error").elementType("department")
                            .elementId(dept.getId()).field("name")
                            .message("Department name must not be empty").build());
                }
                // VM-O-03: parent must reference existing department
                if (dept.getParentDepartmentId() != null
                        && !dept.getParentDepartmentId().isBlank()
                        && !deptIds.contains(dept.getParentDepartmentId())) {
                    issues.add(ValidationIssue.builder()
                            .code("VM-O-03").severity("warning").elementType("department")
                            .elementId(dept.getId()).field("parentDepartmentId")
                            .message("Parent department not found in same document").build());
                }
            }
        }

        if (org.getPositions() != null) {
            for (var pos : org.getPositions()) {
                // VM-O-04: position id required
                if (pos.getId() == null || pos.getId().isBlank()) {
                    issues.add(ValidationIssue.builder()
                            .code("VM-O-04").severity("error").elementType("position")
                            .message("Position id must not be empty").build());
                }
                // VM-O-05: position departmentId must reference department
                if (pos.getDepartmentId() != null
                        && !pos.getDepartmentId().isBlank()
                        && !deptIds.contains(pos.getDepartmentId())) {
                    issues.add(ValidationIssue.builder()
                            .code("VM-O-05").severity("error").elementType("position")
                            .elementId(pos.getId()).field("departmentId")
                            .message("Position departmentId must reference an existing department").build());
                }
            }
        }
    }

    private void validateMetrics(OntologyExchangeDocument.MetricsModel metrics,
                                 List<ValidationIssue> issues) {
        if (metrics == null || metrics.getMetrics() == null) return;

        for (var metric : metrics.getMetrics()) {
            // V-XL-O-01: metric id required
            if (metric.getId() == null || metric.getId().isBlank()) {
                issues.add(ValidationIssue.builder()
                        .code("V-XL-O-01").severity("error").elementType("metric")
                        .message("Metric id must not be empty").build());
            }
            // V-XL-O-02: metric name required
            if (metric.getName() == null || metric.getName().isBlank()) {
                issues.add(ValidationIssue.builder()
                        .code("V-XL-O-02").severity("error").elementType("metric")
                        .elementId(metric.getId()).field("name")
                        .message("Metric name must not be empty").build());
            }
        }
    }
}

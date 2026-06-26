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
 * Organization Validator — VM-O / V-XL-O / VM-HR rules.
 *
 * <p>Validates organization model, metrics model, and HR sync rules
 * in v2 exchange documents.</p>
 */
@Component
public class OrganizationValidator implements ValidationPlugin {

    private static final Set<String> VALID_PERIODS = Set.of(
            "daily", "weekly", "monthly", "quarterly", "yearly", "real_time", "on_demand"
    );

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
        Set<String> entityIds = collectEntityIds(project);
        Set<String> dsIds = collectDataSourceIds(project);

        validateOrganization(project.getOrganizationModel(), issues);
        validateMetrics(project.getMetricsModel(), issues, entityIds, dsIds);
        return issues;
    }

    // ═══════════════════════════════════════════════════════════════
    // Organization model validation (VM-O + VM-HR)
    // ═══════════════════════════════════════════════════════════════

    private void validateOrganization(OrganizationModel org, List<ValidationIssue> issues) {
        if (org == null) return;

        Set<String> deptIds = new HashSet<>();
        Map<String, List<String>> deptPositions = new HashMap<>(); // deptId → positionIds

        // ── Department validations ──
        if (org.getDepartments() != null) {
            for (var dept : org.getDepartments()) {
                // VM-O-01: department id required
                if (dept.getId() == null || dept.getId().isBlank()) {
                    issues.add(makeIssue("VM-O-01", "error", "department", null, null,
                            "Department id must not be empty"));
                } else {
                    deptIds.add(dept.getId());
                }
                // VM-O-02: department name required
                if (dept.getName() == null || dept.getName().isBlank()) {
                    issues.add(makeIssue("VM-O-02", "error", "department", dept.getId(), "name",
                            "Department name must not be empty"));
                }
                // VM-O-03: parent must reference existing department (deferred check)
            }
            // Post-collect: VM-O-03 parent reference check
            for (var dept : org.getDepartments()) {
                if (dept.getParentDepartmentId() != null && !dept.getParentDepartmentId().isBlank()
                        && !deptIds.contains(dept.getParentDepartmentId())) {
                    issues.add(makeIssue("VM-O-03", "warning", "department", dept.getId(),
                            "parentDepartmentId",
                            "Parent department '" + dept.getParentDepartmentId() + "' not found"));
                }
            }
        }

        // ── Position validations ──
        if (org.getPositions() != null) {
            Set<String> positionNamesInDept = new HashSet<>();
            for (var pos : org.getPositions()) {
                // VM-O-04: position id required
                if (pos.getId() == null || pos.getId().isBlank()) {
                    issues.add(makeIssue("VM-O-04", "error", "position", null, null,
                            "Position id must not be empty"));
                    continue;
                }
                // VM-O-05: position departmentId must reference department
                if (pos.getDepartmentId() != null && !pos.getDepartmentId().isBlank()
                        && !deptIds.contains(pos.getDepartmentId())) {
                    issues.add(makeIssue("VM-O-05", "error", "position", pos.getId(), "departmentId",
                            "Position departmentId must reference an existing department"));
                }

                // VM-HR-01: Position should have responsibilities
                if (pos.getResponsibilities() == null || pos.getResponsibilities().isEmpty()) {
                    issues.add(makeIssue("VM-HR-01", "warning", "position", pos.getId(), "responsibilities",
                            "Position should define at least one responsibility"));
                }

                // VM-HR-03: Position name must be unique within department
                if (pos.getName() != null && pos.getDepartmentId() != null) {
                    String nameInDept = pos.getDepartmentId() + "::" + pos.getName();
                    if (!positionNamesInDept.add(nameInDept)) {
                        issues.add(makeIssue("VM-HR-03", "error", "position", pos.getId(), "name",
                                "Duplicate position name '" + pos.getName() + "' in department "
                                        + pos.getDepartmentId()));
                    }
                }

                // Track positions per department
                if (pos.getDepartmentId() != null) {
                    deptPositions.computeIfAbsent(pos.getDepartmentId(), k -> new ArrayList<>())
                            .add(pos.getId());
                }
            }
        }

        // VM-HR-02: Departments with positions should have a parent chain
        if (org.getDepartments() != null) {
            for (var dept : org.getDepartments()) {
                if (dept.getId() != null && deptPositions.containsKey(dept.getId())
                        && dept.getParentDepartmentId() == null) {
                    issues.add(makeIssue("VM-HR-02", "info", "department", dept.getId(), "parentDepartmentId",
                            "Department with positions should belong to a parent hierarchy"));
                }
            }
        }

        // VM-HR-04: Department tree cycle detection
        if (org.getDepartments() != null && deptIds.size() > 1) {
            detectCycles(org.getDepartments(), deptIds, issues);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Cycle detection for department tree (VM-HR-04)
    // ═══════════════════════════════════════════════════════════════

    private void detectCycles(List<Department> departments, Set<String> deptIds,
                               List<ValidationIssue> issues) {
        Map<String, String> parentMap = new HashMap<>();
        for (var dept : departments) {
            if (dept.getId() != null) {
                parentMap.put(dept.getId(), dept.getParentDepartmentId());
            }
        }

        Set<String> visited = new HashSet<>();
        Set<String> inPath = new HashSet<>();

        for (String deptId : deptIds) {
            if (!visited.contains(deptId)) {
                detectCycleDFS(deptId, parentMap, visited, inPath, issues);
            }
        }
    }

    private boolean detectCycleDFS(String nodeId, Map<String, String> parentMap,
                                    Set<String> visited, Set<String> inPath,
                                    List<ValidationIssue> issues) {
        if (inPath.contains(nodeId)) {
            issues.add(makeIssue("VM-HR-04", "error", "department", nodeId, "parentDepartmentId",
                    "Department hierarchy contains a cycle involving: " + nodeId));
            return true;
        }
        if (visited.contains(nodeId)) return false;

        visited.add(nodeId);
        inPath.add(nodeId);

        String parent = parentMap.get(nodeId);
        if (parent != null && parentMap.containsKey(parent)) { // parent exists in our tree
            detectCycleDFS(parent, parentMap, visited, inPath, issues);
        }

        inPath.remove(nodeId);
        return false;
    }

    // ═══════════════════════════════════════════════════════════════
    // Metrics model validation (V-XL-O)
    // ═══════════════════════════════════════════════════════════════

    private void validateMetrics(MetricsModel metrics, List<ValidationIssue> issues,
                                  Set<String> entityIds, Set<String> dsIds) {
        if (metrics == null || metrics.getMetrics() == null) return;

        for (var metric : metrics.getMetrics()) {
            // V-XL-O-01: metric id required
            if (metric.getId() == null || metric.getId().isBlank()) {
                issues.add(makeIssue("V-XL-O-01", "error", "metric", null, null,
                        "Metric id must not be empty"));
            }
            // V-XL-O-02: metric name required
            if (metric.getName() == null || metric.getName().isBlank()) {
                issues.add(makeIssue("V-XL-O-02", "error", "metric", metric.getId(), "name",
                        "Metric name must not be empty"));
            }
            // V-XL-O-03: formula should not be empty
            if (metric.getFormula() == null || metric.getFormula().isBlank()) {
                issues.add(makeIssue("V-XL-O-03", "warning", "metric", metric.getId(), "formula",
                        "Metric formula should not be empty"));
            }
            // V-XL-O-04: dataSourceRef should reference existing data source
            if (metric.getDataSourceRef() != null && !metric.getDataSourceRef().isBlank()
                    && !dsIds.contains(metric.getDataSourceRef())) {
                issues.add(makeIssue("V-XL-O-04", "warning", "metric", metric.getId(),
                        "dataSourceRef",
                        "Metric dataSourceRef '" + metric.getDataSourceRef() + "' not found"));
            }
            // V-XL-O-05: period must be valid
            if (metric.getPeriod() != null && !VALID_PERIODS.contains(metric.getPeriod().toLowerCase())) {
                issues.add(makeIssue("V-XL-O-05", "warning", "metric", metric.getId(), "period",
                        "Metric period '" + metric.getPeriod() + "' is not a standard value; "
                                + "expected: " + String.join(", ", VALID_PERIODS)));
            }
            // V-XL-O-06: targetEntity should reference existing entity
            if (metric.getTargetEntity() != null && !metric.getTargetEntity().isBlank()
                    && !entityIds.contains(metric.getTargetEntity())) {
                issues.add(makeIssue("V-XL-O-06", "warning", "metric", metric.getId(),
                        "targetEntity",
                        "Metric targetEntity '" + metric.getTargetEntity() + "' not found"));
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

    private Set<String> collectDataSourceIds(OntologyProject project) {
        if (project.getDataSourcesModel() == null || project.getDataSourcesModel().getSources() == null)
            return Set.of();
        return project.getDataSourcesModel().getSources().stream()
                .map(DataSource::getId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private ValidationIssue makeIssue(String code, String severity, String elementType,
                                       String elementId, String field, String message) {
        return ValidationIssue.builder()
                .code(code).severity(severity).elementType(elementType)
                .elementId(elementId).field(field).message(message).build();
    }
}

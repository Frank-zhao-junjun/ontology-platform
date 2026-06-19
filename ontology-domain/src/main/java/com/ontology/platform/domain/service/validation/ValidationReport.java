package com.ontology.platform.domain.service.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Aggregated validation report from all {@link ValidationPlugin}s.
 */
@Data
@Builder
@AllArgsConstructor
public class ValidationReport {
    private List<ValidationIssue> issues;
    private String mode;  // strict | warn

    public boolean isValid() {
        return "warn".equals(mode) || issues.stream().noneMatch(i -> "error".equals(i.getSeverity()));
    }

    public int errorCount() {
        return (int) issues.stream().filter(i -> "error".equals(i.getSeverity())).count();
    }

    public int warningCount() {
        return (int) issues.stream().filter(i -> "warning".equals(i.getSeverity())).count();
    }
}

package com.ontology.platform.application.service.exchange;

import com.ontology.platform.domain.service.validation.ValidationContext;
import com.ontology.platform.domain.service.validation.ValidationPlugin;
import com.ontology.platform.domain.service.validation.ValidationReport;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ontology.platform.domain.service.validation.ValidationIssue;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates all registered {@link ValidationPlugin}s to produce a
 * consolidated {@link ValidationReport} for an exchange document.
 *
 * <p>Phase 3 / §5 — Validator Architecture.</p>
 */
@Service
@RequiredArgsConstructor
public class ExchangeValidationService {

    private final List<ValidationPlugin> plugins;

    /**
     * Run all registered validators against the document.
     *
     * @param doc    the v2 exchange document to validate
     * @param mode   strict | warn
     * @return consolidated report
     */
    public ValidationReport validate(OntologyExchangeDocument doc, String mode) {
        String effectiveMode = (mode != null && !mode.isBlank()) ? mode : "strict";
        String projectId = (doc != null && doc.getMetadata() != null) ? doc.getMetadata().getProjectId() : null;
        var ctx = new ValidationContext(doc, effectiveMode, projectId);

        List<ValidationIssue> allIssues = new ArrayList<>();
        for (ValidationPlugin plugin : plugins) {
            allIssues.addAll(plugin.validate(ctx));
        }

        return new ValidationReport(allIssues, effectiveMode);
    }
}

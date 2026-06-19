package com.ontology.platform.domain.service.validation;

import java.util.List;

/**
 * Validation plugin interface for Phase 3 Exchange validators.
 *
 * <p>Each plugin implements a family of validation rules (VE, VM, VX, V-LC, V-AS, etc.)
 * and is auto-discovered by {@link ExchangeValidationService} via Spring DI.</p>
 */
public interface ValidationPlugin {

    /** Short code prefix for this plugin's rules (e.g. "VE", "VM", "VX"). */
    String pluginCode();

    /** Human-readable plugin name (e.g. "EPC Event Validator"). */
    String pluginName();

    /** Run validation against the given context and return all issues found. */
    List<ValidationIssue> validate(ValidationContext context);
}

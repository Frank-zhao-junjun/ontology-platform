package com.ontology.platform.domain.service.validation;

import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Context passed to each {@link ValidationPlugin} during validation.
 */
@Data
@Builder
@AllArgsConstructor
public class ValidationContext {
    private OntologyExchangeDocument document;
    private String validationMode;  // strict | warn
    private String projectId;
}

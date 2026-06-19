package com.ontology.platform.domain.service.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * A single validation issue found by a {@link ValidationPlugin}.
 */
@Data
@Builder
@AllArgsConstructor
public class ValidationIssue {
    private String code;         // e.g. "VE-01"
    private String severity;     // error | warning | info
    private String elementType;  // entity | action | state | event | etc.
    private String elementId;    // ID of the element with the issue
    private String field;        // specific field name
    private String message;      // human-readable description
}

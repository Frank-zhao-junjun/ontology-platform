package com.ontology.platform.domain.dto.imports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Exchange import request/response DTO.
 * Carries the raw document payload plus optional metadata for validation control.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeImportDocument {

    /** JSON string of OntologyExchange */
    private String document;

    /** Optional external identifier */
    private String externalId;

    /** Validation mode: strict | warn */
    private String validationMode;
}

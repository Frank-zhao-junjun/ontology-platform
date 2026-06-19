package com.ontology.platform.domain.dto.imports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response returned after submitting an exchange import request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeImportResponse {

    private String id;

    /** pending | passed | failed */
    private String status;

    private int totalEntities;

    private int warnings;
}

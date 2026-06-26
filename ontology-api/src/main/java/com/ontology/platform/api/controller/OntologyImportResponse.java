package com.ontology.platform.api.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OntologyImportResponse {
    private String draftId;
    private String externalId;
    private Map<String, Integer> importedCounts;
}

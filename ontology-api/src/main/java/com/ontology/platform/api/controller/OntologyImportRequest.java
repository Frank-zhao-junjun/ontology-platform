package com.ontology.platform.api.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OntologyImportRequest {
    private String rawContent;
    private String createdBy;
}

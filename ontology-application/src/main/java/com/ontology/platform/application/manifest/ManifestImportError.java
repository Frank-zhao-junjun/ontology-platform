package com.ontology.platform.application.manifest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManifestImportError {
    private String code;
    private String elementType;
    private String id;
    private String field;
    private String message;
}

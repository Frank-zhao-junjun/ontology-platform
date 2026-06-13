package com.ontology.platform.application.dto.manifest;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ImportManifestRequest {
    private String sourceFormat; // YAML or JSON
    private String rawContent;
    private String createdBy;
}

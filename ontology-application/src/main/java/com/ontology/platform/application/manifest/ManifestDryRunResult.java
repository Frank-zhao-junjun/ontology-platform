package com.ontology.platform.application.manifest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManifestDryRunResult {
    private boolean valid;
    private String draftId;
    @Builder.Default
    private ManifestImportedCounts importedCounts = new ManifestImportedCounts();
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
    @Builder.Default
    private List<ManifestImportError> errors = new ArrayList<>();
}

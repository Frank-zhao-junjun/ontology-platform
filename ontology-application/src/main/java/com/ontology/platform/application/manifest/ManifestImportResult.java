package com.ontology.platform.application.manifest;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ManifestImportResult extends ManifestDryRunResult {
    private String contextId;
}

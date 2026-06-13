package com.ontology.platform.application.service.manifest;

import com.ontology.platform.application.dto.manifest.*;
import com.ontology.platform.domain.vo.manifest.*;

public interface ManifestService {
    ImportManifestResponse importManifest(ImportManifestRequest request);
    ManifestPreviewResponse preview(String importId);
    ManifestPublishResponse publish(String importId);
    ManifestDocument export(String versionId, String format);
}

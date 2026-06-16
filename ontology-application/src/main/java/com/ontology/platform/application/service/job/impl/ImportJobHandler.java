package com.ontology.platform.application.service.job.impl;

import com.ontology.platform.application.dto.upload.ImportRequest;
import com.ontology.platform.application.dto.upload.ImportTaskResponse;
import com.ontology.platform.domain.service.JobHandler;
import com.ontology.platform.application.service.upload.ImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Handles "import.execute" async jobs by delegating to ImportService.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImportJobHandler implements JobHandler {

    private final ImportService importService;

    @Override
    public String supportedJobType() {
        return "import.execute";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> payload, String tenantId, String agentId)
            throws Exception {

        var request = ImportRequest.builder()
                .uploadId((String) payload.get("uploadId"))
                .ontologyId((String) payload.get("ontologyId"))
                .objectTypeName((String) payload.get("objectTypeName"))
                .encoding((String) payload.getOrDefault("encoding", "UTF-8"))
                .errorHandling((String) payload.getOrDefault("errorHandling", "SKIP"))
                .mergeStrategy((String) payload.getOrDefault("mergeStrategy", "INSERT"))
                .build();

        log.info("Executing import job: uploadId={}, ontologyId={}, objectType={}",
                request.getUploadId(), request.getOntologyId(), request.getObjectTypeName());

        ImportTaskResponse result = importService.executeImport(request, agentId, tenantId);

        var progress = result.getProgress();
        return Map.<String, Object>of(
                "importId", result.getImportId(),
                "status", result.getStatus() != null ? result.getStatus() : "COMPLETED",
                "totalRows", progress != null ? progress.getTotalRows() : 0L,
                "processedRows", progress != null ? progress.getProcessedRows() : 0L,
                "failedRows", 0L
        );
    }
}

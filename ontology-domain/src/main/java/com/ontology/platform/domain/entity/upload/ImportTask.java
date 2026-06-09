package com.ontology.platform.domain.entity.upload;

import com.ontology.platform.common.enums.upload.ErrorHandling;
import com.ontology.platform.common.enums.upload.ImportStatus;
import com.ontology.platform.common.enums.upload.MergeStrategy;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportTask {
    private String id;
    private String uploadId;
    private String ontologyId;
    private String objectTypeName;
    private String objectTypeId;
    private MergeStrategy mergeStrategy;
    private ErrorHandling errorHandling;
    private String userId;
    private String tenantId;
    private ImportStatus status;
    private long totalRows;
    private long processedRows;
    private long successRows;
    private long failedRows;
    @Builder.Default
    private List<ImportError> errors = new ArrayList<>();
    private Instant createdAt;
    private Instant completedAt;
    private Instant estimatedCompletion;

    public static ImportTask create(String uploadId, String ontologyId, String objectTypeName,
                                    MergeStrategy mergeStrategy, ErrorHandling errorHandling,
                                    String userId, String tenantId) {
        return ImportTask.builder()
                .id(UUID.randomUUID().toString())
                .uploadId(uploadId)
                .ontologyId(ontologyId)
                .objectTypeName(objectTypeName)
                .mergeStrategy(mergeStrategy)
                .errorHandling(errorHandling)
                .userId(userId)
                .tenantId(tenantId)
                .status(ImportStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }

    public void startParsing(long totalRows) {
        this.status = ImportStatus.PARSING;
        this.totalRows = totalRows;
    }

    public void startValidating() {
        this.status = ImportStatus.VALIDATING;
    }

    public void startImporting() {
        this.status = ImportStatus.IMPORTING;
    }

    public void updateProgress(long processedRows, long successRows, long failedRows) {
        this.processedRows = processedRows;
        this.successRows = successRows;
        this.failedRows = failedRows;
    }

    public void addError(int row, String field, String message, String originalValue) {
        errors.add(new ImportError(row, field, message, originalValue));
    }

    public void markCompleted() {
        this.status = ImportStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void markFailed(String message) {
        this.status = ImportStatus.FAILED;
        addError(0, null, message, null);
        this.completedAt = Instant.now();
    }

    public void markCancelled() {
        this.status = ImportStatus.CANCELLED;
        this.completedAt = Instant.now();
    }

    public int getProgressPercent() {
        return totalRows == 0 ? 0 : (int) Math.round(processedRows * 100.0 / totalRows);
    }

    public record ImportError(int row, String field, String message, String originalValue) {
        public int getRow() { return row; }
        public String getField() { return field; }
        public String getMessage() { return message; }
        public String getOriginalValue() { return originalValue; }
    }
}

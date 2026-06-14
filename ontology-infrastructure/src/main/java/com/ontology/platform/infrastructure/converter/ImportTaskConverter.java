package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.common.enums.upload.ErrorHandling;
import com.ontology.platform.common.enums.upload.ImportStatus;
import com.ontology.platform.common.enums.upload.MergeStrategy;
import com.ontology.platform.domain.entity.upload.ImportTask;
import com.ontology.platform.infrastructure.persistence.ImportTaskPO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ImportTaskConverter {

    public ImportTask toEntity(ImportTaskPO po) {
        if (po == null) return null;
        ImportTask entity = new ImportTask();
        entity.setId(po.getId());
        entity.setUploadId(po.getUploadId());
        entity.setOntologyId(po.getOntologyId());
        entity.setObjectTypeName(po.getObjectTypeName());
        entity.setObjectTypeId(po.getObjectTypeId());
        entity.setMergeStrategy(po.getMergeStrategy() != null ? MergeStrategy.fromCode(po.getMergeStrategy()) : null);
        entity.setErrorHandling(po.getErrorHandling() != null ? ErrorHandling.fromCode(po.getErrorHandling()) : null);
        entity.setUserId(po.getUserId());
        entity.setTenantId(po.getTenantId());
        entity.setStatus(po.getStatus() != null ? ImportStatus.valueOf(po.getStatus()) : null);
        entity.setTotalRows(po.getTotalRows() != null ? po.getTotalRows() : 0);
        entity.setProcessedRows(po.getProcessedRows() != null ? po.getProcessedRows() : 0);
        entity.setSuccessRows(po.getSuccessRows() != null ? po.getSuccessRows() : 0);
        entity.setFailedRows(po.getFailedRows() != null ? po.getFailedRows() : 0);
        entity.setErrors(toEntityErrors(po.getErrorsList()));
        entity.setCreatedAt(po.getCreatedAt());
        entity.setCompletedAt(po.getCompletedAt());
        entity.setEstimatedCompletion(po.getEstimatedCompletion());
        return entity;
    }

    public ImportTaskPO toPO(ImportTask entity) {
        if (entity == null) return null;
        ImportTaskPO po = new ImportTaskPO();
        po.setId(entity.getId());
        po.setUploadId(entity.getUploadId());
        po.setOntologyId(entity.getOntologyId());
        po.setObjectTypeName(entity.getObjectTypeName());
        po.setObjectTypeId(entity.getObjectTypeId());
        po.setMergeStrategy(entity.getMergeStrategy() != null ? entity.getMergeStrategy().name() : null);
        po.setErrorHandling(entity.getErrorHandling() != null ? entity.getErrorHandling().name() : null);
        po.setUserId(entity.getUserId());
        po.setTenantId(entity.getTenantId());
        po.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        po.setTotalRows(entity.getTotalRows());
        po.setProcessedRows(entity.getProcessedRows());
        po.setSuccessRows(entity.getSuccessRows());
        po.setFailedRows(entity.getFailedRows());
        po.setErrorsList(toPOErrors(entity.getErrors()));
        po.setCreatedAt(entity.getCreatedAt());
        po.setCompletedAt(entity.getCompletedAt());
        po.setEstimatedCompletion(entity.getEstimatedCompletion());
        return po;
    }

    private List<ImportTask.ImportError> toEntityErrors(List<Map<String, Object>> poErrors) {
        if (poErrors == null) return new ArrayList<>();
        return poErrors.stream().map(m -> {
            int row = m.get("row") instanceof Number n ? n.intValue() : 0;
            String field = (String) m.getOrDefault("field", null);
            String message = (String) m.getOrDefault("message", null);
            String originalValue = (String) m.getOrDefault("originalValue", null);
            return new ImportTask.ImportError(row, field, message, originalValue);
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> toPOErrors(List<ImportTask.ImportError> entityErrors) {
        if (entityErrors == null) return new ArrayList<>();
        return entityErrors.stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("row", e.getRow());
            m.put("field", e.getField());
            m.put("message", e.getMessage());
            m.put("originalValue", e.getOriginalValue());
            return m;
        }).collect(Collectors.toList());
    }
}

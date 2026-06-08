package com.ontology.platform.application.service.impl.upload;

import com.ontology.platform.application.dto.upload.ImportRequest;
import com.ontology.platform.application.dto.upload.ImportTaskResponse;
import com.ontology.platform.application.service.upload.ImportService;
import com.ontology.platform.common.enums.upload.ErrorHandling;
import com.ontology.platform.common.enums.upload.ImportStatus;
import com.ontology.platform.common.enums.upload.MergeStrategy;
import com.ontology.platform.common.exception.upload.ImportException;
import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.entity.upload.UploadTask;
import com.ontology.platform.domain.repository.ObjectTypeRepository;
import com.ontology.platform.domain.repository.upload.ImportTaskRepository;
import com.ontology.platform.domain.repository.upload.UploadTaskRepository;
import com.ontology.platform.domain.service.upload.DataFileParser;
import com.ontology.platform.domain.service.upload.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据导入服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImportServiceImpl implements ImportService {

    private final ImportTaskRepository importTaskRepository;
    private final UploadTaskRepository uploadTaskRepository;
    private final FileStorageService fileStorageService;
    private final DataFileParser dataFileParser;
    private final ObjectTypeRepository objectTypeRepository;

    private final Map<String, Boolean> runningTasks = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public ImportTaskResponse executeImport(ImportRequest request, String userId, String tenantId) {
        log.info("Executing import: ontologyId={}, objectType={}, userId={}", 
                request.getOntologyId(), request.getObjectTypeName(), userId);

        UploadTask uploadTask = uploadTaskRepository.findById(request.getUploadId())
                .orElseThrow(() -> ImportException.parsingError(
                        request.getUploadId(), "Upload task not found"));

        ObjectType objectType = objectTypeRepository.findById(request.getObjectTypeName())
                .orElseThrow(() -> ImportException.objectTypeNotFound(request.getObjectTypeName()));

        var importTask = com.ontology.platform.domain.entity.upload.ImportTask.create(
                request.getUploadId(),
                request.getOntologyId(),
                request.getObjectTypeName(),
                MergeStrategy.fromCode(request.getMergeStrategy()),
                ErrorHandling.fromCode(request.getErrorHandling()),
                userId,
                tenantId
        );
        importTask.setObjectTypeId(objectType.getId());
        importTaskRepository.save(importTask);

        try {
            return doImport(importTask, uploadTask, request, objectType);
        } catch (Exception e) {
            log.error("Import failed: importId={}", importTask.getId(), e);
            importTask.markFailed(e.getMessage());
            importTaskRepository.update(importTask);
            throw e;
        }
    }

    @Override
    @Async("importTaskExecutor")
    public String executeImportAsync(ImportRequest request, String userId, String tenantId) {
        log.info("Starting async import: ontologyId={}, objectType={}", 
                request.getOntologyId(), request.getObjectTypeName());

        try {
            executeImport(request, userId, tenantId);
            return "completed";
        } catch (Exception e) {
            log.error("Async import failed", e);
            return "failed: " + e.getMessage();
        }
    }

    @Override
    public ImportTaskResponse getImportStatus(String importId) {
        var task = importTaskRepository.findById(importId)
                .orElseThrow(() -> new com.ontology.platform.common.exception.ResourceNotFoundException(
                        "ImportTask", importId));
        return toImportTaskResponse(task);
    }

    @Override
    @Transactional
    public void cancelImport(String importId) {
        runningTasks.put(importId, false);
        
        var task = importTaskRepository.findById(importId)
                .orElseThrow(() -> new com.ontology.platform.common.exception.ResourceNotFoundException(
                        "ImportTask", importId));
        
        if (task.getStatus() == ImportStatus.PENDING || 
            task.getStatus() == ImportStatus.PARSING ||
            task.getStatus() == ImportStatus.VALIDATING ||
            task.getStatus() == ImportStatus.IMPORTING) {
            task.markCancelled();
            importTaskRepository.update(task);
            log.info("Import cancelled: importId={}", importId);
        }
    }

    @Override
    public Map<String, Object> parseDataFile(String filePath, String fileType, Map<String, Object> config) {
        try (InputStream inputStream = new FileInputStream(filePath)) {
            var parseConfig = new DataFileParser.ParseConfig(
                    fileType,
                    (Boolean) config.getOrDefault("skipHeader", true),
                    (String) config.getOrDefault("encoding", "UTF-8"),
                    0
            );
            
            List<Map<String, String>> data = dataFileParser.parseDataFile(inputStream, parseConfig);
            
            Map<String, Object> result = new HashMap<>();
            result.put("headers", data.isEmpty() ? List.of() : new ArrayList<>(data.get(0).keySet()));
            result.put("data", data);
            result.put("totalRows", data.size());
            
            return result;
        } catch (Exception e) {
            log.error("Failed to parse data file: {}", filePath, e);
            throw ImportException.parsingError(filePath, e.getMessage());
        }
    }

    @Override
    public ValidationResult batchValidate(List<Map<String, String>> dataList, String objectTypeId) {
        List<ValidationResult.ValidationError> errors = new ArrayList<>();
        int successCount = 0;

        ObjectType objectType = objectTypeRepository.findById(objectTypeId)
                .orElseThrow(() -> ImportException.objectTypeNotFound(objectTypeId));

        Map<String, com.ontology.platform.domain.vo.Property> propertyMap = 
                objectType.getProperties().stream()
                        .collect(Collectors.toMap(
                                com.ontology.platform.domain.vo.Property::getName,
                                p -> p
                        ));

        for (int i = 0; i < dataList.size(); i++) {
            Map<String, String> row = dataList.get(i);
            boolean rowValid = true;

            for (Map.Entry<String, String> entry : row.entrySet()) {
                String columnName = entry.getKey();
                String value = entry.getValue();
                
                var property = propertyMap.get(columnName);
                if (property == null) {
                    continue;
                }

                if (property.isRequired() && (value == null || value.isBlank())) {
                    errors.add(new ValidationResult.ValidationError(i + 1, columnName, "字段不能为空"));
                    rowValid = false;
                }

                if (value != null && !value.isBlank() && !property.validateValue(value)) {
                    errors.add(new ValidationResult.ValidationError(i + 1, columnName, "数据类型错误"));
                    rowValid = false;
                }
            }

            if (rowValid) {
                successCount++;
            }
        }

        return new ValidationResult(
                errors.isEmpty(),
                dataList.size(),
                successCount,
                dataList.size() - successCount,
                errors
        );
    }

    private ImportTaskResponse doImport(
            com.ontology.platform.domain.entity.upload.ImportTask importTask,
            UploadTask uploadTask,
            ImportRequest request,
            ObjectType objectType) throws Exception {
        
        runningTasks.put(importTask.getId(), true);
        
        try {
            importTask.startParsing(0);
            importTaskRepository.update(importTask);

            String filePath = uploadTask.getStoredFilePath();
            var parseConfig = new DataFileParser.ParseConfig(
                    uploadTask.getFileType().getCode(),
                    request.getSkipHeader() != null ? request.getSkipHeader() : true,
                    request.getEncoding() != null ? request.getEncoding() : "UTF-8",
                    0
            );

            List<Map<String, String>> data;
            try (InputStream inputStream = new FileInputStream(filePath)) {
                data = dataFileParser.parseDataFile(inputStream, parseConfig);
            }

            if (data.isEmpty()) {
                throw ImportException.parsingError(uploadTask.getOriginalFileName(), "文件为空");
            }

            List<Map<String, String>> mappedData = applyColumnMapping(data, request.getColumnMapping());

            importTask.startValidating();
            importTask.setTotalRows(mappedData.size());
            importTaskRepository.update(importTask);

            ValidationResult validationResult = batchValidate(mappedData, objectType.getId());

            importTask.startImporting();
            importTaskRepository.update(importTask);

            int batchSize = 1000;
            int successCount = 0;
            int failCount = 0;

            for (int i = 0; i < mappedData.size(); i += batchSize) {
                if (!runningTasks.getOrDefault(importTask.getId(), true)) {
                    log.info("Import cancelled by user: importId={}", importTask.getId());
                    break;
                }

                int endIndex = Math.min(i + batchSize, mappedData.size());
                List<Map<String, String>> batch = mappedData.subList(i, endIndex);

                for (Map<String, String> row : batch) {
                    try {
                        importObject(row, objectType, request);
                        successCount++;
                    } catch (Exception e) {
                        failCount++;
                        importTask.addError(i, null, e.getMessage(), row.toString());
                        
                        if (ErrorHandling.STOP.name().equals(request.getErrorHandling())) {
                            throw e;
                        }
                    }
                }

                importTask.updateProgress(endIndex, successCount, failCount);
                importTaskRepository.update(importTask);
            }

            importTask.markCompleted();
            importTaskRepository.update(importTask);

            log.info("Import completed: importId={}, success={}, failed={}", 
                    importTask.getId(), successCount, failCount);

            runningTasks.remove(importTask.getId());
            return toImportTaskResponse(importTask);

        } catch (Exception e) {
            runningTasks.remove(importTask.getId());
            throw e;
        }
    }

    private List<Map<String, String>> applyColumnMapping(
            List<Map<String, String>> data, Map<String, String> columnMapping) {
        
        if (columnMapping == null || columnMapping.isEmpty()) {
            return data;
        }

        return data.stream().map(row -> {
            Map<String, String> mappedRow = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : row.entrySet()) {
                String mappedKey = columnMapping.getOrDefault(entry.getKey(), entry.getKey());
                mappedRow.put(mappedKey, entry.getValue());
            }
            return mappedRow;
        }).collect(Collectors.toList());
    }

    private void importObject(Map<String, String> data, ObjectType objectType, ImportRequest request) {
        log.debug("Importing object: objectType={}, data={}", objectType.getName(), data);
    }

    private ImportTaskResponse toImportTaskResponse(
            com.ontology.platform.domain.entity.upload.ImportTask task) {
        List<ImportTaskResponse.ErrorDetail> errors = task.getErrors().stream()
                .map(e -> ImportTaskResponse.ErrorDetail.builder()
                        .row(e.getRow())
                        .field(e.getField())
                        .message(e.getMessage())
                        .originalValue(e.getOriginalValue())
                        .build())
                .collect(Collectors.toList());

        return ImportTaskResponse.builder()
                .importId(task.getId())
                .uploadId(task.getUploadId())
                .ontologyId(task.getOntologyId())
                .objectTypeName(task.getObjectTypeName())
                .status(task.getStatus().getCode())
                .progress(ImportTaskResponse.Progress.builder()
                        .totalRows(task.getTotalRows())
                        .processedRows(task.getProcessedRows())
                        .successRows(task.getSuccessRows())
                        .failedRows(task.getFailedRows())
                        .progressPercent(task.getProgressPercent())
                        .build())
                .errors(errors)
                .startedAt(task.getCreatedAt().toString())
                .completedAt(task.getCompletedAt() != null ? task.getCompletedAt().toString() : null)
                .estimatedCompletion(
                        task.getEstimatedCompletion() != null ? 
                                task.getEstimatedCompletion().toString() : null)
                .build();
    }
}

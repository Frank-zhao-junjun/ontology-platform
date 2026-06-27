package com.ontology.platform.application.service.impl.upload;

import com.ontology.platform.application.dto.upload.ExportRequest;
import com.ontology.platform.application.service.upload.ExportService;
import com.ontology.platform.domain.entity.ObjectInstance;
import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.repository.ObjectInstanceRepository;
import com.ontology.platform.domain.repository.ObjectTypeRepository;
import com.ontology.platform.domain.service.upload.DataExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据导出服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExportServiceImpl implements ExportService {

    private final ObjectTypeRepository objectTypeRepository;
    private final ObjectInstanceRepository objectInstanceRepository;
    private final DataExportService dataExportService;

    private final Map<String, ExportTaskInfo> exportTasks = new ConcurrentHashMap<>();

    @Override
    public ExportResult executeExport(ExportRequest request, String userId, String tenantId) {
        log.info("Executing export: ontologyId={}, objectType={}, format={}", 
                request.getOntologyId(), request.getObjectTypeName(), request.getFormat());

        ObjectType objectType = objectTypeRepository.findById(request.getObjectTypeName())
                .orElseThrow(() -> new com.ontology.platform.common.exception.ResourceNotFoundException(
                        "ObjectType", request.getObjectTypeName()));

        List<Map<String, Object>> data = queryData(objectType, request);

        String exportId = "export_" + UUID.randomUUID().toString().substring(0, 12);
        String fileName = objectType.getName() + "_export_" + System.currentTimeMillis() + "." + request.getFormat();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        List<String> headers = objectType.getProperties().stream()
                .map(p -> p.getName())
                .collect(Collectors.toList());

        if ("xlsx".equalsIgnoreCase(request.getFormat())) {
            dataExportService.exportToExcel(data, headers, outputStream, objectType.getDisplayName());
        } else {
            dataExportService.exportToCsv(data, headers, outputStream, 
                    request.getEncoding() != null ? request.getEncoding() : "UTF-8");
        }

        byte[] fileData = outputStream.toByteArray();

        log.info("Export completed: exportId={}, rows={}", exportId, data.size());

        return new ExportResult(exportId, fileName, request.getFormat(), fileData, data.size(), data.size());
    }

    @Override
    public String executeExportAsync(ExportRequest request, String userId, String tenantId) {
        String exportId = "export_" + UUID.randomUUID().toString().substring(0, 12);
        
        exportTasks.put(exportId, new ExportTaskInfo(
                exportId, request, userId, tenantId, "PENDING", null));

        new Thread(() -> {
            try {
                ExportResult result = executeExport(request, userId, tenantId);
                exportTasks.put(exportId, new ExportTaskInfo(
                        exportId, request, userId, tenantId, "COMPLETED", result.data()));
            } catch (Exception e) {
                log.error("Async export failed: exportId={}", exportId, e);
                exportTasks.put(exportId, new ExportTaskInfo(
                        exportId, request, userId, tenantId, "FAILED", null));
            }
        }).start();

        return exportId;
    }

    @Override
    public StreamingResponseBody streamExport(ExportRequest request, String userId, String tenantId) {
        log.info("Streaming export: ontologyId={}, objectType={}", 
                request.getOntologyId(), request.getObjectTypeName());

        ObjectType objectType = objectTypeRepository.findById(request.getObjectTypeName())
                .orElseThrow(() -> new com.ontology.platform.common.exception.ResourceNotFoundException(
                        "ObjectType", request.getObjectTypeName()));

        List<String> headers = objectType.getProperties().stream()
                .map(p -> p.getName())
                .collect(Collectors.toList());

        return outputStream -> {
            int pageSize = 1000;
            int page = 1;
            boolean hasMore = true;

            while (hasMore) {
                List<Map<String, Object>> pageData = queryDataPage(objectType, request, page, pageSize);
                
                if (pageData.isEmpty()) {
                    hasMore = false;
                } else {
                    if ("xlsx".equalsIgnoreCase(request.getFormat())) {
                        dataExportService.exportToExcel(pageData, headers, outputStream, objectType.getDisplayName());
                    } else {
                        if (page == 1) {
                            dataExportService.exportToCsv(pageData, headers, outputStream,
                                    request.getEncoding() != null ? request.getEncoding() : "UTF-8");
                        } else {
                            writeCsvData(pageData, outputStream);
                        }
                    }
                    
                    outputStream.flush();
                    
                    if (pageData.size() < pageSize) {
                        hasMore = false;
                    } else {
                        page++;
                    }
                }
            }
        };
    }

    @Override
    public byte[] getExportFile(String exportId) {
        ExportTaskInfo taskInfo = exportTasks.get(exportId);
        if (taskInfo == null) {
            throw new com.ontology.platform.common.exception.ResourceNotFoundException("ExportTask", exportId);
        }
        return taskInfo.data();
    }

    private List<Map<String, Object>> queryData(ObjectType objectType, ExportRequest request) {
        int limit = request.getLimit() != null ? request.getLimit() : 100000;
        return queryDataPage(objectType, request, 1, limit);
    }

    private List<Map<String, Object>> queryDataPage(ObjectType objectType,
            ExportRequest request, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<ObjectInstance> instances = objectInstanceRepository.findByObjectTypeId(
                objectType.getId(), offset, pageSize);

        List<Map<String, Object>> result = new ArrayList<>();
        var properties = objectType.getProperties();

        for (ObjectInstance instance : instances) {
            Map<String, Object> row = new LinkedHashMap<>();
            Map<String, Object> coreData = instance.getCoreData();
            for (var prop : properties) {
                row.put(prop.getName(), coreData != null ? coreData.get(prop.getName()) : null);
            }
            result.add(row);
        }

        return result;
    }

    private void writeCsvData(List<Map<String, Object>> data, OutputStream outputStream) {
        try {
            for (Map<String, Object> row : data) {
                StringBuilder line = new StringBuilder();
                for (Object value : row.values()) {
                    if (line.length() > 0) {
                        line.append(",");
                    }
                    line.append(escapeCsvValue(value));
                }
                line.append("\n");
                outputStream.write(line.toString().getBytes("UTF-8"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to write CSV data", e);
        }
    }

    private String escapeCsvValue(Object value) {
        if (value == null) {
            return "";
        }
        String str = value.toString();
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }

    private record ExportTaskInfo(
            String exportId,
            ExportRequest request,
            String userId,
            String tenantId,
            String status,
            byte[] data
    ) {}
}

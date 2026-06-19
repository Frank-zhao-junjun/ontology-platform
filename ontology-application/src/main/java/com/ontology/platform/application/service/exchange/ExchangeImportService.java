package com.ontology.platform.application.service.exchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.domain.dto.imports.ExchangeImportResponse;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.service.validation.ValidationReport;
import com.ontology.platform.infrastructure.imports.ExcelExchangeMapper;
import com.ontology.platform.infrastructure.persistence.ExchangeImportPO;
import com.ontology.platform.infrastructure.persistence.ExchangeImportPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Exchange import service for Phase 3a/3b v2 exchange import pipeline.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExchangeImportService {

    private final ExchangeImportPOMapper mapper;
    private final ObjectMapper objectMapper;
    private final ExchangeValidationService validationService;
    private final ExchangePhase3bPublisher phase3bPublisher;
    private final ExcelExchangeMapper excelExchangeMapper;

    public ExchangeImportResponse importExchange(String jsonDocument, String validationMode) {
        if (jsonDocument == null || jsonDocument.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Document must not be empty");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(jsonDocument);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "Invalid JSON document: " + e.getMessage());
        }

        validateEnvelope(root);

        OntologyExchangeDocument doc = parseDocument(jsonDocument);
        String mode = (validationMode != null && !validationMode.isBlank()) ? validationMode : "strict";
        ValidationReport report = validationService.validate(doc, mode);

        JsonNode metadataNode = root.get("metadata");
        String metadataId = getTextSafely(metadataNode, "id", UUID.randomUUID().toString());
        String metadataName = getTextSafely(metadataNode, "name", "");
        String metadataVersion = getTextSafely(metadataNode, "version", "0.0.0");
        String metadataSource = getTextSafely(metadataNode, "source", "api");
        String metadataStatus = getTextSafely(metadataNode, "status", "draft");

        JsonNode specNode = root.get("spec");
        String projectId = null;
        String projectName = null;
        int totalEntities = 0;
        if (specNode != null) {
            JsonNode projectNode = specNode.get("project");
            if (projectNode != null) {
                projectId = getTextSafely(projectNode, "id", null);
                projectName = getTextSafely(projectNode, "name", null);
                JsonNode dataModelNode = projectNode.get("dataModel");
                if (dataModelNode != null) {
                    JsonNode entitiesNode = dataModelNode.get("entities");
                    if (entitiesNode != null && entitiesNode.isArray()) {
                        totalEntities = entitiesNode.size();
                    }
                }
            }
        }

        String validationStatus = report.isValid() ? "passed" : "failed";
        String validationReportJson = buildValidationReportJson(totalEntities, report);

        String importId = UUID.randomUUID().toString();
        ExchangeImportPO po = ExchangeImportPO.builder()
                .id(importId)
                .metadataId(metadataId)
                .metadataName(metadataName)
                .metadataVersion(metadataVersion)
                .metadataSource(metadataSource)
                .metadataStatus(metadataStatus)
                .projectId(projectId)
                .projectName(projectName)
                .rawDocument(jsonDocument)
                .validationStatus(validationStatus)
                .validationReport(validationReportJson)
                .importedAt(Instant.now())
                .createdBy("system")
                .updatedAt(Instant.now())
                .build();

        mapper.insert(po);

        log.info("Exchange import saved: id={}, status={}, entities={}", importId, validationStatus, totalEntities);

        return ExchangeImportResponse.builder()
                .id(importId)
                .status(validationStatus)
                .totalEntities(totalEntities)
                .warnings(report.warningCount())
                .build();
    }

    /**
     * Import from Excel xlsx stream — compiles to v2 exchange then imports.
     */
    public ExchangeImportResponse importFromExcel(InputStream xlsxStream, String externalId, String validationMode)
            throws IOException {
        OntologyExchangeDocument doc = excelExchangeMapper.mapFromXlsx(xlsxStream, externalId);
        String json = excelExchangeMapper.toJson(doc);
        return importExchange(json, validationMode);
    }

    /**
     * Import from pre-parsed JSON (OntologyProject or full OntologyExchange).
     */
    public ExchangeImportResponse importFromParsedData(String parsedDataJson, String validationMode)
            throws IOException {
        OntologyExchangeDocument doc = excelExchangeMapper.mapFromParsedData(parsedDataJson);
        String json = excelExchangeMapper.toJson(doc);
        return importExchange(json, validationMode);
    }

    @Transactional(readOnly = true)
    public ExchangeImportResponse getImportStatus(String id) {
        ExchangeImportPO po = mapper.selectById(id);
        if (po == null) {
            throw new ResourceNotFoundException("ExchangeImport", id);
        }
        return ExchangeImportResponse.builder()
                .id(po.getId())
                .status(po.getValidationStatus())
                .totalEntities(parseTotalEntities(po.getValidationReport()))
                .warnings(parseWarnings(po.getValidationReport()))
                .build();
    }

    public ExchangeImportResponse publishImport(String id) {
        ExchangeImportPO po = mapper.selectById(id);
        if (po == null) {
            throw new ResourceNotFoundException("ExchangeImport", id);
        }
        if (!"passed".equals(po.getValidationStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "Cannot publish import with status: " + po.getValidationStatus());
        }

        OntologyExchangeDocument doc = parseDocument(po.getRawDocument());
        String ontologyId = po.getMetadataId() != null ? po.getMetadataId() : po.getProjectId();
        Map<String, Integer> phase3bCounts = phase3bPublisher.publish(ontologyId, doc);

        po.setMetadataStatus("published");
        po.setPublishedAt(Instant.now());
        po.setUpdatedAt(Instant.now());
        mapper.updateById(po);

        log.info("Exchange import published: id={}, phase3bCounts={}", id, phase3bCounts);

        return ExchangeImportResponse.builder()
                .id(po.getId())
                .status("published")
                .totalEntities(parseTotalEntities(po.getValidationReport()))
                .warnings(parseWarnings(po.getValidationReport()))
                .build();
    }

    private void validateEnvelope(JsonNode root) {
        JsonNode apiVersionNode = root.get("apiVersion");
        if (apiVersionNode == null || !apiVersionNode.asText().startsWith("ontology.platform/")) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Missing or invalid apiVersion; expected ontology.platform/v2");
        }
        JsonNode kindNode = root.get("kind");
        if (kindNode == null || !"OntologyExchange".equals(kindNode.asText())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Missing or invalid kind; expected OntologyExchange");
        }
        if (root.get("metadata") == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Missing required field: metadata");
        }
    }

    private OntologyExchangeDocument parseDocument(String json) {
        try {
            return objectMapper.readValue(json, OntologyExchangeDocument.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "Invalid exchange document: " + e.getMessage());
        }
    }

    private String buildValidationReportJson(int totalEntities, ValidationReport report) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "totalEntities", totalEntities,
                    "warnings", report.warningCount(),
                    "errors", report.errorCount(),
                    "issues", report.getIssues()
            ));
        } catch (JsonProcessingException e) {
            return "{\"totalEntities\":" + totalEntities + ",\"warnings\":" + report.warningCount() + "}";
        }
    }

    private String getTextSafely(JsonNode parent, String field, String defaultValue) {
        JsonNode node = parent.get(field);
        return (node != null && !node.isNull()) ? node.asText() : defaultValue;
    }

    private int parseTotalEntities(String validationReport) {
        if (validationReport == null || validationReport.isBlank()) return 0;
        try {
            JsonNode report = objectMapper.readTree(validationReport);
            JsonNode te = report.get("totalEntities");
            return te != null ? te.asInt(0) : 0;
        } catch (JsonProcessingException e) {
            return 0;
        }
    }

    private int parseWarnings(String validationReport) {
        if (validationReport == null || validationReport.isBlank()) return 0;
        try {
            JsonNode report = objectMapper.readTree(validationReport);
            JsonNode w = report.get("warnings");
            return w != null ? w.asInt(0) : 0;
        } catch (JsonProcessingException e) {
            return 0;
        }
    }
}

package com.ontology.platform.application.service.exchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.domain.dto.imports.ExchangeImportDocument;
import com.ontology.platform.domain.dto.imports.ExchangeImportResponse;
import com.ontology.platform.infrastructure.persistence.ExchangeImportPO;
import com.ontology.platform.infrastructure.persistence.ExchangeImportPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Exchange import service for Phase 3a v2 exchange import pipeline.
 * Handles parsing, validation, persistence, and publishing of OntologyExchange documents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExchangeImportService {

    private final ExchangeImportPOMapper mapper;
    private final ObjectMapper objectMapper;

    /**
     * Import a v2 OntologyExchange JSON document.
     *
     * @param jsonDocument   the raw JSON string of the OntologyExchange document
     * @param validationMode strict | warn
     * @return ExchangeImportResponse with import id and status
     */
    public ExchangeImportResponse importExchange(String jsonDocument, String validationMode) {
        if (jsonDocument == null || jsonDocument.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Document must not be empty");
        }

        // Parse JSON and validate basic structure
        JsonNode root;
        try {
            root = objectMapper.readTree(jsonDocument);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "Invalid JSON document: " + e.getMessage());
        }

        // Validate apiVersion
        JsonNode apiVersionNode = root.get("apiVersion");
        if (apiVersionNode == null || !apiVersionNode.asText().startsWith("ontology.platform/")) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Missing or invalid apiVersion; expected ontology.platform/v2");
        }

        // Validate kind
        JsonNode kindNode = root.get("kind");
        if (kindNode == null || !"OntologyExchange".equals(kindNode.asText())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Missing or invalid kind; expected OntologyExchange");
        }

        // Validate metadata
        JsonNode metadataNode = root.get("metadata");
        if (metadataNode == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Missing required field: metadata");
        }

        String metadataId = getTextSafely(metadataNode, "id", UUID.randomUUID().toString());
        String metadataName = getTextSafely(metadataNode, "name", "");
        String metadataVersion = getTextSafely(metadataNode, "version", "0.0.0");
        String metadataSource = getTextSafely(metadataNode, "source", "api");
        String metadataStatus = getTextSafely(metadataNode, "status", "draft");

        // Validate spec / project
        JsonNode specNode = root.get("spec");
        String projectId = null;
        String projectName = null;
        if (specNode != null) {
            JsonNode projectNode = specNode.get("project");
            if (projectNode != null) {
                projectId = getTextSafely(projectNode, "id", null);
                projectName = getTextSafely(projectNode, "name", null);
            }
        }

        // Count entities from dataModel
        int totalEntities = 0;
        int warnings = 0;
        if (specNode != null) {
            JsonNode projectNode = specNode.get("project");
            if (projectNode != null) {
                JsonNode dataModelNode = projectNode.get("dataModel");
                if (dataModelNode != null) {
                    JsonNode entitiesNode = dataModelNode.get("entities");
                    if (entitiesNode != null && entitiesNode.isArray()) {
                        totalEntities = entitiesNode.size();
                    }
                }
            }
        }

        String mode = (validationMode != null && !validationMode.isBlank()) ? validationMode : "strict";

        // Build and save PO
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
                .validationStatus("passed")
                .validationReport("{\"totalEntities\":" + totalEntities + ",\"warnings\":" + warnings + "}")
                .importedAt(Instant.now())
                .createdBy("system")
                .updatedAt(Instant.now())
                .build();

        mapper.insert(po);

        log.info("Exchange import saved: id={}, name={}, version={}, entities={}",
                importId, metadataName, metadataVersion, totalEntities);

        return ExchangeImportResponse.builder()
                .id(importId)
                .status("passed")
                .totalEntities(totalEntities)
                .warnings(warnings)
                .build();
    }

    /**
     * Get the status of an exchange import by ID.
     *
     * @param id the import record ID
     * @return ExchangeImportResponse with current status
     */
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

    /**
     * Publish a validated exchange import.
     *
     * @param id the import record ID
     * @return ExchangeImportResponse with updated status
     */
    public ExchangeImportResponse publishImport(String id) {
        ExchangeImportPO po = mapper.selectById(id);
        if (po == null) {
            throw new ResourceNotFoundException("ExchangeImport", id);
        }
        if (!"passed".equals(po.getValidationStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "Cannot publish import with status: " + po.getValidationStatus());
        }

        po.setMetadataStatus("published");
        po.setPublishedAt(Instant.now());
        po.setUpdatedAt(Instant.now());
        mapper.updateById(po);

        log.info("Exchange import published: id={}", id);

        return ExchangeImportResponse.builder()
                .id(po.getId())
                .status("published")
                .totalEntities(parseTotalEntities(po.getValidationReport()))
                .warnings(parseWarnings(po.getValidationReport()))
                .build();
    }

    // ==================== Private Helpers ====================

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

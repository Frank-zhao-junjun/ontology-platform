package com.ontology.platform.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.service.exchange.ExchangeImportService;
import com.ontology.platform.domain.dto.imports.ExchangeImportResponse;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.infrastructure.imports.Project1JsonToExchangeConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 项目1 本体模型导入 API。
 *
 * <p>接收项目1（Ontology 设计台）导出的 JSON，通过
 * {@link Project1JsonToExchangeConverter} 转换为 v2 交换格式，
 * 再委托 {@link ExchangeImportService} 统一校验与持久化。
 * 支持 autoPublish 一键发布到 V12-V14 领域表。</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Ontology Import", description = "项目1 本体模型导入 API (P01)")
public class OntologyImportController {

    private final Project1JsonToExchangeConverter converter;
    private final ExchangeImportService exchangeImportService;
    private final ObjectMapper objectMapper;

    @PostMapping("/api/v1/ontologies/import")
    @Operation(summary = "导入本体模型",
            description = "接收项目1导出的本体模型 JSON，转换为 v2 交换格式后统一校验+持久化。支持 autoPublish 一键发布")
    public ResponseEntity<ApiResponse<OntologyImportResponse>> importOntology(
            @RequestBody OntologyImportRequest request) {

        String rawContent = request.getRawContent();
        if (rawContent == null || rawContent.isBlank()) {
            return ResponseEntity.status(422)
                    .body(ApiResponse.error(422, "VALIDATION_ERROR: rawContent 不能为空"));
        }

        // ── Parse JSON ──
        JsonNode root;
        try {
            root = objectMapper.readTree(rawContent);
        } catch (Exception e) {
            log.warn("Failed to parse rawContent JSON: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "PARSE_ERROR: JSON 解析失败：" + e.getMessage()));
        }

        // ── Convert Project1 JSON → OntologyExchangeDocument ──
        OntologyExchangeDocument doc = converter.convert(rawContent);
        if (doc == null) {
            return ResponseEntity.status(422)
                    .body(ApiResponse.error(422,
                            "CONVERSION_ERROR: 无法将 Project1 JSON 转换为 v2 交换格式"));
        }
        String exchangeJson;
        try {
            exchangeJson = objectMapper.writeValueAsString(doc);
        } catch (Exception e) {
            return ResponseEntity.status(422)
                    .body(ApiResponse.error(422,
                            "CONVERSION_ERROR: 序列化 v2 交换文档失败：" + e.getMessage()));
        }

        // ── Extract metadata for response ──
        String externalId = doc.getMetadata() != null ? doc.getMetadata().getId() : null;
        String validationMode = request.getValidationMode() != null
                ? request.getValidationMode() : "strict";

        // ── Compute counts from the converted v2 document (format-independent) ──
        Map<String, Integer> counts = computeCounts(doc);

        // ── Delegate to unified v2 pipeline ──
        ExchangeImportResponse importResponse;
        try {
            importResponse = exchangeImportService.importExchange(exchangeJson, validationMode);
        } catch (Exception e) {
            log.error("Exchange import failed: {}", e.getMessage());
            return ResponseEntity.status(422)
                    .body(ApiResponse.error(422, "IMPORT_ERROR: " + e.getMessage()));
        }

        String status = importResponse.getStatus();

        // ── Auto-publish if requested ──
        if (Boolean.TRUE.equals(request.getAutoPublish()) && "passed".equals(status)) {
            try {
                ExchangeImportResponse pubResponse =
                        exchangeImportService.publishImport(importResponse.getId());
                status = pubResponse.getStatus(); // "published"
                log.info("Auto-published import: id={}, status={}", importResponse.getId(), status);
            } catch (Exception e) {
                log.warn("Auto-publish failed for import {}: {}", importResponse.getId(), e.getMessage());
                // Don't fail the import — return "passed" with a note
            }
        }

        OntologyImportResponse response = OntologyImportResponse.builder()
                .draftId(importResponse.getId())
                .externalId(externalId)
                .status(status)
                .totalEntities(importResponse.getTotalEntities())
                .warnings(importResponse.getWarnings())
                .importedCounts(counts)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Compute import counts from the v2 exchange document (format-independent).
     * Works for both raw Project1 JSON and v1 Manifest input formats.
     */
    private Map<String, Integer> computeCounts(OntologyExchangeDocument doc) {
        Map<String, Integer> counts = new HashMap<>();
        OntologyExchangeDocument.OntologyProject project = doc.getSpec() != null
                ? doc.getSpec().getProject() : null;
        if (project == null) return counts;

        // dataModel
        var dataModel = project.getDataModel();
        counts.put("entities", dataModel != null && dataModel.getEntities() != null
                ? dataModel.getEntities().size() : 0);

        // behaviorModel
        var behaviorModel = project.getBehaviorModel();
        counts.put("stateMachines", behaviorModel != null && behaviorModel.getStateMachines() != null
                ? behaviorModel.getStateMachines().size() : 0);
        counts.put("actions", behaviorModel != null && behaviorModel.getActions() != null
                ? behaviorModel.getActions().size() : 0);

        // ruleModel
        var ruleModel = project.getRuleModel();
        counts.put("rules", ruleModel != null && ruleModel.getRules() != null
                ? ruleModel.getRules().size() : 0);

        // eventModel
        var eventModel = project.getEventModel();
        counts.put("events", eventModel != null && eventModel.getEvents() != null
                ? eventModel.getEvents().size() : 0);

        // governanceModel
        var governanceModel = project.getGovernanceModel();
        counts.put("governance", governanceModel != null && governanceModel.getRoles() != null
                ? governanceModel.getRoles().size() : 0);

        // dataSourcesModel
        var dataSourcesModel = project.getDataSourcesModel();
        counts.put("dataSources", dataSourcesModel != null && dataSourcesModel.getSources() != null
                ? dataSourcesModel.getSources().size() : 0);

        // metricsModel
        var metricsModel = project.getMetricsModel();
        counts.put("metrics", metricsModel != null && metricsModel.getMetrics() != null
                ? metricsModel.getMetrics().size() : 0);

        // processModel
        var processModel = project.getProcessModel();
        counts.put("orchestrations", processModel != null && processModel.getOrchestrations() != null
                ? processModel.getOrchestrations().size() : 0);

        // organizationModel
        var orgModel = project.getOrganizationModel();
        counts.put("departments", orgModel != null && orgModel.getDepartments() != null
                ? orgModel.getDepartments().size() : 0);
        counts.put("positions", orgModel != null && orgModel.getPositions() != null
                ? orgModel.getPositions().size() : 0);

        // agentSemanticLayer
        var asl = project.getAgentSemanticLayer();
        counts.put("intents", asl != null && asl.getIntents() != null ? asl.getIntents().size() : 0);
        counts.put("businessTerms", asl != null && asl.getBusinessTerms() != null
                ? asl.getBusinessTerms().size() : 0);

        // epcModel
        var epcModel = project.getEpcModel();
        counts.put("epcChains", epcModel != null && epcModel.getChains() != null
                ? epcModel.getChains().size() : 0);

        return counts;
    }
}

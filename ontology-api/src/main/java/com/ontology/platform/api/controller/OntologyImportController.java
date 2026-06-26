package com.ontology.platform.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.infrastructure.persistence.ManifestImportPO;
import com.ontology.platform.infrastructure.persistence.ManifestImportPOMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 项目1 本体模型导入 API
 * 接收项目1（Ontology 设计台）导出的 JSON，持久化到 manifest_import 表
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Ontology Import", description = "项目1 本体模型导入 API (P01)")
public class OntologyImportController {

    private final ManifestImportPOMapper manifestImportMapper;
    private final ObjectMapper objectMapper;

    @PostMapping("/api/v1/ontologies/import")
    @Operation(summary = "导入本体模型", description = "接收项目1导出的 .ontology-model.json，解析校验后写入 manifest_import 表")
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

        // ── Validate structure ──
        if (!root.has("version") || !root.has("project") || !root.has("entities")) {
            return ResponseEntity.status(422)
                    .body(ApiResponse.error(422,
                            "VALIDATION_ERROR: 模型 JSON 格式不正确，缺少 version / project / entities 字段"));
        }

        // ── Extract identifiers ──
        String version = root.get("version").asText();
        String externalId = root.get("project").has("id")
                ? root.get("project").get("id").asText()
                : root.get("project").get("name").asText("unknown");
        String projectName = root.get("project").has("name")
                ? root.get("project").get("name").asText("")
                : "";

        // ── Compute counts ──
        Map<String, Integer> counts = new HashMap<>();
        counts.put("entities", root.has("entities") ? root.get("entities").size() : 0);
        counts.put("stateMachines", root.has("stateMachines") ? root.get("stateMachines").size() : 0);
        counts.put("rules", root.has("rules") ? root.get("rules").size() : 0);
        counts.put("metrics", root.has("metrics") ? root.get("metrics").size() : 0);
        counts.put("dataSources", root.has("dataSources") ? root.get("dataSources").size() : 0);
        counts.put("businessChain", root.has("businessChain") ? 1 : 0);
        counts.put("governance", root.has("governance") ? 1 : 0);

        // ── Build PO and insert ──
        String draftId = UUID.randomUUID().toString();
        String ontologyId = UUID.randomUUID().toString();

        ManifestImportPO po = ManifestImportPO.builder()
                .id(draftId)
                .ontologyId(ontologyId)
                .externalId(externalId)
                .tenantId("default")
                .status("DRAFT")
                .apiVersion("v1")
                .manifestVersion(version)
                .sourceFormat("JSON")
                .rawContent(rawContent)
                .importedCounts(toJsonString(counts))
                .validationErrors("[]")
                .createdBy(request.getCreatedBy() != null ? request.getCreatedBy() : "system")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        try {
            manifestImportMapper.insert(po);
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate import: externalId={}, version={}", externalId, version);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(409,
                            "DUPLICATE: 该模型版本已存在：" + externalId + " / " + version));
        }

        log.info("Ontology model imported: project={}, version={}, externalId={}, draftId={}, counts={}",
                projectName, version, externalId, draftId, counts);

        OntologyImportResponse response = OntologyImportResponse.builder()
                .draftId(draftId)
                .externalId(externalId)
                .importedCounts(counts)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize to JSON", e);
            return "{}";
        }
    }
}

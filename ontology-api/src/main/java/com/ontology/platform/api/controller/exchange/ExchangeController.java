package com.ontology.platform.api.controller.exchange;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.service.exchange.ExchangeImportService;
import com.ontology.platform.domain.dto.imports.ExchangeImportDocument;
import com.ontology.platform.domain.dto.imports.ExchangeImportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Phase 3a v2 exchange import pipeline.
 * Manages importing, querying, and publishing OntologyExchange documents.
 */
@Slf4j
@RestController
@RequestMapping("/api/v2/exchanges")
@RequiredArgsConstructor
@Tag(name = "Exchange Import", description = "v2 OntologyExchange 导入管理API")
public class ExchangeController {

    private final ExchangeImportService exchangeService;

    /**
     * Import a v2 OntologyExchange JSON document.
     */
    @PostMapping("/import")
    @Operation(summary = "导入 v2 Exchange 文档", description = "解析并导入一个 v2 OntologyExchange JSON 文档")
    public ResponseEntity<ApiResponse<ExchangeImportResponse>> importExchange(
            @Valid @RequestBody ExchangeImportDocument request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId) {

        log.info("REST: Import exchange, userId={}, tenantId={}", userId, tenantId);

        ExchangeImportResponse response = exchangeService.importExchange(
                request.getDocument(), request.getValidationMode());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * Get the status of an exchange import by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询导入状态", description = "获取指定导入记录的状态")
    public ResponseEntity<ApiResponse<ExchangeImportResponse>> getImport(
            @Parameter(description = "导入记录ID")
            @PathVariable("id") String id) {

        log.debug("REST: Get exchange import, id={}", id);

        ExchangeImportResponse response = exchangeService.getImportStatus(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Publish a validated exchange import.
     */
    @PostMapping("/{id}/publish")
    @Operation(summary = "发布导入", description = "发布一个已验证通过的导入文档")
    public ResponseEntity<ApiResponse<ExchangeImportResponse>> publishImport(
            @Parameter(description = "导入记录ID")
            @PathVariable("id") String id) {

        log.info("REST: Publish exchange import, id={}", id);

        ExchangeImportResponse response = exchangeService.publishImport(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

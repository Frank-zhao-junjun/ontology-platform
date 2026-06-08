package com.ontology.platform.api.controller;

import com.ontology.platform.application.dto.*;
import com.ontology.platform.application.service.OntologyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 本体管理API控制器
 * Ontology Management API Controller
 */
@Slf4j
@RestController
@RequestMapping("/v1/ontologies")
@RequiredArgsConstructor
@Tag(name = "Ontology", description = "本体管理API")
public class OntologyController {

    private final OntologyService ontologyService;

    /**
     * 创建本体
     */
    @PostMapping
    @Operation(summary = "创建本体", description = "创建一个新的本体定义")
    public ResponseEntity<ApiResponse<OntologyResponse>> createOntology(
            @Valid @RequestBody CreateOntologyRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        log.info("REST: Create ontology, name={}, userId={}", request.getName(), userId);
        OntologyResponse response = ontologyService.createOntology(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 获取本体详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取本体详情", description = "根据ID获取本体详细信息")
    public ResponseEntity<ApiResponse<OntologyDetailResponse>> getOntology(
            @Parameter(description = "本体ID") @PathVariable String id) {
        log.debug("REST: Get ontology, id={}", id);
        OntologyDetailResponse response = ontologyService.getOntologyById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取本体列表
     */
    @GetMapping
    @Operation(summary = "获取本体列表", description = "分页获取本体列表")
    public ResponseEntity<ApiResponse<List<OntologyResponse>>> listOntologies(
            @Parameter(description = "租户ID") @RequestParam(defaultValue = "default") String tenantId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int pageSize) {
        log.debug("REST: List ontologies, tenantId={}, page={}, pageSize={}", tenantId, page, pageSize);
        List<OntologyResponse> response = ontologyService.listOntologies(tenantId, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 更新本体
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新本体", description = "更新本体的显示名称和描述")
    public ResponseEntity<ApiResponse<OntologyResponse>> updateOntology(
            @Parameter(description = "本体ID") @PathVariable String id,
            @Valid @RequestBody UpdateOntologyRequest request) {
        log.info("REST: Update ontology, id={}", id);
        OntologyResponse response = ontologyService.updateOntology(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 删除本体
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除本体", description = "删除指定的本体")
    public ResponseEntity<ApiResponse<Void>> deleteOntology(
            @Parameter(description = "本体ID") @PathVariable String id) {
        log.info("REST: Delete ontology, id={}", id);
        ontologyService.deleteOntology(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 发布本体
     */
    @PostMapping("/{id}/publish")
    @Operation(summary = "发布本体", description = "将草稿状态的本体发布")
    public ResponseEntity<ApiResponse<OntologyResponse>> publishOntology(
            @Parameter(description = "本体ID") @PathVariable String id) {
        log.info("REST: Publish ontology, id={}", id);
        OntologyResponse response = ontologyService.publishOntology(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 归档本体
     */
    @PostMapping("/{id}/archive")
    @Operation(summary = "归档本体", description = "将本体归档")
    public ResponseEntity<ApiResponse<OntologyResponse>> archiveOntology(
            @Parameter(description = "本体ID") @PathVariable String id) {
        log.info("REST: Archive ontology, id={}", id);
        OntologyResponse response = ontologyService.archiveOntology(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 验证本体
     */
    @PostMapping("/{id}/validate")
    @Operation(summary = "验证本体", description = "验证本体的完整性和一致性")
    public ResponseEntity<ApiResponse<ValidationResultResponse>> validateOntology(
            @Parameter(description = "本体ID") @PathVariable String id) {
        log.info("REST: Validate ontology, id={}", id);
        ValidationResultResponse response = ontologyService.validateOntology(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

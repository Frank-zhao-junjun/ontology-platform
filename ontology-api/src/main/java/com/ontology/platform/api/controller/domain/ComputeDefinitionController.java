package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateComputeDefinitionRequest;
import com.ontology.platform.application.dto.domain.ComputeDefinitionResponse;
import com.ontology.platform.application.service.ComputeDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/ontologies/{ontologyId}/compute")
@RequiredArgsConstructor
@Tag(name = "计算定义", description = "公式计算")
public class ComputeDefinitionController {

    private final ComputeDefinitionService computeDefinitionService;

    @PostMapping
    @Operation(summary = "创建计算定义", description = "在指定本体下创建公式计算")
    public ResponseEntity<ApiResponse<ComputeDefinitionResponse>> create(
            @PathVariable String ontologyId,
            @Valid @RequestBody CreateComputeDefinitionRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        log.info("REST Create ComputeDefinition: ontologyId={}", ontologyId);
        ComputeDefinitionResponse response = computeDefinitionService.create(ontologyId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "获取计算定义列表", description = "获取指定本体下所有公式计算")
    public ResponseEntity<ApiResponse<List<ComputeDefinitionResponse>>> list(@PathVariable String ontologyId) {
        List<ComputeDefinitionResponse> list = computeDefinitionService.listByOntologyId(ontologyId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取计算定义详情", description = "根据ID获取公式计算详细信息")
    public ResponseEntity<ApiResponse<ComputeDefinitionResponse>> getById(@PathVariable String id) {
        ComputeDefinitionResponse response = computeDefinitionService.getById(id);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新计算定义", description = "更新公式计算")
    public ResponseEntity<ApiResponse<ComputeDefinitionResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CreateComputeDefinitionRequest request) {
        ComputeDefinitionResponse response = computeDefinitionService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除计算定义", description = "删除公式计算")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        computeDefinitionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

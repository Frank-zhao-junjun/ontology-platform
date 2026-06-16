package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateQueryDefinitionRequest;
import com.ontology.platform.application.dto.domain.QueryDefinitionResponse;
import com.ontology.platform.application.service.QueryDefinitionService;
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
@RequestMapping("/v1/ontologies/{ontologyId}/queries")
@RequiredArgsConstructor
@Tag(name = "查询定义", description = "数据查询模板")
public class QueryDefinitionController {

    private final QueryDefinitionService queryDefinitionService;

    @PostMapping
    @Operation(summary = "创建查询定义", description = "在指定本体下创建数据查询模板")
    public ResponseEntity<ApiResponse<QueryDefinitionResponse>> create(
            @PathVariable String ontologyId,
            @Valid @RequestBody CreateQueryDefinitionRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        log.info("REST Create QueryDefinition: ontologyId={}", ontologyId);
        QueryDefinitionResponse response = queryDefinitionService.create(ontologyId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "获取查询定义列表", description = "获取指定本体下所有数据查询模板")
    public ResponseEntity<ApiResponse<List<QueryDefinitionResponse>>> list(@PathVariable String ontologyId) {
        List<QueryDefinitionResponse> list = queryDefinitionService.listByOntologyId(ontologyId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取查询定义详情", description = "根据ID获取数据查询模板详细信息")
    public ResponseEntity<ApiResponse<QueryDefinitionResponse>> getById(@PathVariable String id) {
        QueryDefinitionResponse response = queryDefinitionService.getById(id);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新查询定义", description = "更新数据查询模板")
    public ResponseEntity<ApiResponse<QueryDefinitionResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CreateQueryDefinitionRequest request) {
        QueryDefinitionResponse response = queryDefinitionService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除查询定义", description = "删除数据查询模板")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        queryDefinitionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

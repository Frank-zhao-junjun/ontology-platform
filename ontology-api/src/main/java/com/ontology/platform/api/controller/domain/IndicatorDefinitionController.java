package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateIndicatorDefinitionRequest;
import com.ontology.platform.application.dto.domain.IndicatorDefinitionResponse;
import com.ontology.platform.application.service.IndicatorDefinitionService;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/ontologies/{ontologyId}/indicators")
@RequiredArgsConstructor
@Tag(name = "指标定义", description = "业务指标")
public class IndicatorDefinitionController {

    private final IndicatorDefinitionService indicatorDefinitionService;

    @PostMapping
    @Operation(summary = "创建指标定义", description = "在指定本体下创建业务指标")
    public ResponseEntity<ApiResponse<IndicatorDefinitionResponse>> create(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateIndicatorDefinitionRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        log.info("REST Create IndicatorDefinition: ontologyId={}", ontologyId);
        IndicatorDefinitionResponse response = indicatorDefinitionService.create(ontologyId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "获取指标定义列表", description = "获取指定本体下所有业务指标")
    public ResponseEntity<ApiResponse<List<IndicatorDefinitionResponse>>> list(@Parameter(description = "本体ID") @PathVariable String ontologyId) {
        List<IndicatorDefinitionResponse> list = indicatorDefinitionService.listByOntologyId(ontologyId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取指标定义详情", description = "根据ID获取业务指标详细信息")
    public ResponseEntity<ApiResponse<IndicatorDefinitionResponse>> getById(@Parameter(description = "指标定义ID") @PathVariable String id) {
        IndicatorDefinitionResponse response = indicatorDefinitionService.getById(id);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新指标定义", description = "更新业务指标")
    public ResponseEntity<ApiResponse<IndicatorDefinitionResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CreateIndicatorDefinitionRequest request) {
        IndicatorDefinitionResponse response = indicatorDefinitionService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除指标定义", description = "删除业务指标")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "指标定义ID") @PathVariable String id) {
        indicatorDefinitionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

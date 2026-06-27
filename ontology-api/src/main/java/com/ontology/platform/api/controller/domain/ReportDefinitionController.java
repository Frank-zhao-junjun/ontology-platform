package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateReportDefinitionRequest;
import com.ontology.platform.application.dto.domain.ReportDefinitionResponse;
import com.ontology.platform.application.service.ReportDefinitionService;
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
@RequestMapping("/api/v1/ontologies/{ontologyId}/reports")
@RequiredArgsConstructor
@Tag(name = "报表定义", description = "数据报表")
public class ReportDefinitionController {

    private final ReportDefinitionService reportDefinitionService;

    @PostMapping
    @Operation(summary = "创建报表定义", description = "在指定本体下创建数据报表")
    public ResponseEntity<ApiResponse<ReportDefinitionResponse>> create(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateReportDefinitionRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        log.info("REST Create ReportDefinition: ontologyId={}", ontologyId);
        ReportDefinitionResponse response = reportDefinitionService.create(ontologyId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "获取报表定义列表", description = "获取指定本体下所有数据报表")
    public ResponseEntity<ApiResponse<List<ReportDefinitionResponse>>> list(@Parameter(description = "本体ID") @PathVariable String ontologyId) {
        List<ReportDefinitionResponse> list = reportDefinitionService.listByOntologyId(ontologyId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取报表定义详情", description = "根据ID获取数据报表详细信息")
    public ResponseEntity<ApiResponse<ReportDefinitionResponse>> getById(@Parameter(description = "报表定义ID") @PathVariable String id) {
        ReportDefinitionResponse response = reportDefinitionService.getById(id);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新报表定义", description = "更新数据报表")
    public ResponseEntity<ApiResponse<ReportDefinitionResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CreateReportDefinitionRequest request) {
        ReportDefinitionResponse response = reportDefinitionService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除报表定义", description = "删除数据报表")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "报表定义ID") @PathVariable String id) {
        reportDefinitionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

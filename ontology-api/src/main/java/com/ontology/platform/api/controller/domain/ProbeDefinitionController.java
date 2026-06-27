package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateProbeDefinitionRequest;
import com.ontology.platform.application.dto.domain.ProbeDefinitionResponse;
import com.ontology.platform.application.service.ProbeDefinitionService;
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
@RequestMapping("/api/v1/ontologies/{ontologyId}/probes")
@RequiredArgsConstructor
@Tag(name = "探针定义", description = "健康探测")
public class ProbeDefinitionController {

    private final ProbeDefinitionService probeDefinitionService;

    @PostMapping
    @Operation(summary = "创建探针定义", description = "在指定本体下创建健康探测")
    public ResponseEntity<ApiResponse<ProbeDefinitionResponse>> create(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateProbeDefinitionRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        log.info("REST Create ProbeDefinition: ontologyId={}", ontologyId);
        ProbeDefinitionResponse response = probeDefinitionService.create(ontologyId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "获取探针定义列表", description = "获取指定本体下所有健康探测")
    public ResponseEntity<ApiResponse<List<ProbeDefinitionResponse>>> list(@Parameter(description = "本体ID") @PathVariable String ontologyId) {
        List<ProbeDefinitionResponse> list = probeDefinitionService.listByOntologyId(ontologyId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取探针定义详情", description = "根据ID获取健康探测详细信息")
    public ResponseEntity<ApiResponse<ProbeDefinitionResponse>> getById(@Parameter(description = "探针定义ID") @PathVariable String id) {
        ProbeDefinitionResponse response = probeDefinitionService.getById(id);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新探针定义", description = "更新健康探测")
    public ResponseEntity<ApiResponse<ProbeDefinitionResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CreateProbeDefinitionRequest request) {
        ProbeDefinitionResponse response = probeDefinitionService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除探针定义", description = "删除健康探测")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "探针定义ID") @PathVariable String id) {
        probeDefinitionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

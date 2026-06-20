package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateApiDefinitionRequest;
import com.ontology.platform.application.dto.domain.ApiDefinitionResponse;
import com.ontology.platform.application.service.ApiDefinitionService;
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
@RequestMapping("/v1/ontologies/{ontologyId}/apis")
@RequiredArgsConstructor
@Tag(name = "API定义", description = "外部接口")
public class ApiDefinitionController {

    private final ApiDefinitionService apiDefinitionService;

    @PostMapping
    @Operation(summary = "创建API定义", description = "在指定本体下创建外部接口")
    public ResponseEntity<ApiResponse<ApiDefinitionResponse>> create(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateApiDefinitionRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        log.info("REST Create ApiDefinition: ontologyId={}", ontologyId);
        ApiDefinitionResponse response = apiDefinitionService.create(ontologyId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "获取API定义列表", description = "获取指定本体下所有外部接口")
    public ResponseEntity<ApiResponse<List<ApiDefinitionResponse>>> list(@Parameter(description = "本体ID") @PathVariable String ontologyId) {
        List<ApiDefinitionResponse> list = apiDefinitionService.listByOntologyId(ontologyId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取API定义详情", description = "根据ID获取外部接口详细信息")
    public ResponseEntity<ApiResponse<ApiDefinitionResponse>> getById(@Parameter(description = "API定义ID") @PathVariable String id) {
        ApiDefinitionResponse response = apiDefinitionService.getById(id);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新API定义", description = "更新外部接口")
    public ResponseEntity<ApiResponse<ApiDefinitionResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CreateApiDefinitionRequest request) {
        ApiDefinitionResponse response = apiDefinitionService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除API定义", description = "删除外部接口")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "API定义ID") @PathVariable String id) {
        apiDefinitionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

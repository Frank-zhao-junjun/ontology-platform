package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateMetadataTemplateRequest;
import com.ontology.platform.application.dto.domain.MetadataTemplateResponse;
import com.ontology.platform.application.service.MetadataTemplateService;
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
@RequestMapping("/api/v1/ontologies/{ontologyId}/metadata-templates")
@RequiredArgsConstructor
@Tag(name = "元数据模板", description = "元数据模板管理")
public class MetadataTemplateController {

    private final MetadataTemplateService metadataTemplateService;

    @PostMapping
    @Operation(summary = "创建元数据模板")
    public ResponseEntity<ApiResponse<MetadataTemplateResponse>> create(@Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateMetadataTemplateRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(metadataTemplateService.create(ontologyId, request, userId)));
    }

    @GetMapping
    @Operation(summary = "获取元数据模板列表")
    public ResponseEntity<ApiResponse<List<MetadataTemplateResponse>>> list(@Parameter(description = "本体ID") @PathVariable String ontologyId) {
        return ResponseEntity.ok(ApiResponse.success(metadataTemplateService.listByOntologyId(ontologyId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取元数据模板详情")
    public ResponseEntity<ApiResponse<MetadataTemplateResponse>> getById(@Parameter(description = "元数据模板ID") @PathVariable String id) {
        MetadataTemplateResponse response = metadataTemplateService.getById(id);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除元数据模板")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "元数据模板ID") @PathVariable String id) {
        metadataTemplateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateSemanticFieldMappingRequest;
import com.ontology.platform.application.dto.domain.SemanticFieldMappingResponse;
import com.ontology.platform.application.service.SemanticFieldMappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j @RestController
@RequestMapping("/v1/ontologies/{ontologyId}/semantic-field-mappings")
@RequiredArgsConstructor
@Tag(name = "字段映射")
public class SemanticFieldMappingController {
    private final SemanticFieldMappingService semanticFieldMappingService;

    @PostMapping @Operation(summary = "创建字段映射")
    public ResponseEntity<ApiResponse<SemanticFieldMappingResponse>> create(@PathVariable String ontologyId,
            @Valid @RequestBody CreateSemanticFieldMappingRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(semanticFieldMappingService.create(ontologyId, request, userId)));
    }

    @GetMapping @Operation(summary = "列表")
    public ResponseEntity<ApiResponse<List<SemanticFieldMappingResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(semanticFieldMappingService.list()));
    }

    @GetMapping("/{id}") @Operation(summary = "详情")
    public ResponseEntity<ApiResponse<SemanticFieldMappingResponse>> getById(@PathVariable String id) {
        SemanticFieldMappingResponse r = semanticFieldMappingService.getById(id);
        return r == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(ApiResponse.success(r));
    }

    @DeleteMapping("/{id}") @Operation(summary = "删除")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        semanticFieldMappingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

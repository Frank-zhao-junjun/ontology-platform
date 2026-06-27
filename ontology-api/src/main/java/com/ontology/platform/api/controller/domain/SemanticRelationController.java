package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateSemanticRelationRequest;
import com.ontology.platform.application.dto.domain.SemanticRelationResponse;
import com.ontology.platform.application.service.SemanticRelationService;
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

@Slf4j @RestController
@RequestMapping("/api/v1/ontologies/{ontologyId}/semantic-relations")
@RequiredArgsConstructor
@Tag(name = "语义关系")
public class SemanticRelationController {
    private final SemanticRelationService semanticRelationService;

    @PostMapping @Operation(summary = "创建语义关系")
    public ResponseEntity<ApiResponse<SemanticRelationResponse>> create(@Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateSemanticRelationRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(semanticRelationService.create(ontologyId, request, userId)));
    }

    @GetMapping @Operation(summary = "列表")
    public ResponseEntity<ApiResponse<List<SemanticRelationResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(semanticRelationService.list()));
    }

    @GetMapping("/{id}") @Operation(summary = "详情")
    public ResponseEntity<ApiResponse<SemanticRelationResponse>> getById(@Parameter(description = "语义关系ID") @PathVariable String id) {
        SemanticRelationResponse r = semanticRelationService.getById(id);
        return r == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(ApiResponse.success(r));
    }

    @DeleteMapping("/{id}") @Operation(summary = "删除")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "语义关系ID") @PathVariable String id) {
        semanticRelationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

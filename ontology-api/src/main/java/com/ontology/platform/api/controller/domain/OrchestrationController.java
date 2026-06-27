package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateOrchestrationRequest;
import com.ontology.platform.application.dto.domain.OrchestrationResponse;
import com.ontology.platform.application.service.OrchestrationService;
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
@RequestMapping("/api/v1/ontologies/{ontologyId}/orchestrations")
@RequiredArgsConstructor
@Tag(name = "编排", description = "编排管理")
public class OrchestrationController {

    private final OrchestrationService orchestrationService;

    @PostMapping
    @Operation(summary = "创建编排")
    public ResponseEntity<ApiResponse<OrchestrationResponse>> create(@Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateOrchestrationRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(orchestrationService.create(ontologyId, request, userId)));
    }

    @GetMapping
    @Operation(summary = "获取编排列表")
    public ResponseEntity<ApiResponse<List<OrchestrationResponse>>> list(@Parameter(description = "本体ID") @PathVariable String ontologyId) {
        return ResponseEntity.ok(ApiResponse.success(orchestrationService.listByOntologyId(ontologyId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取编排详情")
    public ResponseEntity<ApiResponse<OrchestrationResponse>> getById(@Parameter(description = "编排ID") @PathVariable String id) {
        OrchestrationResponse response = orchestrationService.getById(id);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除编排")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "编排ID") @PathVariable String id) {
        orchestrationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

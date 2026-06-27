package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateProcessStepRequest;
import com.ontology.platform.application.dto.domain.ProcessStepResponse;
import com.ontology.platform.application.service.ProcessStepService;
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
@RequestMapping("/api/v1/ontologies/{ontologyId}/process-steps")
@RequiredArgsConstructor
@Tag(name = "流程步骤", description = "流程步骤管理")
public class ProcessStepController {

    private final ProcessStepService processStepService;

    @PostMapping
    @Operation(summary = "创建流程步骤")
    public ResponseEntity<ApiResponse<ProcessStepResponse>> create(@Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateProcessStepRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(processStepService.create(ontologyId, request, userId)));
    }

    @GetMapping
    @Operation(summary = "获取流程步骤列表")
    public ResponseEntity<ApiResponse<List<ProcessStepResponse>>> list(@Parameter(description = "本体ID") @PathVariable String ontologyId) {
        return ResponseEntity.ok(ApiResponse.success(processStepService.listByOntologyId(ontologyId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取流程步骤详情")
    public ResponseEntity<ApiResponse<ProcessStepResponse>> getById(@Parameter(description = "流程步骤ID") @PathVariable String id) {
        ProcessStepResponse response = processStepService.getById(id);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除流程步骤")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "流程步骤ID") @PathVariable String id) {
        processStepService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateErrorRecoveryRequest;
import com.ontology.platform.application.dto.domain.ErrorRecoveryResponse;
import com.ontology.platform.application.service.ErrorRecoveryService;
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
@RequestMapping("/v1/ontologies/{ontologyId}/error-recoveries")
@RequiredArgsConstructor
@Tag(name = "错误恢复")
public class ErrorRecoveryController {
    private final ErrorRecoveryService errorRecoveryService;

    @PostMapping @Operation(summary = "创建错误恢复")
    public ResponseEntity<ApiResponse<ErrorRecoveryResponse>> create(@Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateErrorRecoveryRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(errorRecoveryService.create(ontologyId, request, userId)));
    }

    @GetMapping @Operation(summary = "列表")
    public ResponseEntity<ApiResponse<List<ErrorRecoveryResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(errorRecoveryService.list()));
    }

    @GetMapping("/{id}") @Operation(summary = "详情")
    public ResponseEntity<ApiResponse<ErrorRecoveryResponse>> getById(@Parameter(description = "容错策略ID") @PathVariable String id) {
        ErrorRecoveryResponse r = errorRecoveryService.getById(id);
        return r == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(ApiResponse.success(r));
    }

    @DeleteMapping("/{id}") @Operation(summary = "删除")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "容错策略ID") @PathVariable String id) {
        errorRecoveryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateGuardrailRuleRequest;
import com.ontology.platform.application.dto.domain.GuardrailRuleResponse;
import com.ontology.platform.application.service.GuardrailRuleService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/v1/ontologies/{ontologyId}/guardrails")
@RequiredArgsConstructor
@Tag(name = "护栏规则", description = "安全护栏")
public class GuardrailRuleController {

    private final GuardrailRuleService guardrailRuleService;

    @PostMapping
    @Operation(summary = "创建护栏规则", description = "在指定本体下创建安全护栏")
    public ResponseEntity<ApiResponse<GuardrailRuleResponse>> create(
            @PathVariable String ontologyId,
            @Valid @RequestBody CreateGuardrailRuleRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        log.info("REST Create GuardrailRule: ontologyId={}", ontologyId);
        GuardrailRuleResponse response = guardrailRuleService.create(ontologyId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "获取护栏规则列表", description = "获取指定本体下所有安全护栏")
    public ResponseEntity<ApiResponse<List<GuardrailRuleResponse>>> list(@PathVariable String ontologyId) {
        List<GuardrailRuleResponse> list = guardrailRuleService.listByOntologyId(ontologyId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取护栏规则详情", description = "根据ID获取安全护栏详细信息")
    public ResponseEntity<ApiResponse<GuardrailRuleResponse>> getById(@PathVariable String id) {
        GuardrailRuleResponse response = guardrailRuleService.getById(id);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新护栏规则", description = "更新安全护栏")
    public ResponseEntity<ApiResponse<GuardrailRuleResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CreateGuardrailRuleRequest request) {
        GuardrailRuleResponse response = guardrailRuleService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除护栏规则", description = "删除安全护栏")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        guardrailRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateValidationRuleRequest;
import com.ontology.platform.application.dto.domain.ValidationRuleResponse;
import com.ontology.platform.application.service.ValidationRuleService;
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
@RequestMapping("/v1/ontologies/{ontologyId}/validations")
@RequiredArgsConstructor
@Tag(name = "校验规则", description = "规则验证定义")
public class ValidationRuleController {

    private final ValidationRuleService validationRuleService;

    @PostMapping
    @Operation(summary = "创建校验规则", description = "在指定本体下创建规则验证定义")
    public ResponseEntity<ApiResponse<ValidationRuleResponse>> create(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateValidationRuleRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        log.info("REST Create ValidationRule: ontologyId={}", ontologyId);
        ValidationRuleResponse response = validationRuleService.create(ontologyId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "获取校验规则列表", description = "获取指定本体下所有规则验证定义")
    public ResponseEntity<ApiResponse<List<ValidationRuleResponse>>> list(@Parameter(description = "本体ID") @PathVariable String ontologyId) {
        List<ValidationRuleResponse> list = validationRuleService.listByOntologyId(ontologyId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取校验规则详情", description = "根据ID获取规则验证定义详细信息")
    public ResponseEntity<ApiResponse<ValidationRuleResponse>> getById(@Parameter(description = "校验规则ID") @PathVariable String id) {
        ValidationRuleResponse response = validationRuleService.getById(id);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新校验规则", description = "更新规则验证定义")
    public ResponseEntity<ApiResponse<ValidationRuleResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CreateValidationRuleRequest request) {
        ValidationRuleResponse response = validationRuleService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除校验规则", description = "删除规则验证定义")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "校验规则ID") @PathVariable String id) {
        validationRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

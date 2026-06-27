package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreatePolicyRuleRequest;
import com.ontology.platform.application.dto.domain.PolicyRuleResponse;
import com.ontology.platform.application.service.PolicyRuleService;
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
@RequestMapping("/api/v1/ontologies/{ontologyId}/policies")
@RequiredArgsConstructor
@Tag(name = "策略规则", description = "访问策略")
public class PolicyRuleController {

    private final PolicyRuleService policyRuleService;

    @PostMapping
    @Operation(summary = "创建策略规则", description = "在指定本体下创建访问策略")
    public ResponseEntity<ApiResponse<PolicyRuleResponse>> create(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreatePolicyRuleRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        log.info("REST Create PolicyRule: ontologyId={}", ontologyId);
        PolicyRuleResponse response = policyRuleService.create(ontologyId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "获取策略规则列表", description = "获取指定本体下所有访问策略")
    public ResponseEntity<ApiResponse<List<PolicyRuleResponse>>> list(@Parameter(description = "本体ID") @PathVariable String ontologyId) {
        List<PolicyRuleResponse> list = policyRuleService.listByOntologyId(ontologyId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取策略规则详情", description = "根据ID获取访问策略详细信息")
    public ResponseEntity<ApiResponse<PolicyRuleResponse>> getById(@Parameter(description = "策略规则ID") @PathVariable String id) {
        PolicyRuleResponse response = policyRuleService.getById(id);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新策略规则", description = "更新访问策略")
    public ResponseEntity<ApiResponse<PolicyRuleResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CreatePolicyRuleRequest request) {
        PolicyRuleResponse response = policyRuleService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除策略规则", description = "删除访问策略")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "策略规则ID") @PathVariable String id) {
        policyRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

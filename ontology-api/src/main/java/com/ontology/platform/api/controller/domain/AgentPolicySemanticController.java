package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateAgentPolicySemanticRequest;
import com.ontology.platform.application.dto.domain.AgentPolicySemanticResponse;
import com.ontology.platform.application.service.AgentPolicySemanticService;
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
@RequestMapping("/api/v1/ontologies/{ontologyId}/agent-policies")
@RequiredArgsConstructor
@Tag(name = "Agent策略")
public class AgentPolicySemanticController {
    private final AgentPolicySemanticService agentPolicySemanticService;

    @PostMapping @Operation(summary = "创建Agent策略")
    public ResponseEntity<ApiResponse<AgentPolicySemanticResponse>> create(@Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateAgentPolicySemanticRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(agentPolicySemanticService.create(ontologyId, request, userId)));
    }

    @GetMapping @Operation(summary = "列表")
    public ResponseEntity<ApiResponse<List<AgentPolicySemanticResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(agentPolicySemanticService.list()));
    }

    @GetMapping("/{id}") @Operation(summary = "详情")
    public ResponseEntity<ApiResponse<AgentPolicySemanticResponse>> getById(@Parameter(description = "Agent策略ID") @PathVariable String id) {
        AgentPolicySemanticResponse r = agentPolicySemanticService.getById(id);
        return r == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(ApiResponse.success(r));
    }

    @DeleteMapping("/{id}") @Operation(summary = "删除")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "Agent策略ID") @PathVariable String id) {
        agentPolicySemanticService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

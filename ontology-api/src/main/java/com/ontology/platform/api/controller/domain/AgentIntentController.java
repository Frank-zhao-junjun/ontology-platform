package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateAgentIntentRequest;
import com.ontology.platform.application.dto.domain.AgentIntentResponse;
import com.ontology.platform.application.service.AgentIntentService;
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
@RequestMapping("/v1/ontologies/{ontologyId}/agent-intents")
@RequiredArgsConstructor
@Tag(name = "Agent意图", description = "Agent意图管理")
public class AgentIntentController {

    private final AgentIntentService agentIntentService;

    @PostMapping
    @Operation(summary = "创建Agent意图")
    public ResponseEntity<ApiResponse<AgentIntentResponse>> create(@Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Valid @RequestBody CreateAgentIntentRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(agentIntentService.create(ontologyId, request, userId)));
    }

    @GetMapping
    @Operation(summary = "获取Agent意图列表")
    public ResponseEntity<ApiResponse<List<AgentIntentResponse>>> list(@Parameter(description = "本体ID") @PathVariable String ontologyId) {
        return ResponseEntity.ok(ApiResponse.success(agentIntentService.listByOntologyId(ontologyId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取Agent意图详情")
    public ResponseEntity<ApiResponse<AgentIntentResponse>> getById(@Parameter(description = "Agent意图ID") @PathVariable String id) {
        AgentIntentResponse response = agentIntentService.getById(id);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除Agent意图")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "Agent意图ID") @PathVariable String id) {
        agentIntentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

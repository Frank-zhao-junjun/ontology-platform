package com.ontology.platform.api.controller.agent;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.agent.AgentInfoResponse;
import com.ontology.platform.application.dto.agent.AgentTaskResponse;
import com.ontology.platform.application.dto.agent.SubmitAgentTaskRequest;
import com.ontology.platform.application.service.agent.AgentOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
@Tag(name = "Agent Orchestration", description = "Agent 编排集成 — ACP 协议接入 Kimi/Claude/Codex")
public class AgentController {

    private final AgentOrchestrationService agentOrchestrationService;

    @PostMapping("/tasks")
    @Operation(summary = "提交 Agent 任务", description = "将任务转发到指定的 Agent CLI 执行（kimi/claude/codex）")
    public ResponseEntity<ApiResponse<AgentTaskResponse>> submitTask(
            @Valid @RequestBody SubmitAgentTaskRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "admin") String userId) {

        log.info("REST: Submit agent task, type={}, userId={}", request.getAgentType(), userId);
        AgentTaskResponse response = agentOrchestrationService.executeTask(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "列出可用 Agent", description = "返回当前系统支持的 Agent 列表")
    public ResponseEntity<ApiResponse<List<AgentInfoResponse>>> listAgents() {
        List<AgentInfoResponse> agents = agentOrchestrationService.listAgents();
        return ResponseEntity.ok(ApiResponse.success(agents));
    }
}

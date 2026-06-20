package com.ontology.platform.application.service.agent;

import com.ontology.platform.application.dto.agent.AgentInfoResponse;
import com.ontology.platform.application.dto.agent.AgentTaskResponse;
import com.ontology.platform.application.dto.agent.SubmitAgentTaskRequest;
import com.ontology.platform.application.service.agent.AgentBridgePort.AgentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Agent 编排服务 — 接收任务请求，路由到对应 Agent CLI，返回结果。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentOrchestrationService {

    private final AgentBridgePort bridgeService;

    @Value("${agent.project-root:D:\\AI\\ontology-platform}")
    private String projectRoot;

    public AgentTaskResponse executeTask(SubmitAgentTaskRequest request, String userId) {
        String agentType = request.getAgentType().toLowerCase().trim();
        String prompt = request.getPrompt();
        String cwd = request.getCwd() != null ? request.getCwd() : projectRoot;
        Long timeout = request.getTimeout() != null ? request.getTimeout().longValue() : null;

        log.info("Agent task: type={}, prompt(len)={}, cwd={}, userId={}", agentType, prompt.length(), cwd, userId);

        AgentResult result = switch (agentType) {
            case "kimi" -> bridgeService.executeKimi(prompt, cwd, timeout);
            case "claude" -> bridgeService.executeClaude(prompt, cwd, timeout, request.getMaxTurns(), request.getModel());
            case "codex" -> bridgeService.executeCodex(prompt, cwd, timeout);
            default -> AgentResult.builder()
                    .status("FAILURE")
                    .errorMessage("不支持的 agent 类型: " + agentType + "，支持: kimi, claude, codex")
                    .build();
        };

        return AgentTaskResponse.builder()
                .agentType(agentType)
                .status(result.getStatus())
                .output(result.getOutput())
                .errorMessage(result.getErrorMessage())
                .durationMs(result.getDurationMs())
                .build();
    }

    public List<AgentInfoResponse> listAgents() {
        return List.of(
                AgentInfoResponse.builder().agentType("kimi").available(true)
                        .description("Kimi Code CLI — 代码生成与审查").build(),
                AgentInfoResponse.builder().agentType("claude").available(true)
                        .description("Claude Code CLI — 深度推理与代码审查").build(),
                AgentInfoResponse.builder().agentType("codex").available(true)
                        .description("Codex CLI — 批量代码生成").build()
        );
    }
}

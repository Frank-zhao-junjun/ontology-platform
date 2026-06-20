package com.ontology.platform.application.service.agent;

import com.ontology.platform.application.dto.agent.AgentInfoResponse;
import com.ontology.platform.application.dto.agent.AgentTaskResponse;
import com.ontology.platform.application.dto.agent.SubmitAgentTaskRequest;
import com.ontology.platform.infrastructure.bridge.AgentBridgeService;
import com.ontology.platform.infrastructure.bridge.AgentBridgeService.AgentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentOrchestrationServiceTest {

    @Mock
    private AgentBridgeService bridgeService;

    private AgentOrchestrationService service;

    @BeforeEach
    void setUp() {
        service = new AgentOrchestrationService(bridgeService);
    }

    @Test
    void executeTask_kimi_shouldRouteCorrectly() {
        var request = SubmitAgentTaskRequest.builder()
                .agentType("kimi")
                .prompt("测试任务")
                .build();
        when(bridgeService.executeKimi(eq("测试任务"), any(), isNull()))
                .thenReturn(AgentResult.builder()
                        .status("SUCCESS")
                        .output("完成")
                        .durationMs(1000L)
                        .build());

        AgentTaskResponse resp = service.executeTask(request, "admin");

        assertEquals("kimi", resp.getAgentType());
        assertEquals("SUCCESS", resp.getStatus());
        assertEquals("完成", resp.getOutput());
    }

    @Test
    void executeTask_claude_shouldRouteCorrectly() {
        var request = SubmitAgentTaskRequest.builder()
                .agentType("claude")
                .prompt("审查代码")
                .maxTurns(30)
                .build();
        when(bridgeService.executeClaude(eq("审查代码"), any(), isNull(), eq(30), isNull()))
                .thenReturn(AgentResult.builder()
                        .status("SUCCESS")
                        .output("OK")
                        .durationMs(5000L)
                        .build());

        AgentTaskResponse resp = service.executeTask(request, "admin");

        assertEquals("claude", resp.getAgentType());
        assertEquals("SUCCESS", resp.getStatus());
    }

    @Test
    void executeTask_codex_shouldRouteCorrectly() {
        var request = SubmitAgentTaskRequest.builder()
                .agentType("codex")
                .prompt("生成代码")
                .build();
        when(bridgeService.executeCodex(eq("生成代码"), any(), isNull()))
                .thenReturn(AgentResult.builder()
                        .status("SUCCESS")
                        .output("代码生成结果")
                        .durationMs(30000L)
                        .build());

        AgentTaskResponse resp = service.executeTask(request, "admin");

        assertEquals("codex", resp.getAgentType());
        assertEquals("SUCCESS", resp.getStatus());
    }

    @Test
    void executeTask_unsupportedAgent_shouldReturnFailure() {
        var request = SubmitAgentTaskRequest.builder()
                .agentType("unknown")
                .prompt("测试")
                .build();

        AgentTaskResponse resp = service.executeTask(request, "admin");

        assertEquals("FAILURE", resp.getStatus());
        assertTrue(resp.getErrorMessage().contains("不支持的 agent 类型"));
    }

    @Test
    void executeTask_failure_shouldPropagate() {
        var request = SubmitAgentTaskRequest.builder()
                .agentType("kimi")
                .prompt("会失败的任务")
                .build();
        when(bridgeService.executeKimi(any(), any(), isNull()))
                .thenReturn(AgentResult.builder()
                        .status("FAILURE")
                        .errorMessage("脚本执行失败")
                        .durationMs(500L)
                        .build());

        AgentTaskResponse resp = service.executeTask(request, "admin");

        assertEquals("FAILURE", resp.getStatus());
        assertEquals("脚本执行失败", resp.getErrorMessage());
    }

    @Test
    void executeTask_timeout_shouldPropagate() {
        var request = SubmitAgentTaskRequest.builder()
                .agentType("kimi")
                .prompt("超时任务")
                .timeout(5)
                .build();
        when(bridgeService.executeKimi(any(), any(), eq(5L)))
                .thenReturn(AgentResult.builder()
                        .status("TIMEOUT")
                        .errorMessage("执行超时(>5s)")
                        .output("部分输出")
                        .durationMs(5000L)
                        .build());

        AgentTaskResponse resp = service.executeTask(request, "admin");

        assertEquals("TIMEOUT", resp.getStatus());
        assertNotNull(resp.getOutput());
    }

    @Test
    void listAgents_shouldReturnAll() {
        List<AgentInfoResponse> agents = service.listAgents();
        assertEquals(3, agents.size());
        var types = agents.stream().map(AgentInfoResponse::getAgentType).toList();
        assertTrue(types.containsAll(List.of("kimi", "claude", "codex")));
    }
}

package com.ontology.platform.api.controller.agent;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.agent.AgentInfoResponse;
import com.ontology.platform.application.dto.agent.AgentTaskResponse;
import com.ontology.platform.application.dto.agent.SubmitAgentTaskRequest;
import com.ontology.platform.application.service.agent.AgentOrchestrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentControllerTest {

    @Mock
    private AgentOrchestrationService agentOrchestrationService;

    private AgentController controller;

    @BeforeEach
    void setUp() {
        controller = new AgentController(agentOrchestrationService);
    }

    @Test
    void submitTask_kimi_shouldReturnResult() {
        var request = SubmitAgentTaskRequest.builder()
                .agentType("kimi")
                .prompt("列出项目目录结构")
                .build();
        var expected = AgentTaskResponse.builder()
                .agentType("kimi")
                .status("SUCCESS")
                .output("src/main/java/...")
                .durationMs(1500L)
                .build();
        when(agentOrchestrationService.executeTask(any(), anyString())).thenReturn(expected);

        ResponseEntity<ApiResponse<AgentTaskResponse>> resp = controller.submitTask(request, "admin");

        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertEquals(0, resp.getBody().getCode());
        assertEquals("kimi", resp.getBody().getData().getAgentType());
        assertEquals("SUCCESS", resp.getBody().getData().getStatus());
    }

    @Test
    void submitTask_claude_shouldReturnResult() {
        var request = SubmitAgentTaskRequest.builder()
                .agentType("claude")
                .prompt("审查这段代码")
                .maxTurns(30)
                .build();
        var expected = AgentTaskResponse.builder()
                .agentType("claude")
                .status("SUCCESS")
                .output("代码审查结果...")
                .durationMs(45000L)
                .build();
        when(agentOrchestrationService.executeTask(any(), anyString())).thenReturn(expected);

        ResponseEntity<ApiResponse<AgentTaskResponse>> resp = controller.submitTask(request, "admin");

        assertEquals(200, resp.getStatusCode().value());
        assertEquals("claude", resp.getBody().getData().getAgentType());
    }

    @Test
    void submitTask_failure_shouldReturnError() {
        var request = SubmitAgentTaskRequest.builder()
                .agentType("kimi")
                .prompt("不存在的命令")
                .build();
        var expected = AgentTaskResponse.builder()
                .agentType("kimi")
                .status("FAILURE")
                .errorMessage("Command not found")
                .durationMs(200L)
                .build();
        when(agentOrchestrationService.executeTask(any(), anyString())).thenReturn(expected);

        ResponseEntity<ApiResponse<AgentTaskResponse>> resp = controller.submitTask(request, "admin");

        assertFalse(resp.getBody().getData().getStatus().equals("SUCCESS"));
        assertNotNull(resp.getBody().getData().getErrorMessage());
    }

    @Test
    void listAgents_shouldReturnThree() {
        var agents = List.of(
                AgentInfoResponse.builder().agentType("kimi").available(true).build(),
                AgentInfoResponse.builder().agentType("claude").available(true).build(),
                AgentInfoResponse.builder().agentType("codex").available(true).build()
        );
        when(agentOrchestrationService.listAgents()).thenReturn(agents);

        ResponseEntity<ApiResponse<List<AgentInfoResponse>>> resp = controller.listAgents();

        assertEquals(3, resp.getBody().getData().size());
    }
}

package com.ontology.platform.api.controller.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.agent.AgentInfoResponse;
import com.ontology.platform.application.dto.agent.AgentTaskResponse;
import com.ontology.platform.application.dto.agent.SubmitAgentTaskRequest;
import com.ontology.platform.application.service.agent.AgentOrchestrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AgentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AgentOrchestrationService agentOrchestrationService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        var controller = new AgentController(agentOrchestrationService);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void submitTask_kimi_shouldReturnResult() throws Exception {
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

        mockMvc.perform(post("/api/v1/agents/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "admin")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.agentType").value("kimi"))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    void submitTask_claude_shouldReturnResult() throws Exception {
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

        mockMvc.perform(post("/api/v1/agents/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "admin")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.agentType").value("claude"));
    }

    @Test
    void submitTask_failure_shouldReturnError() throws Exception {
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

        mockMvc.perform(post("/api/v1/agents/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "admin")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FAILURE"))
                .andExpect(jsonPath("$.data.errorMessage").value("Command not found"));
    }

    @Test
    void listAgents_shouldReturnThree() throws Exception {
        var agents = List.of(
                AgentInfoResponse.builder().agentType("kimi").available(true).build(),
                AgentInfoResponse.builder().agentType("claude").available(true).build(),
                AgentInfoResponse.builder().agentType("codex").available(true).build()
        );
        when(agentOrchestrationService.listAgents()).thenReturn(agents);

        mockMvc.perform(get("/api/v1/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));
    }
}

package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.config.DomainTestConfig;
import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateAgentPolicySemanticRequest;
import com.ontology.platform.application.dto.domain.AgentPolicySemanticResponse;
import com.ontology.platform.application.service.AgentPolicySemanticService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AgentPolicySemanticController.class)
@Import(DomainTestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class AgentPolicySemanticControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgentPolicySemanticService agentPolicySemanticService;

    @Test
    void create_shouldReturn201() throws Exception {
        AgentPolicySemanticResponse resp = AgentPolicySemanticResponse.builder().id("test-id").build();
        when(agentPolicySemanticService.create(any(), any(), any())).thenReturn(resp);

        mockMvc.perform(post("/v1/ontologies/onto-1/agent-policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void list_shouldReturn200() throws Exception {
        mockMvc.perform(get("/v1/ontologies/onto-1/agent-policies"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturn200_whenFound() throws Exception {
        AgentPolicySemanticResponse resp = AgentPolicySemanticResponse.builder().id("id-1").build();
        when(agentPolicySemanticService.getById("id-1")).thenReturn(resp);

        mockMvc.perform(get("/v1/ontologies/onto-1/agent-policies/id-1"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(agentPolicySemanticService.getById("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/v1/ontologies/onto-1/agent-policies/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/v1/ontologies/onto-1/agent-policies/id-1"))
                .andExpect(status().isNoContent());
    }
}

package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.config.DomainTestConfig;
import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateEpcNodeRequest;
import com.ontology.platform.application.dto.domain.EpcNodeResponse;
import com.ontology.platform.application.service.EpcNodeService;
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

@WebMvcTest(EpcNodeController.class)
@Import(DomainTestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class EpcNodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EpcNodeService epcNodeService;

    @Test
    void create_shouldReturn201() throws Exception {
        EpcNodeResponse resp = EpcNodeResponse.builder().id("test-id").build();
        when(epcNodeService.create(any(), any(), any())).thenReturn(resp);

        mockMvc.perform(post("/v1/ontologies/onto-1/epc/nodes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void list_shouldReturn200() throws Exception {
        mockMvc.perform(get("/v1/ontologies/onto-1/epc/nodes"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturn200_whenFound() throws Exception {
        EpcNodeResponse resp = EpcNodeResponse.builder().id("id-1").build();
        when(epcNodeService.getById("id-1")).thenReturn(resp);

        mockMvc.perform(get("/v1/ontologies/onto-1/epc/nodes/id-1"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(epcNodeService.getById("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/v1/ontologies/onto-1/epc/nodes/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/v1/ontologies/onto-1/epc/nodes/id-1"))
                .andExpect(status().isNoContent());
    }
}

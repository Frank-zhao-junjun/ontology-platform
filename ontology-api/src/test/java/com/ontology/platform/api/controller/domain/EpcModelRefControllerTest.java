package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateEpcModelRefRequest;
import com.ontology.platform.application.dto.domain.EpcModelRefResponse;
import com.ontology.platform.application.service.EpcModelRefService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EpcModelRefController.class)
@AutoConfigureMockMvc(addFilters = false)
class EpcModelRefControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EpcModelRefService epcModelRefService;

    @Test
    void create_shouldReturn201() throws Exception {
        EpcModelRefResponse resp = EpcModelRefResponse.builder().id("test-id").build();
        when(epcModelRefService.create(any(), any(), any())).thenReturn(resp);

        mockMvc.perform(post("/v1/ontologies/onto-1/epc/model-refs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void list_shouldReturn200() throws Exception {
        mockMvc.perform(get("/v1/ontologies/onto-1/epc/model-refs"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturn200_whenFound() throws Exception {
        EpcModelRefResponse resp = EpcModelRefResponse.builder().id("id-1").build();
        when(epcModelRefService.getById("id-1")).thenReturn(resp);

        mockMvc.perform(get("/v1/ontologies/onto-1/epc/model-refs/id-1"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(epcModelRefService.getById("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/v1/ontologies/onto-1/epc/model-refs/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/v1/ontologies/onto-1/epc/model-refs/id-1"))
                .andExpect(status().isNoContent());
    }
}

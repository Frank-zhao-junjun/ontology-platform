package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateEpcChainRequest;
import com.ontology.platform.application.dto.domain.EpcChainResponse;
import com.ontology.platform.application.service.EpcChainService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EpcChainController.class)
class EpcChainControllerTest {{

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EpcChainService epcChainService;

    @Test
    void create_shouldReturn201() throws Exception {{
        EpcChainResponse resp = EpcChainResponse.builder().id("test-id").build();
        when(epcChainService.create(any(), any(), any())).thenReturn(resp);

        mockMvc.perform(post("/v1/ontologies/onto-1/epc/chains")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{{}}"))
                .andExpect(status().isCreated());
    }}

    @Test
    void list_shouldReturn200() throws Exception {{
        mockMvc.perform(get("/v1/ontologies/onto-1/epc/chains"))
                .andExpect(status().isOk());
    }}

    @Test
    void getById_shouldReturn200_whenFound() throws Exception {{
        EpcChainResponse resp = EpcChainResponse.builder().id("id-1").build();
        when(epcChainService.getById("id-1")).thenReturn(resp);

        mockMvc.perform(get("/v1/ontologies/onto-1/epc/chains/id-1"))
                .andExpect(status().isOk());
    }}

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {{
        when(epcChainService.getById("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/v1/ontologies/onto-1/epc/chains/nonexistent"))
                .andExpect(status().isNotFound());
    }}

    @Test
    void delete_shouldReturn204() throws Exception {{
        mockMvc.perform(delete("/v1/ontologies/onto-1/epc/chains/id-1"))
                .andExpect(status().isNoContent());
    }}
}}

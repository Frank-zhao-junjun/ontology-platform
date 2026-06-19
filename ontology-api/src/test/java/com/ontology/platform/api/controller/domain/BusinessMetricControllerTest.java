package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.config.DomainTestConfig;
import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateBusinessMetricRequest;
import com.ontology.platform.application.dto.domain.BusinessMetricResponse;
import com.ontology.platform.application.service.BusinessMetricService;
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

@WebMvcTest(BusinessMetricController.class)
@Import(DomainTestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class BusinessMetricControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BusinessMetricService businessMetricService;

    @Test
    void create_shouldReturn201() throws Exception {
        BusinessMetricResponse resp = BusinessMetricResponse.builder().id("test-id").build();
        when(businessMetricService.create(any(), any(), any())).thenReturn(resp);

        mockMvc.perform(post("/v1/ontologies/onto-1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void list_shouldReturn200() throws Exception {
        mockMvc.perform(get("/v1/ontologies/onto-1/metrics"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturn200_whenFound() throws Exception {
        BusinessMetricResponse resp = BusinessMetricResponse.builder().id("id-1").build();
        when(businessMetricService.getById("id-1")).thenReturn(resp);

        mockMvc.perform(get("/v1/ontologies/onto-1/metrics/id-1"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(businessMetricService.getById("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/v1/ontologies/onto-1/metrics/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/v1/ontologies/onto-1/metrics/id-1"))
                .andExpect(status().isNoContent());
    }
}

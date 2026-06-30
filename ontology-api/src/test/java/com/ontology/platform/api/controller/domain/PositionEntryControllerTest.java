package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreatePositionEntryRequest;
import com.ontology.platform.application.dto.domain.PositionEntryResponse;
import com.ontology.platform.application.service.PositionEntryService;
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

@WebMvcTest(PositionEntryController.class)
@AutoConfigureMockMvc(addFilters = false)
class PositionEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PositionEntryService positionEntryService;

    @Test
    void create_shouldReturn201() throws Exception {
        PositionEntryResponse resp = PositionEntryResponse.builder().id("test-id").build();
        when(positionEntryService.create(any(), any(), any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/ontologies/onto-1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void list_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/ontologies/onto-1/positions"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturn200_whenFound() throws Exception {
        PositionEntryResponse resp = PositionEntryResponse.builder().id("id-1").build();
        when(positionEntryService.getById("id-1")).thenReturn(resp);

        mockMvc.perform(get("/api/v1/ontologies/onto-1/positions/id-1"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(positionEntryService.getById("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/api/v1/ontologies/onto-1/positions/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/ontologies/onto-1/positions/id-1"))
                .andExpect(status().isNoContent());
    }
}

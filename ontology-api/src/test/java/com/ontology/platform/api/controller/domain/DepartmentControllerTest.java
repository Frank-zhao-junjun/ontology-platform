package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateDepartmentRequest;
import com.ontology.platform.application.dto.domain.DepartmentResponse;
import com.ontology.platform.application.service.DepartmentService;
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

@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {{

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartmentService departmentService;

    @Test
    void create_shouldReturn201() throws Exception {{
        DepartmentResponse resp = DepartmentResponse.builder().id("test-id").build();
        when(departmentService.create(any(), any(), any())).thenReturn(resp);

        mockMvc.perform(post("/v1/ontologies/onto-1/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{{}}"))
                .andExpect(status().isCreated());
    }}

    @Test
    void list_shouldReturn200() throws Exception {{
        mockMvc.perform(get("/v1/ontologies/onto-1/departments"))
                .andExpect(status().isOk());
    }}

    @Test
    void getById_shouldReturn200_whenFound() throws Exception {{
        DepartmentResponse resp = DepartmentResponse.builder().id("id-1").build();
        when(departmentService.getById("id-1")).thenReturn(resp);

        mockMvc.perform(get("/v1/ontologies/onto-1/departments/id-1"))
                .andExpect(status().isOk());
    }}

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {{
        when(departmentService.getById("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/v1/ontologies/onto-1/departments/nonexistent"))
                .andExpect(status().isNotFound());
    }}

    @Test
    void delete_shouldReturn204() throws Exception {{
        mockMvc.perform(delete("/v1/ontologies/onto-1/departments/id-1"))
                .andExpect(status().isNoContent());
    }}
}}

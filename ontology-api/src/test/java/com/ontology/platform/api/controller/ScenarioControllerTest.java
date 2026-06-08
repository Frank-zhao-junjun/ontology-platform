package com.ontology.platform.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * S02: Business Scenario CRUD tests.
 * Tests cover AC-1 (create), AC-2 (list/apply), AC-3 (update), AC-4 (applicable types).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ScenarioControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper mapper;

    private static String contextId;
    private static String scenarioId;

    @Test @Order(1)
    void createContext() throws Exception {
        String body = mapper.writeValueAsString(java.util.Map.of(
                "name", "生产制造P1", "code", "mfg-p1", "description", "P1测试上下文"));
        String resp = mockMvc.perform(post("/v1/contexts")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        contextId = (String) mapper.readTree(resp).get("id").asText();
    }

    @Test @Order(2)
    void createScenario() throws Exception {
        String body = mapper.writeValueAsString(java.util.Map.of(
                "name", "面向库存生产", "code", "MTS",
                "nameEn", "MakeToStock", "description", "按库存预测驱动"));
        String resp = mockMvc.perform(post("/v1/contexts/" + contextId + "/scenarios")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("面向库存生产"))
                .andExpect(jsonPath("$.code").value("MTS"))
                .andReturn().getResponse().getContentAsString();
        scenarioId = (String) mapper.readTree(resp).get("id").asText();
    }

    @Test @Order(3)
    void createSecondScenario() throws Exception {
        String body = mapper.writeValueAsString(java.util.Map.of(
                "name", "面向订单生产", "code", "MTO",
                "nameEn", "MakeToOrder", "description", "按销售订单驱动"));
        mockMvc.perform(post("/v1/contexts/" + contextId + "/scenarios")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("MTO"));
    }

    @Test @Order(4)
    void rejectDuplicateCode() throws Exception {
        String body = mapper.writeValueAsString(java.util.Map.of(
                "name", "重复MTS", "code", "MTS", "nameEn", "Dup"));
        mockMvc.perform(post("/v1/contexts/" + contextId + "/scenarios")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(5)
    void listScenarios() throws Exception {
        mockMvc.perform(get("/v1/contexts/" + contextId + "/scenarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test @Order(6)
    void updateScenario() throws Exception {
        String body = mapper.writeValueAsString(java.util.Map.of(
                "name", "面向库存生产(改)", "description", "更新后的描述"));
        mockMvc.perform(put("/v1/contexts/" + contextId + "/scenarios/" + scenarioId)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("面向库存生产(改)"));
    }

    @Test @Order(7)
    void setApplicableObjectTypes() throws Exception {
        String body = mapper.writeValueAsString(java.util.Map.of(
                "objectTypeIds", "[\"ot-1\",\"ot-2\"]"));
        mockMvc.perform(put("/v1/contexts/" + contextId + "/scenarios/" + scenarioId + "/applicable-object-types")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicableObjectTypeIdsJson").value("[\"ot-1\",\"ot-2\"]"));
    }
}

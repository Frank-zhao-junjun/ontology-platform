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
 * S06: State Machine CRUD tests.
 * Tests AC-1 (define states), AC-2 (transitions with actions).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StateMachineControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper mapper;

    private static String contextId, smId;

    @Test @Order(1)
    void createContext() throws Exception {
        String body = mapper.writeValueAsString(java.util.Map.of(
                "name", "状态机测试", "code", "sm-test", "description", "S06测试"));
        String resp = mockMvc.perform(post("/v1/contexts")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        contextId = (String) mapper.readTree(resp).get("id").asText();
    }

    @Test @Order(2)
    void createStateMachine() throws Exception {
        String states = "["
                + "{\"code\":\"DRAFT\",\"name\":\"创建\",\"isInitial\":true},"
                + "{\"code\":\"RELEASED\",\"name\":\"已下达\"},"
                + "{\"code\":\"IN_PROGRESS\",\"name\":\"执行中\"},"
                + "{\"code\":\"CLOSED\",\"name\":\"技术关闭\",\"isFinal\":true}"
                + "]";
        String transitions = "["
                + "{\"from\":\"DRAFT\",\"to\":\"RELEASED\",\"actionId\":\"act-release\"},"
                + "{\"from\":\"RELEASED\",\"to\":\"IN_PROGRESS\",\"actionId\":\"act-start\"},"
                + "{\"from\":\"IN_PROGRESS\",\"to\":\"CLOSED\",\"actionId\":\"act-close\"}"
                + "]";
        String body = mapper.writeValueAsString(java.util.Map.of(
                "name", "生产订单状态机", "nameEn", "OrderStateMachine",
                "objectTypeId", "ot-order-1", "statusField", "status",
                "statesJson", states, "transitionsJson", transitions));
        String resp = mockMvc.perform(post("/v1/contexts/" + contextId + "/state-machines")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("生产订单状态机"))
                .andReturn().getResponse().getContentAsString();
        smId = (String) mapper.readTree(resp).get("id").asText();
    }

    @Test @Order(3)
    void rejectDuplicateForSameObjectType() throws Exception {
        String body = mapper.writeValueAsString(java.util.Map.of(
                "name", "重复状态机", "objectTypeId", "ot-order-1",
                "statesJson", "[]", "transitionsJson", "[]"));
        mockMvc.perform(post("/v1/contexts/" + contextId + "/state-machines")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(4)
    void listByContext() throws Exception {
        mockMvc.perform(get("/v1/contexts/" + contextId + "/state-machines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test @Order(5)
    void getById() throws Exception {
        mockMvc.perform(get("/v1/contexts/" + contextId + "/state-machines/" + smId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statesJson").isNotEmpty());
    }

    @Test @Order(6)
    void updateStates() throws Exception {
        String newStates = "[{\"code\":\"DRAFT\",\"name\":\"草稿\",\"isInitial\":true}]";
        String body = mapper.writeValueAsString(java.util.Map.of("statesJson", newStates));
        mockMvc.perform(put("/v1/contexts/" + contextId + "/state-machines/" + smId)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statesJson").value(newStates));
    }

    @Test @Order(7)
    void listByObjectType() throws Exception {
        mockMvc.perform(get("/v1/contexts/" + contextId + "/state-machines/by-object-type/ot-order-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}

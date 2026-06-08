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
 * S09: Cross-context relationship tests.
 * Tests AC-1 (define cross-context), AC-2 (dependency graph), AC-3 (integration events).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CrossContextControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper mapper;

    private static String ctx1, ctx2, ot1, ot2;

    @Test @Order(1)
    void createContexts() throws Exception {
        String b1 = mapper.writeValueAsString(java.util.Map.of(
                "name", "生产制造", "code", "mfg-cc", "description", "源上下文"));
        ctx1 = (String) mapper.readTree(mockMvc.perform(post("/v1/contexts")
                        .contentType(MediaType.APPLICATION_JSON).content(b1))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText();

        String b2 = mapper.writeValueAsString(java.util.Map.of(
                "name", "供应链", "code", "scm-cc", "description", "目标上下文"));
        ctx2 = (String) mapper.readTree(mockMvc.perform(post("/v1/contexts")
                        .contentType(MediaType.APPLICATION_JSON).content(b2))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText();
    }

    @Test @Order(2)
    void createObjectTypes() throws Exception {
        String b1 = mapper.writeValueAsString(java.util.Map.of(
                "name", "生产订单", "code", "ProductionOrder", "objectKind", "ENTITY"));
        ot1 = (String) mapper.readTree(mockMvc.perform(post("/v1/contexts/" + ctx1 + "/object-types")
                        .contentType(MediaType.APPLICATION_JSON).content(b1))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText();

        String b2 = mapper.writeValueAsString(java.util.Map.of(
                "name", "采购申请", "code", "PurchaseReq", "objectKind", "ENTITY"));
        ot2 = (String) mapper.readTree(mockMvc.perform(post("/v1/contexts/" + ctx2 + "/object-types")
                        .contentType(MediaType.APPLICATION_JSON).content(b2))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText();
    }

    @Test @Order(3)
    void createCrossContextRelationship() throws Exception {
        String body = mapper.writeValueAsString(java.util.Map.of(
                "sourceObjectId", ot1, "targetObjectId", ot2,
                "name", "触发采购", "code", "triggers_purchase",
                "cardinality", "1:N", "relationKind", "DEPENDENCY",
                "crossContext", true, "targetContextId", ctx2));
        mockMvc.perform(post("/v1/contexts/" + ctx1 + "/object-types/relationships")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.crossContext").value(true))
                .andExpect(jsonPath("$.targetContextId").value(ctx2));
    }

    @Test @Order(4)
    void listAllCrossContextRelationships() throws Exception {
        mockMvc.perform(get("/v1/cross-context-relationships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test @Order(5)
    void listByContext() throws Exception {
        mockMvc.perform(get("/v1/contexts/" + ctx1 + "/cross-context-relationships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test @Order(6)
    void getDependencyGraph() throws Exception {
        mockMvc.perform(get("/v1/cross-context-dependency-graph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contextIds.length()").value(2))
                .andExpect(jsonPath("$.edges.length()").value(1))
                .andExpect(jsonPath("$.totalEdges").value(1));
    }
}

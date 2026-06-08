package com.ontology.platform.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = "h2", inheritProfiles = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GovernanceControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper mapper;
    private static String contextId;
    private static String objectTypeId;
    private static String dataSourceId;
    private static String roleId;

    @Test @Order(1) void createContextAndObjectType() throws Exception {
        String ctxResp = mockMvc.perform(post("/v1/contexts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"S2\",\"code\":\"s2ctx_" + System.nanoTime() + "\",\"domainTag\":\"manufacturing\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        contextId = mapper.readTree(ctxResp).get("data").get("id").asText();
        String otResp = mockMvc.perform(post("/v1/contexts/{cid}/object-types", contextId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Mat\",\"code\":\"Material\",\"objectKind\":\"ENTITY\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        objectTypeId = mapper.readTree(otResp).get("data").get("id").asText();
    }

    @Test @Order(2) void createDataSourceAndAccessMethod() throws Exception {
        String dsResp = mockMvc.perform(post("/v1/data-sources").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"SAP\",\"code\":\"sap_" + System.nanoTime() + "\",\"sourceType\":\"SQL\",\"connectionConfig\":{}}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        dataSourceId = mapper.readTree(dsResp).get("data").get("id").asText();
        mockMvc.perform(post("/v1/contexts/{cid}/data-access-methods", contextId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"objectTypeId\":\"" + objectTypeId + "\",\"dataSourceId\":\"" + dataSourceId
                                + "\",\"methodType\":\"SQL_QUERY\",\"accessConfig\":{}}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/v1/data-sources").param("sourceType", "SQL")).andExpect(status().isOk());
    }

    @Test @Order(3) void createRolePermissionAndSandbox() throws Exception {
        String roleResp = mockMvc.perform(post("/v1/roles").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Planner\",\"code\":\"planner_" + System.nanoTime() + "\",\"contextId\":\"" + contextId + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        roleId = mapper.readTree(roleResp).get("data").get("id").asText();
        mockMvc.perform(post("/v1/roles/{rid}/object-permissions", roleId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"objectTypeId\":\"" + objectTypeId + "\",\"permRead\":true,\"permExecute\":true}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/v1/roles/{rid}/field-permissions", roleId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"objectTypeId\":\"" + objectTypeId + "\",\"fieldName\":\"order_id\",\"isVisible\":true,\"isEditable\":false}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/v1/roles/{rid}/field-permissions", roleId)).andExpect(status().isOk());
        mockMvc.perform(post("/v1/sandboxes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Agent-1\",\"agentRoleId\":\"" + roleId + "\","
                                + "\"allowedTools\":[\"resolve_intent\",\"query_ontology\",\"execute_action\"]}"))
                .andExpect(status().isCreated());
        JsonNode list = mapper.readTree(mockMvc.perform(get("/v1/sandboxes")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).get("data");
        Assertions.assertTrue(list.isArray() && list.size() >= 1);
    }
}

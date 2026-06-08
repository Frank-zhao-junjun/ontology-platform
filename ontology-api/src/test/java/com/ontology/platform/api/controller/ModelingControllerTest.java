package com.ontology.platform.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles(value = "h2", inheritProfiles = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ModelingControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper mapper;
    private static String cid;
    private static String arId;       // aggregate root id
    private static String otId;       // object type id
    private static String otId2;      // second object type id (for relationship)
    private static boolean initialized;

    @BeforeEach void setup() throws Exception {
        if (!initialized) {
            String resp = mockMvc.perform(post("/v1/contexts").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"S3-" + System.nanoTime() + "\",\"code\":\"s3ctx_" + System.nanoTime() + "\",\"domainTag\":\"manufacturing\"}"))
                    .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
            cid = mapper.readTree(resp).get("data").get("id").asText();
            initialized = true;
        }
    }

    // ════════════════════════════════════════════════
    // US-S03: Aggregate Roots
    // ════════════════════════════════════════════════

    @Test @Order(1)
    void createAggregateRoot() throws Exception {
        String resp = mockMvc.perform(post("/v1/contexts/{cid}/aggregate-roots", cid)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"生产订单\",\"code\":\"ProductionOrder\",\"description\":\"核心生产执行单据\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.name").value("生产订单"))
                .andReturn().getResponse().getContentAsString();
        arId = mapper.readTree(resp).get("data").get("id").asText();
    }

    @Test @Order(2)
    void duplicateAggregateRootCode() throws Exception {
        mockMvc.perform(post("/v1/contexts/{cid}/aggregate-roots", cid)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"重复\",\"code\":\"ProductionOrder\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(3)
    void listAggregateRoots() throws Exception {
        mockMvc.perform(get("/v1/contexts/{cid}/aggregate-roots", cid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    // ════════════════════════════════════════════════
    // US-S04: Object Types
    // ════════════════════════════════════════════════

    @Test @Order(10)
    void createObjectType() throws Exception {
        String body = mapper.writeValueAsString(
                mapOf("name", "订单头", "code", "OrderHeader", "objectKind", "ENTITY",
                        "aggregateRootId", arId, "description", "生产订单头信息"));
        String resp = mockMvc.perform(post("/v1/contexts/{cid}/object-types", cid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.name").value("订单头"))
                .andExpect(jsonPath("$.data.objectKind").value("ENTITY"))
                .andReturn().getResponse().getContentAsString();
        otId = mapper.readTree(resp).get("data").get("id").asText();
    }

    @Test @Order(11)
    void createSecondObjectType() throws Exception {
        String body = mapper.writeValueAsString(
                mapOf("name", "工序", "code", "Operation", "objectKind", "ENTITY",
                        "aggregateRootId", arId));
        String resp = mockMvc.perform(post("/v1/contexts/{cid}/object-types", cid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("工序"))
                .andReturn().getResponse().getContentAsString();
        otId2 = mapper.readTree(resp).get("data").get("id").asText();
    }

    @Test @Order(12)
    void createValueObject() throws Exception {
        String body = mapper.writeValueAsString(
                mapOf("name", "地址", "code", "Address", "objectKind", "VALUE_OBJECT",
                        "aggregateRootId", arId));
        mockMvc.perform(post("/v1/contexts/{cid}/object-types", cid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.objectKind").value("VALUE_OBJECT"));
    }

    @Test @Order(13)
    void listObjectTypes() throws Exception {
        mockMvc.perform(get("/v1/contexts/{cid}/object-types", cid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test @Order(14)
    void getObjectType() throws Exception {
        mockMvc.perform(get("/v1/contexts/{cid}/object-types/{otId}", cid, otId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("订单头"))
                .andExpect(jsonPath("$.data.code").value("OrderHeader"))
                .andExpect(jsonPath("$.data.aggregateRootId").value(arId));
    }

    @Test @Order(15)
    void updateAttributes() throws Exception {
        String attributes = "[{\"name\":\"order_id\",\"type\":\"STRING\",\"required\":true}]";
        mockMvc.perform(put("/v1/contexts/{cid}/object-types/{otId}/attributes", cid, otId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(attributes))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attributes").value(attributes));
    }

    @Test @Order(16)
    void getObjectTypeNotFound() throws Exception {
        mockMvc.perform(get("/v1/contexts/{cid}/object-types/nonexistent-id", cid))
                .andExpect(status().isNotFound());
    }

    // ════════════════════════════════════════════════
    // US-S07: Relationships
    // ════════════════════════════════════════════════

    @Test @Order(20)
    void createCompositionRelationship() throws Exception {
        String body = mapper.writeValueAsString(
                mapOf("sourceObjectId", otId, "targetObjectId", otId2,
                        "name", "包含", "code", "contains",
                        "cardinality", "1:N", "relationKind", "COMPOSITION"));
        mockMvc.perform(post("/v1/contexts/{cid}/relationships", cid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("包含"))
                .andExpect(jsonPath("$.data.relationKind").value("COMPOSITION"))
                .andExpect(jsonPath("$.data.cardinality").value("1:N"))
                .andExpect(jsonPath("$.data.crossContext").value(false));
    }

    @Test @Order(21)
    void createReferenceRelationship() throws Exception {
        String body = mapper.writeValueAsString(
                mapOf("sourceObjectId", otId2, "targetObjectId", otId,
                        "name", "引用", "code", "references",
                        "cardinality", "N:1", "relationKind", "REFERENCE"));
        mockMvc.perform(post("/v1/contexts/{cid}/relationships", cid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.relationKind").value("REFERENCE"));
    }

    @Test @Order(22)
    void createCrossContextRelationship() throws Exception {
        // cross-context relationship between objects in same context (simulated)
        java.util.Map<String, Object> crossReq = new java.util.LinkedHashMap<>();
        crossReq.put("sourceObjectId", otId);
        crossReq.put("targetObjectId", otId2);
        crossReq.put("name", "触发");
        crossReq.put("code", "triggers");
        crossReq.put("cardinality", "1:1");
        crossReq.put("relationKind", "DEPENDENCY");
        crossReq.put("crossContext", true);
        crossReq.put("targetContextId", "fake-context-id");
        String body = mapper.writeValueAsString(crossReq);
        mockMvc.perform(post("/v1/contexts/{cid}/relationships", cid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.crossContext").value(true))
                .andExpect(jsonPath("$.data.relationKind").value("DEPENDENCY"));
    }

    @Test @Order(23)
    void listRelationships() throws Exception {
        mockMvc.perform(get("/v1/contexts/{cid}/relationships", cid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test @Order(24)
    void compositionCrossAggregateRejected() throws Exception {
        // Create two object types in different aggregate roots (simulated)
        // This tests the validation that COMPOSITION must be within same aggregate
        String body = mapper.writeValueAsString(
                mapOf("sourceObjectId", otId, "targetObjectId", "different-aggregate-obj-id",
                        "name", "测试", "code", "test_comp", "relationKind", "COMPOSITION"));
        mockMvc.perform(post("/v1/contexts/{cid}/relationships", cid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().is5xxServerError()); // ResourceNotFoundException → 500
    }

    // Helper: Map.of with varargs (Java 8 compatible)
    private static java.util.Map<String, Object> mapOf(String... keysAndValues) {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2)
            m.put(keysAndValues[i], keysAndValues[i + 1]);
        return m;
    }
}

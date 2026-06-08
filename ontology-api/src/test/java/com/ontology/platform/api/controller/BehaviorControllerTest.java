package com.ontology.platform.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = "h2", inheritProfiles = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BehaviorControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    private static String contextId;

    @BeforeEach
    void setUp() throws Exception {
        if (contextId == null) {
            Map<String, Object> ctx = Map.of("name", "BehaviorTestCtx", "code", "behavior_test",
                    "domainTag", "manufacturing", "description", "Test context for behavior tests");
            String resp = mockMvc.perform(post("/v1/contexts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(ctx)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            var root = objectMapper.readTree(resp);
            contextId = root.get("data").get("id").asText();
        }
    }

    // ── B01: Actions ──

    @Test
    @Order(1)
    void shouldCreateAction() throws Exception {
        Map<String, Object> action = buildActionBody("create_prod_order", "创建生产订单",
                "CreateProductionOrder", "BOTH");
        // Note: aggregateRootId needs a valid aggregate root, which may not exist.
        // For integration test without fixtures, we expect validation error.
        mockMvc.perform(post("/v1/contexts/" + contextId + "/actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(action)))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    void shouldListActions() throws Exception {
        mockMvc.perform(get("/v1/contexts/" + contextId + "/actions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ── B03: Validation Rules ──

    @Test
    @Order(3)
    void shouldCreateValidationRule() throws Exception {
        Map<String, Object> rule = new LinkedHashMap<>();
        rule.put("manifestCode", "material_kit_check");
        rule.put("name", "物料齐套校验");
        rule.put("ruleType", "PRE_CHECK");
        rule.put("expressionJson", "{\"condition\":\"ANY\",\"rules\":[{\"field\":\"material.qty_on_hand\",\"op\":\"<\",\"value\":\"order.qty_required\"}]}");
        rule.put("errorMessage", "物料库存不足，无法下达工单");
        rule.put("failurePayloadSchema", "{\"failed_items\":[],\"message\":\"\"}");

        mockMvc.perform(post("/v1/contexts/" + contextId + "/validation-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rule)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.manifestCode").value("material_kit_check"))
                .andExpect(jsonPath("$.data.name").value("物料齐套校验"));
    }

    @Test
    @Order(4)
    void shouldListValidationRules() throws Exception {
        mockMvc.perform(get("/v1/contexts/" + contextId + "/validation-rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(5)
    void shouldRejectDuplicateRuleManifestCode() throws Exception {
        Map<String, Object> rule = new LinkedHashMap<>();
        rule.put("manifestCode", "material_kit_check");
        rule.put("name", "重复规则");
        rule.put("ruleType", "PRE_CHECK");
        rule.put("expressionJson", "{}");
        rule.put("errorMessage", "test");

        mockMvc.perform(post("/v1/contexts/" + contextId + "/validation-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rule)))
                .andExpect(status().is4xxClientError());
    }

    // ── B05: Metrics ──

    @Test
    @Order(6)
    void shouldCreateMetric() throws Exception {
        Map<String, Object> metric = new LinkedHashMap<>();
        metric.put("manifestCode", "ontime_completion_rate");
        metric.put("name", "准时完工率");
        metric.put("nameEn", "OnTime Completion Rate");
        metric.put("formula", "按时完工工单数 / 总完工工单数 * 100%");
        metric.put("dataSourceRefJson", "[{\"eventId\":\"work_order_tech_close\",\"field\":\"timestamp\"}]");
        metric.put("aggregationDimensionsJson", "[\"workshop\",\"month\"]");
        metric.put("period", "monthly");

        mockMvc.perform(post("/v1/contexts/" + contextId + "/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metric)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.manifestCode").value("ontime_completion_rate"))
                .andExpect(jsonPath("$.data.name").value("准时完工率"))
                .andExpect(jsonPath("$.data.period").value("monthly"));
    }

    @Test
    @Order(7)
    void shouldListMetrics() throws Exception {
        mockMvc.perform(get("/v1/contexts/" + contextId + "/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(8)
    void shouldRejectDuplicateMetricManifestCode() throws Exception {
        Map<String, Object> metric = new LinkedHashMap<>();
        metric.put("manifestCode", "ontime_completion_rate");
        metric.put("name", "重复指标");
        metric.put("formula", "1+1");

        mockMvc.perform(post("/v1/contexts/" + contextId + "/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metric)))
                .andExpect(status().is4xxClientError());
    }

    // ── Domain Events (E01) ──

    @Test
    @Order(9)
    void shouldCreateDomainEvent() throws Exception {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("manifestCode", "prod_order_dispatched");
        event.put("name", "生产订单已下达");
        event.put("nameEn", "ProductionOrderDispatched");
        event.put("aggregateRootId", "some-ar-id");
        event.put("triggerActionId", "");
        event.put("payloadSchemaJson", "{\"orderId\":\"string\",\"dispatchedAt\":\"datetime\"}");

        // aggregateRootId validation may fail without fixtures
        mockMvc.perform(post("/v1/contexts/" + contextId + "/domain-events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated());
    }

    // helper

    private Map<String, Object> buildActionBody(String manifestCode, String name, String nameEn, String invocationMode) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("manifestCode", manifestCode);
        m.put("name", name);
        m.put("nameEn", nameEn);
        m.put("description", name + " description");
        m.put("aggregateRootId", "some-ar-id");
        m.put("invocationMode", invocationMode);
        m.put("parametersJson", "[{\"name\":\"orderId\",\"type\":\"string\",\"required\":true}]");
        m.put("publishesEventIdsJson", "[]");
        m.put("allowedStateFromJson", "[]");
        m.put("businessScenarioIdsJson", "[]");
        m.put("mcpToolName", "create_production_order");
        return m;
    }
}

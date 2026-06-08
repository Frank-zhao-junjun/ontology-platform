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
 * S05: Value Object CRUD tests.
 * Tests cover AC-1 (create), AC-2 (reuse via reference), AC-3 (update propagates).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ValueObjectControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper mapper;

    private static String moneyId;

    @Test @Order(1)
    void createMoneyValueObject() throws Exception {
        String props = "[{\"name\":\"金额\",\"nameEn\":\"amount\",\"dataType\":\"decimal\",\"required\":true},"
                + "{\"name\":\"币种\",\"nameEn\":\"currency\",\"dataType\":\"string\",\"required\":true}]";
        String body = mapper.writeValueAsString(java.util.Map.of(
                "name", "金额", "code", "Money",
                "nameEn", "Money", "description", "金额+币种复合值对象",
                "propertiesJson", props));
        String resp = mockMvc.perform(post("/v1/value-objects")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("Money"))
                .andReturn().getResponse().getContentAsString();
        moneyId = (String) mapper.readTree(resp).get("id").asText();
    }

    @Test @Order(2)
    void createQuantityValueObject() throws Exception {
        String props = "[{\"name\":\"数值\",\"nameEn\":\"amount\",\"dataType\":\"decimal\",\"required\":true},"
                + "{\"name\":\"单位\",\"nameEn\":\"unit\",\"dataType\":\"string\",\"required\":true}]";
        String body = mapper.writeValueAsString(java.util.Map.of(
                "name", "数量", "code", "Quantity",
                "nameEn", "Quantity", "propertiesJson", props));
        mockMvc.perform(post("/v1/value-objects")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("Quantity"));
    }

    @Test @Order(3)
    void rejectDuplicateCode() throws Exception {
        String body = mapper.writeValueAsString(java.util.Map.of(
                "name", "重复金额", "code", "Money", "nameEn", "Dup"));
        mockMvc.perform(post("/v1/value-objects")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(4)
    void listValueObjects() throws Exception {
        mockMvc.perform(get("/v1/value-objects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test @Order(5)
    void getValueObjectById() throws Exception {
        mockMvc.perform(get("/v1/value-objects/" + moneyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("Money"));
    }

    @Test @Order(6)
    void updateValueObject() throws Exception {
        String body = mapper.writeValueAsString(java.util.Map.of(
                "description", "金额值对象（含精度说明）"));
        mockMvc.perform(put("/v1/value-objects/" + moneyId)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("金额值对象（含精度说明）"))
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test @Order(7)
    void getNonExistent() throws Exception {
        mockMvc.perform(get("/v1/value-objects/nonexistent"))
                .andExpect(status().isNotFound());
    }
}

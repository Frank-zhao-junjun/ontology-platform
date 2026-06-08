package com.ontology.platform.api.controller;

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

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles(value = "h2", inheritProfiles = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ModelingControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper mapper;
    private static String cid;
    private static boolean initialized;

    @BeforeEach void setup() throws Exception {
        if (!initialized) {
            String resp = mockMvc.perform(post("/v1/contexts").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"S1-" + System.nanoTime() + "\",\"code\":\"s1ctx_" + System.nanoTime() + "\",\"domainTag\":\"manufacturing\"}"))
                    .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
            cid = mapper.readTree(resp).get("data").get("id").asText();
            initialized = true;
        }
    }

    @Test @Order(1) void createAggregateRoot() throws Exception {
        mockMvc.perform(post("/v1/contexts/{cid}/aggregate-roots", cid).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"PO\",\"code\":\"ProductionOrder\"}"))
                .andExpect(status().isCreated());
    }
    @Test @Order(2) void createObjectType() throws Exception {
        mockMvc.perform(post("/v1/contexts/{cid}/object-types", cid).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"OrderHeader\",\"code\":\"OrderHeader\",\"objectKind\":\"ENTITY\"}"))
                .andExpect(status().isCreated());
    }
    @Test @Order(3) void listAggregateRoots() throws Exception { mockMvc.perform(get("/v1/contexts/{cid}/aggregate-roots", cid)).andExpect(status().isOk()); }
    @Test @Order(4) void listObjectTypes() throws Exception { mockMvc.perform(get("/v1/contexts/{cid}/object-types", cid)).andExpect(status().isOk()); }
}

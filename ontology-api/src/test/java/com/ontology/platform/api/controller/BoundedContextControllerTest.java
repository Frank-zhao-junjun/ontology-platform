package com.ontology.platform.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.api.dto.BoundedContextCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = "h2", inheritProfiles = false)
class BoundedContextControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void shouldCreateBoundedContext() throws Exception {
        BoundedContextCreateRequest req = new BoundedContextCreateRequest();
        req.setName("test");
        req.setCode("testctx");
        req.setDomainTag("manufacturing");
        String resp = mockMvc.perform(post("/v1/contexts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        System.out.println("RESPONSE: " + resp);
    }
}

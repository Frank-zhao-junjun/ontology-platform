package com.ontology.platform.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = "h2", inheritProfiles = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OntologyImportControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void dryRunUsA01SmokeEndpoint() throws Exception {
        mockMvc.perform(post("/ontology/import/dry-run/us-a01-smoke"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.draftId").value("dry-run:manufacturing-ontology:0.1.0"))
                .andExpect(jsonPath("$.data.importedCounts.objectTypes").value(5))
                .andExpect(jsonPath("$.data.errors").isEmpty());
    }

    @Test
    void dryRunAcceptsYamlBody() throws Exception {
        byte[] yaml = new ClassPathResource("manifests/us-a01/manufacturing-manifest.yaml")
                .getContentAsByteArray();
        mockMvc.perform(post("/ontology/import/dry-run")
                        .contentType("application/yaml")
                        .content(yaml))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.importedCounts.actions").value(3));
    }

    @Test
    void importUsA01SmokePersistsContextAndModel() throws Exception {
        String resp = mockMvc.perform(post("/ontology/import/import/us-a01-smoke"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.contextId").isNotEmpty())
                .andExpect(jsonPath("$.data.draftId").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String contextId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(resp).get("data").get("contextId").asText();

        mockMvc.perform(get("/v1/contexts/{id}", contextId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/contexts/{cid}/aggregate-roots", contextId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(4));

        mockMvc.perform(get("/v1/contexts/{cid}/object-types", contextId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(5));

        mockMvc.perform(get("/v1/roles").param("contextId", contextId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }
}

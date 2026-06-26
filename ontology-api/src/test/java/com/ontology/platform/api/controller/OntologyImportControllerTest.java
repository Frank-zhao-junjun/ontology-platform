package com.ontology.platform.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.infrastructure.persistence.ManifestImportPO;
import com.ontology.platform.infrastructure.persistence.ManifestImportPOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OntologyImportControllerTest {

    @Mock
    private ManifestImportPOMapper mapper;

    @Captor
    private ArgumentCaptor<ManifestImportPO> poCaptor;

    private OntologyImportController controller;
    private ObjectMapper objectMapper;

    private final String SAMPLE_MODEL = """
            {
              "version": "v1",
              "project": { "name": "测试模型", "id": "TP-001" },
              "entities": [
                { "id": "material", "name": "物料", "attributes": [], "relations": [] }
              ],
              "stateMachines": [],
              "rules": [],
              "metrics": [],
              "dataSources": [],
              "businessChain": { "valueDomains": [], "capabilities": [], "scenarios": [], "epcProcesses": [] },
              "governance": { "roles": [] }
            }
            """;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        controller = new OntologyImportController(mapper, objectMapper);
    }

    @Test
    void import_shouldReturn200_whenValid() {
        OntologyImportRequest request = new OntologyImportRequest(SAMPLE_MODEL, "admin");

        ResponseEntity<ApiResponse<OntologyImportResponse>> response = controller.importOntology(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
        assertEquals("TP-001", response.getBody().getData().getExternalId());
        assertNotNull(response.getBody().getData().getDraftId());
        assertEquals(1, response.getBody().getData().getImportedCounts().get("entities"));
    }

    @Test
    void import_shouldReturn400_whenInvalidJson() {
        OntologyImportRequest request = new OntologyImportRequest("not valid json", "admin");

        ResponseEntity<ApiResponse<OntologyImportResponse>> response = controller.importOntology(request);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals(400, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("PARSE_ERROR"));
    }

    @Test
    void import_shouldReturn422_whenMissingFields() {
        OntologyImportRequest request = new OntologyImportRequest("{\"version\":\"v1\"}", "admin");

        ResponseEntity<ApiResponse<OntologyImportResponse>> response = controller.importOntology(request);

        assertEquals(422, response.getStatusCodeValue());
        assertEquals(422, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("VALIDATION_ERROR"));
    }

    @Test
    void import_shouldReturn422_whenEmptyBody() {
        OntologyImportRequest request = new OntologyImportRequest("", "admin");

        ResponseEntity<ApiResponse<OntologyImportResponse>> response = controller.importOntology(request);

        assertEquals(422, response.getStatusCodeValue());
        assertEquals(422, response.getBody().getCode());
    }

    @Test
    void import_shouldPersistToDB() {
        OntologyImportRequest request = new OntologyImportRequest(SAMPLE_MODEL, "admin");

        controller.importOntology(request);

        verify(mapper).insert(poCaptor.capture());
        ManifestImportPO po = poCaptor.getValue();
        assertEquals("TP-001", po.getExternalId());
        assertEquals("v1", po.getManifestVersion());
        assertEquals("DRAFT", po.getStatus());
        assertNotNull(po.getRawContent());
    }

    @Test
    void import_shouldReturnCountsCorrectness() {
        String multiModel = """
                {
                  "version": "v1",
                  "project": { "name": "多实体模型", "id": "TP-002" },
                  "entities": [{"id":"e1"},{"id":"e2"},{"id":"e3"},{"id":"e4"},{"id":"e5"}],
                  "stateMachines": [{"id":"s1"},{"id":"s2"},{"id":"s3"}],
                  "rules": [{"id":"r1"},{"id":"r2"},{"id":"r3"},{"id":"r4"},{"id":"r5"},{"id":"r6"},{"id":"r7"},{"id":"r8"},{"id":"r9"},{"id":"r10"}],
                  "metrics": [{"id":"m1"},{"id":"m2"}],
                  "dataSources": [],
                  "businessChain": {"valueDomains":[],"capabilities":[],"scenarios":[],"epcProcesses":[]},
                  "governance": {"roles":[]}
                }
                """;
        OntologyImportRequest request = new OntologyImportRequest(multiModel, "admin");

        ResponseEntity<ApiResponse<OntologyImportResponse>> response = controller.importOntology(request);

        Map<String, Integer> counts = response.getBody().getData().getImportedCounts();
        assertEquals(5, counts.get("entities"));
        assertEquals(3, counts.get("stateMachines"));
        assertEquals(10, counts.get("rules"));
        assertEquals(2, counts.get("metrics"));
        assertEquals(0, counts.get("dataSources"));
    }

    @Test
    void import_shouldReturn409_whenDuplicate() {
        OntologyImportRequest request = new OntologyImportRequest(SAMPLE_MODEL, "admin");
        doThrow(new DataIntegrityViolationException("duplicate")).when(mapper).insert(any());

        ResponseEntity<ApiResponse<OntologyImportResponse>> response = controller.importOntology(request);

        assertEquals(409, response.getStatusCodeValue());
        assertEquals(409, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("DUPLICATE"));
    }
}

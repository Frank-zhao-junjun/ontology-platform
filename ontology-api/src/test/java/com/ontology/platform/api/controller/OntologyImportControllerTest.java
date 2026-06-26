package com.ontology.platform.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.service.exchange.ExchangeImportService;
import com.ontology.platform.domain.dto.imports.ExchangeImportResponse;
import com.ontology.platform.infrastructure.imports.Project1JsonToExchangeConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OntologyImportControllerTest {

    @Mock
    private ExchangeImportService exchangeImportService;

    private Project1JsonToExchangeConverter converter;
    private ObjectMapper objectMapper;
    private OntologyImportController controller;

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
        converter = new Project1JsonToExchangeConverter(objectMapper);
        controller = new OntologyImportController(converter, exchangeImportService, objectMapper);
    }

    @Test
    void import_shouldReturn200_whenValid() {
        ExchangeImportResponse mockResp = ExchangeImportResponse.builder()
                .id("import-001").status("passed").totalEntities(1).warnings(0).build();
        when(exchangeImportService.importExchange(anyString(), eq("strict"))).thenReturn(mockResp);

        OntologyImportRequest request = new OntologyImportRequest(SAMPLE_MODEL, "admin", false, "strict");
        ResponseEntity<ApiResponse<OntologyImportResponse>> response = controller.importOntology(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
        assertEquals("TP-001", response.getBody().getData().getExternalId());
        assertEquals("import-001", response.getBody().getData().getDraftId());
        assertEquals("passed", response.getBody().getData().getStatus());
        assertEquals(1, response.getBody().getData().getTotalEntities());
        assertEquals(0, response.getBody().getData().getWarnings());
        assertEquals(1, response.getBody().getData().getImportedCounts().get("entities"));
    }

    @Test
    void import_shouldReturn400_whenInvalidJson() {
        OntologyImportRequest request = new OntologyImportRequest("not valid json", "admin", false, "strict");

        ResponseEntity<ApiResponse<OntologyImportResponse>> response = controller.importOntology(request);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals(400, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("PARSE_ERROR"));
    }

    @Test
    void import_shouldReturn422_whenMissingFields() {
        // JSON parse succeeds but converter returns null for incomplete document
        String incomplete = "{\"version\":\"v1\"}";
        OntologyImportRequest request = new OntologyImportRequest(incomplete, "admin", false, "strict");

        ResponseEntity<ApiResponse<OntologyImportResponse>> response = controller.importOntology(request);

        assertEquals(422, response.getStatusCodeValue());
        assertEquals(422, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("CONVERSION_ERROR"));
    }

    @Test
    void import_shouldReturn422_whenEmptyBody() {
        OntologyImportRequest request = new OntologyImportRequest("", "admin", false, "strict");

        ResponseEntity<ApiResponse<OntologyImportResponse>> response = controller.importOntology(request);

        assertEquals(422, response.getStatusCodeValue());
        assertEquals(422, response.getBody().getCode());
    }

    @Test
    void import_shouldPersistToDB() {
        ExchangeImportResponse mockResp = ExchangeImportResponse.builder()
                .id("import-002").status("passed").totalEntities(1).warnings(0).build();
        when(exchangeImportService.importExchange(anyString(), eq("strict"))).thenReturn(mockResp);

        OntologyImportRequest request = new OntologyImportRequest(SAMPLE_MODEL, "admin", false, "strict");
        ResponseEntity<ApiResponse<OntologyImportResponse>> response = controller.importOntology(request);

        // Verify ExchangeImportService was called (persistence delegated)
        verify(exchangeImportService).importExchange(anyString(), eq("strict"));
        assertEquals("import-002", response.getBody().getData().getDraftId());
        assertEquals("passed", response.getBody().getData().getStatus());
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
        ExchangeImportResponse mockResp = ExchangeImportResponse.builder()
                .id("import-003").status("passed").totalEntities(5).warnings(0).build();
        when(exchangeImportService.importExchange(anyString(), eq("strict"))).thenReturn(mockResp);

        OntologyImportRequest request = new OntologyImportRequest(multiModel, "admin", false, "strict");
        ResponseEntity<ApiResponse<OntologyImportResponse>> response = controller.importOntology(request);

        Map<String, Integer> counts = response.getBody().getData().getImportedCounts();
        assertEquals(5, counts.get("entities"));
        assertEquals(3, counts.get("stateMachines"));
        assertEquals(10, counts.get("rules"));
        assertEquals(2, counts.get("metrics"));
        assertEquals(0, counts.get("dataSources"));
    }

    @Test
    void import_shouldReturn422_whenServiceFails() {
        when(exchangeImportService.importExchange(anyString(), eq("strict")))
                .thenThrow(new RuntimeException("Database error"));

        OntologyImportRequest request = new OntologyImportRequest(SAMPLE_MODEL, "admin", false, "strict");
        ResponseEntity<ApiResponse<OntologyImportResponse>> response = controller.importOntology(request);

        assertEquals(422, response.getStatusCodeValue());
        assertEquals(422, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("IMPORT_ERROR"));
    }

    @Test
    void import_shouldAutoPublish_whenEnabled() {
        ExchangeImportResponse importResp = ExchangeImportResponse.builder()
                .id("import-004").status("passed").totalEntities(1).warnings(0).build();
        ExchangeImportResponse publishResp = ExchangeImportResponse.builder()
                .id("import-004").status("published").totalEntities(1).warnings(0).build();
        when(exchangeImportService.importExchange(anyString(), eq("strict"))).thenReturn(importResp);
        when(exchangeImportService.publishImport("import-004")).thenReturn(publishResp);

        OntologyImportRequest request = new OntologyImportRequest(SAMPLE_MODEL, "admin", true, "strict");
        ResponseEntity<ApiResponse<OntologyImportResponse>> response = controller.importOntology(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("published", response.getBody().getData().getStatus());
        verify(exchangeImportService).publishImport("import-004");
    }

    @Test
    void import_shouldNotPublish_whenValidationFailed() {
        ExchangeImportResponse importResp = ExchangeImportResponse.builder()
                .id("import-005").status("failed").totalEntities(0).warnings(3).build();
        when(exchangeImportService.importExchange(anyString(), eq("warn"))).thenReturn(importResp);

        OntologyImportRequest request = new OntologyImportRequest(SAMPLE_MODEL, "admin", true, "warn");
        ResponseEntity<ApiResponse<OntologyImportResponse>> response = controller.importOntology(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("failed", response.getBody().getData().getStatus());
        // Publish should NOT be called when status is "failed"
        verify(exchangeImportService, never()).publishImport(anyString());
    }
}

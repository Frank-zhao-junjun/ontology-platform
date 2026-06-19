package com.ontology.platform.api.controller.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.api.config.GlobalExceptionHandler;
import com.ontology.platform.application.service.exchange.ExchangeImportService;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.dto.imports.ExchangeImportDocument;
import com.ontology.platform.domain.dto.imports.ExchangeImportResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link ExchangeController}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeController Test")
class ExchangeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ExchangeImportService exchangeService;

    @InjectMocks
    private ExchangeController exchangeController;

    private ObjectMapper objectMapper;

    private static final String TEST_IMPORT_ID = "test-import-id-001";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(exchangeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Nested
    @DisplayName("POST /api/v2/exchanges/import - import exchange")
    class ImportExchangeTests {

        @Test
        @DisplayName("should return 201 with import response")
        void importExchangeSuccess() throws Exception {
            var request = ExchangeImportDocument.builder()
                    .document("{\"apiVersion\":\"ontology.platform/v2\",\"kind\":\"OntologyExchange\",\"metadata\":{\"id\":\"test-1\",\"name\":\"Test\"}}")
                    .validationMode("strict")
                    .build();

            var response = ExchangeImportResponse.builder()
                    .id(TEST_IMPORT_ID)
                    .status("passed")
                    .totalEntities(0)
                    .warnings(0)
                    .build();

            when(exchangeService.importExchange(anyString(), anyString()))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v2/exchanges/import")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(TEST_IMPORT_ID))
                    .andExpect(jsonPath("$.data.status").value("passed"));

            verify(exchangeService).importExchange(anyString(), eq("strict"));
        }

        @Test
        @DisplayName("should return 400 when document is blank")
        void importExchangeInvalidDocument() throws Exception {
            var request = ExchangeImportDocument.builder()
                    .document("")
                    .build();

            when(exchangeService.importExchange(eq(""), any()))
                    .thenThrow(new BusinessException(ErrorCode.VALIDATION_ERROR, "Document must not be empty"));

            mockMvc.perform(post("/api/v2/exchanges/import")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(is(not(0))));
        }

        @Test
        @DisplayName("should use default validation mode when not provided")
        void importExchangeDefaultMode() throws Exception {
            var request = ExchangeImportDocument.builder()
                    .document("{\"apiVersion\":\"ontology.platform/v2\",\"kind\":\"OntologyExchange\",\"metadata\":{\"id\":\"test-1\",\"name\":\"Test\"}}")
                    .build();

            var response = ExchangeImportResponse.builder()
                    .id(TEST_IMPORT_ID)
                    .status("passed")
                    .totalEntities(0)
                    .warnings(0)
                    .build();

            when(exchangeService.importExchange(anyString(), any()))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v2/exchanges/import")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(0));

            verify(exchangeService).importExchange(anyString(), any());
        }
    }

    @Nested
    @DisplayName("GET /api/v2/exchanges/{id} - get import")
    class GetImportTests {

        @Test
        @DisplayName("should return import status")
        void getImportSuccess() throws Exception {
            var response = ExchangeImportResponse.builder()
                    .id(TEST_IMPORT_ID)
                    .status("passed")
                    .totalEntities(5)
                    .warnings(0)
                    .build();

            when(exchangeService.getImportStatus(TEST_IMPORT_ID)).thenReturn(response);

            mockMvc.perform(get("/api/v2/exchanges/" + TEST_IMPORT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(TEST_IMPORT_ID))
                    .andExpect(jsonPath("$.data.status").value("passed"))
                    .andExpect(jsonPath("$.data.totalEntities").value(5));
        }

        @Test
        @DisplayName("should return 404 when import not found")
        void getImportNotFound() throws Exception {
            when(exchangeService.getImportStatus(TEST_IMPORT_ID))
                    .thenThrow(new ResourceNotFoundException("ExchangeImport", TEST_IMPORT_ID));

            mockMvc.perform(get("/api/v2/exchanges/" + TEST_IMPORT_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(is(not(0))));
        }
    }

    @Nested
    @DisplayName("POST /api/v2/exchanges/{id}/publish - publish import")
    class PublishImportTests {

        @Test
        @DisplayName("should publish successfully")
        void publishImportSuccess() throws Exception {
            var response = ExchangeImportResponse.builder()
                    .id(TEST_IMPORT_ID)
                    .status("published")
                    .totalEntities(5)
                    .warnings(0)
                    .build();

            when(exchangeService.publishImport(TEST_IMPORT_ID)).thenReturn(response);

            mockMvc.perform(post("/api/v2/exchanges/" + TEST_IMPORT_ID + "/publish"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(TEST_IMPORT_ID))
                    .andExpect(jsonPath("$.data.status").value("published"));
        }

        @Test
        @DisplayName("should return 404 when import not found for publish")
        void publishImportNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("ExchangeImport", TEST_IMPORT_ID))
                    .when(exchangeService).publishImport(TEST_IMPORT_ID);

            mockMvc.perform(post("/api/v2/exchanges/" + TEST_IMPORT_ID + "/publish"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(is(not(0))));
        }
    }
}

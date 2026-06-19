package com.ontology.platform.application.service.exchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.dto.imports.ExchangeImportResponse;
import com.ontology.platform.infrastructure.imports.ExcelExchangeMapper;
import com.ontology.platform.infrastructure.persistence.ExchangeImportPO;
import com.ontology.platform.infrastructure.persistence.ExchangeImportPOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ExchangeImportService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeImportService Test")
class ExchangeImportServiceTest {

    @Mock
    private ExchangeImportPOMapper mapper;

    @Mock
    private ExchangePhase3bPublisher phase3bPublisher;

    @Mock
    private ExchangePhase3cPublisher phase3cPublisher;

    @Mock
    private ExcelExchangeMapper excelExchangeMapper;

    private ObjectMapper objectMapper;

    private ExchangeImportService service;

    @Captor
    private ArgumentCaptor<ExchangeImportPO> poCaptor;

    private static final String VALID_MINIMAL_JSON = """
            {
              "apiVersion": "ontology.platform/v2",
              "kind": "OntologyExchange",
              "metadata": {
                "id": "test-ontology",
                "name": "测试本体",
                "version": "1.0.0",
                "source": "test",
                "status": "draft"
              },
              "spec": {
                "project": {
                  "id": "project-1",
                  "name": "测试项目"
                }
              }
            }
            """;

    private static final String VALID_FULL_JSON = """
            {
              "apiVersion": "ontology.platform/v2",
              "kind": "OntologyExchange",
              "metadata": {
                "id": "manufacturing-ontology",
                "version": "0.1.0",
                "name": "生产制造本体",
                "source": "ontology-designer",
                "status": "draft",
                "projectId": "manufacturing-project"
              },
              "spec": {
                "project": {
                  "id": "manufacturing-project",
                  "name": "生产制造本体",
                  "dataModel": {
                    "entities": [
                      { "id": "e1", "name": "Entity1" },
                      { "id": "e2", "name": "Entity2" },
                      { "id": "e3", "name": "Entity3" }
                    ]
                  }
                }
              }
            }
            """;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        ExchangeValidationService validationService = new ExchangeValidationService(List.of());
        service = new ExchangeImportService(mapper, objectMapper, validationService,
                phase3bPublisher, phase3cPublisher, excelExchangeMapper);
    }

    @Nested
    @DisplayName("importExchange()")
    class ImportExchangeTests {

        @Test
        @DisplayName("should parse and save minimal valid JSON document")
        void importExchangeMinimal() {
            when(mapper.insert(any())).thenReturn(1);

            ExchangeImportResponse response = service.importExchange(VALID_MINIMAL_JSON, "strict");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotBlank();
            assertThat(response.getStatus()).isEqualTo("passed");
            assertThat(response.getTotalEntities()).isEqualTo(0);
            assertThat(response.getWarnings()).isEqualTo(0);

            verify(mapper).insert(poCaptor.capture());
            ExchangeImportPO saved = poCaptor.getValue();
            assertThat(saved.getId()).isEqualTo(response.getId());
            assertThat(saved.getMetadataId()).isEqualTo("test-ontology");
            assertThat(saved.getMetadataName()).isEqualTo("测试本体");
            assertThat(saved.getMetadataVersion()).isEqualTo("1.0.0");
            assertThat(saved.getMetadataSource()).isEqualTo("test");
            assertThat(saved.getMetadataStatus()).isEqualTo("draft");
            assertThat(saved.getProjectId()).isEqualTo("project-1");
            assertThat(saved.getProjectName()).isEqualTo("测试项目");
            assertThat(saved.getRawDocument()).isEqualTo(VALID_MINIMAL_JSON);
            assertThat(saved.getValidationStatus()).isEqualTo("passed");
        }

        @Test
        @DisplayName("should parse and save full JSON document with entity count")
        void importExchangeFull() {
            when(mapper.insert(any())).thenReturn(1);

            ExchangeImportResponse response = service.importExchange(VALID_FULL_JSON, "strict");

            assertThat(response).isNotNull();
            assertThat(response.getTotalEntities()).isEqualTo(3);
            assertThat(response.getStatus()).isEqualTo("passed");

            verify(mapper).insert(poCaptor.capture());
            ExchangeImportPO saved = poCaptor.getValue();
            assertThat(saved.getMetadataId()).isEqualTo("manufacturing-ontology");
            assertThat(saved.getProjectId()).isEqualTo("manufacturing-project");
        }

        @Test
        @DisplayName("should throw BusinessException when JSON is blank")
        void importExchangeBlankDocument() {
            assertThatThrownBy(() -> service.importExchange("", "strict"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Document must not be empty");

            verify(mapper, never()).insert(any());
        }

        @Test
        @DisplayName("should throw BusinessException when JSON is null")
        void importExchangeNullDocument() {
            assertThatThrownBy(() -> service.importExchange(null, "strict"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Document must not be empty");

            verify(mapper, never()).insert(any());
        }

        @Test
        @DisplayName("should throw BusinessException when JSON is malformed")
        void importExchangeMalformedJson() {
            assertThatThrownBy(() -> service.importExchange("not valid json{", "strict"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Invalid JSON document");

            verify(mapper, never()).insert(any());
        }

        @Test
        @DisplayName("should throw BusinessException when apiVersion is missing")
        void importExchangeMissingApiVersion() {
            String json = "{\"kind\":\"OntologyExchange\",\"metadata\":{\"id\":\"t\"}}";

            assertThatThrownBy(() -> service.importExchange(json, "strict"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("apiVersion");

            verify(mapper, never()).insert(any());
        }

        @Test
        @DisplayName("should throw BusinessException when kind is missing")
        void importExchangeMissingKind() {
            String json = "{\"apiVersion\":\"ontology.platform/v2\",\"metadata\":{\"id\":\"t\"}}";

            assertThatThrownBy(() -> service.importExchange(json, "strict"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("kind");

            verify(mapper, never()).insert(any());
        }

        @Test
        @DisplayName("should throw BusinessException when metadata is missing")
        void importExchangeMissingMetadata() {
            String json = "{\"apiVersion\":\"ontology.platform/v2\",\"kind\":\"OntologyExchange\"}";

            assertThatThrownBy(() -> service.importExchange(json, "strict"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("metadata");

            verify(mapper, never()).insert(any());
        }

        @Test
        @DisplayName("should default to strict mode when validationMode is blank")
        void importExchangeDefaultMode() {
            when(mapper.insert(any())).thenReturn(1);

            ExchangeImportResponse response = service.importExchange(VALID_MINIMAL_JSON, null);

            assertThat(response.getStatus()).isEqualTo("passed");
            verify(mapper).insert(any());
        }
    }

    @Nested
    @DisplayName("getImportStatus()")
    class GetImportStatusTests {

        @Test
        @DisplayName("should return import status when record exists")
        void getImportStatusFound() {
            ExchangeImportPO po = ExchangeImportPO.builder()
                    .id("test-id")
                    .validationStatus("passed")
                    .validationReport("{\"totalEntities\":3,\"warnings\":1}")
                    .build();

            when(mapper.selectById("test-id")).thenReturn(po);

            ExchangeImportResponse response = service.getImportStatus("test-id");

            assertThat(response.getId()).isEqualTo("test-id");
            assertThat(response.getStatus()).isEqualTo("passed");
            assertThat(response.getTotalEntities()).isEqualTo(3);
            assertThat(response.getWarnings()).isEqualTo(1);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when record does not exist")
        void getImportStatusNotFound() {
            when(mapper.selectById("nonexistent")).thenReturn(null);

            assertThatThrownBy(() -> service.getImportStatus("nonexistent"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ExchangeImport");
        }

        @Test
        @DisplayName("should handle null validation report gracefully")
        void getImportStatusNullReport() {
            ExchangeImportPO po = ExchangeImportPO.builder()
                    .id("test-id")
                    .validationStatus("passed")
                    .validationReport(null)
                    .build();

            when(mapper.selectById("test-id")).thenReturn(po);

            ExchangeImportResponse response = service.getImportStatus("test-id");

            assertThat(response.getTotalEntities()).isEqualTo(0);
            assertThat(response.getWarnings()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("publishImport()")
    class PublishImportTests {

        @Test
        @DisplayName("should publish a passed import")
        void publishImportSuccess() {
            ExchangeImportPO po = ExchangeImportPO.builder()
                    .id("test-id")
                    .metadataId("test-ontology")
                    .validationStatus("passed")
                    .validationReport("{\"totalEntities\":5,\"warnings\":0}")
                    .rawDocument(VALID_MINIMAL_JSON)
                    .build();

            when(mapper.selectById("test-id")).thenReturn(po);
            when(mapper.updateById(any())).thenReturn(1);

            ExchangeImportResponse response = service.publishImport("test-id");

            assertThat(response.getId()).isEqualTo("test-id");
            assertThat(response.getStatus()).isEqualTo("published");
            assertThat(response.getTotalEntities()).isEqualTo(5);

            verify(mapper).updateById(poCaptor.capture());
            ExchangeImportPO updated = poCaptor.getValue();
            assertThat(updated.getMetadataStatus()).isEqualTo("published");
            assertThat(updated.getPublishedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when import not found")
        void publishImportNotFound() {
            when(mapper.selectById("nonexistent")).thenReturn(null);

            assertThatThrownBy(() -> service.publishImport("nonexistent"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ExchangeImport");
        }

        @Test
        @DisplayName("should throw BusinessException when import is not passed")
        void publishImportNotPassed() {
            ExchangeImportPO po = ExchangeImportPO.builder()
                    .id("test-id")
                    .validationStatus("failed")
                    .build();

            when(mapper.selectById("test-id")).thenReturn(po);

            assertThatThrownBy(() -> service.publishImport("test-id"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot publish");

            verify(mapper, never()).updateById(any());
        }
    }
}

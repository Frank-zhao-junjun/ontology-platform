package com.ontology.platform.application.service.exchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.dto.imports.ExchangeImportResponse;
import com.ontology.platform.infrastructure.imports.ExcelExchangeMapper;
import com.ontology.platform.infrastructure.imports.MarkdownExchangeMapper;
import com.ontology.platform.infrastructure.persistence.ExchangeImportPO;
import com.ontology.platform.infrastructure.persistence.ExchangeImportPOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    private ExchangePhase3cLifecyclePublisher phase3cLifecyclePublisher;

    @Mock
    private ExchangePhase3dPublisher phase3dPublisher;

    @Mock
    private ExcelExchangeMapper excelExchangeMapper;

    @Mock
    private MarkdownExchangeMapper markdownExchangeMapper;

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
                phase3bPublisher, phase3cPublisher, phase3cLifecyclePublisher, phase3dPublisher,
                excelExchangeMapper, markdownExchangeMapper);
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
        @DisplayName("should roll back when mapper.insert throws RuntimeException — no stale data retained")
        void importExchangeInsertFailure() {
            when(mapper.insert(any())).thenThrow(new RuntimeException("DB connection lost"));

            assertThatThrownBy(() -> service.importExchange(VALID_MINIMAL_JSON, "strict"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DB connection lost");

            // Verify insert was *attempted* but threw — in a @Transactional context this means
            // no record is committed; the mock verifies the call was made but no response returned.
            verify(mapper).insert(any());
        }

        @Test
        @DisplayName("should default to strict mode when validationMode is blank")
        void importExchangeDefaultMode() {
            when(mapper.insert(any())).thenReturn(1);

            ExchangeImportResponse response = service.importExchange(VALID_MINIMAL_JSON, null);

            assertThat(response.getStatus()).isEqualTo("passed");
            verify(mapper).insert(any());
        }

        @Test
        @DisplayName("concurrent import of same ontology — both succeed, no duplicate detection (known limitation)")
        void concurrentImportSameOntology() throws Exception {
            when(mapper.insert(any())).thenReturn(1);

            int threadCount = 2;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch readyLatch = new CountDownLatch(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);

            // Collect results from both threads
            ExchangeImportResponse[] results = new ExchangeImportResponse[threadCount];
            Throwable[] exceptions = new Throwable[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int idx = i;
                executor.submit(() -> {
                    try {
                        readyLatch.countDown();
                        startLatch.await();  // wait for both threads to be ready
                        results[idx] = service.importExchange(VALID_MINIMAL_JSON, "strict");
                    } catch (Throwable t) {
                        exceptions[idx] = t;
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            // Wait for both threads to be ready, then fire them simultaneously
            readyLatch.await(5, TimeUnit.SECONDS);
            startLatch.countDown();

            // Wait for both to finish
            boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(completed)
                    .as("Both threads should complete within timeout")
                    .isTrue();

            // Verify no exceptions occurred
            for (int i = 0; i < threadCount; i++) {
                assertThat(exceptions[i])
                        .as("Thread %d should not throw", i)
                        .isNull();
            }

            // Verify both returned successful responses
            for (int i = 0; i < threadCount; i++) {
                assertThat(results[i]).as("Result %d", i).isNotNull();
                assertThat(results[i].getStatus()).as("Result %d status", i).isEqualTo("passed");
            }

            // Both results should have different IDs (no dedup)
            assertThat(results[0].getId()).isNotEqualTo(results[1].getId());

            // Verify exactly 2 inserts happened
            verify(mapper, times(2)).insert(any());

            // Capture both inserts and verify no data corruption
            ArgumentCaptor<ExchangeImportPO> poCaptor = ArgumentCaptor.forClass(ExchangeImportPO.class);
            verify(mapper, times(2)).insert(poCaptor.capture());
            List<ExchangeImportPO> allPOs = poCaptor.getAllValues();

            assertThat(allPOs).hasSize(2);
            // Both records should have the same metadataName
            assertThat(allPOs.get(0).getMetadataName()).isEqualTo("测试本体");
            assertThat(allPOs.get(1).getMetadataName()).isEqualTo("测试本体");
            // Both records should have the same metadataId
            assertThat(allPOs.get(0).getMetadataId()).isEqualTo("test-ontology");
            assertThat(allPOs.get(1).getMetadataId()).isEqualTo("test-ontology");
            // Both records should have unique IDs
            assertThat(allPOs.get(0).getId()).isNotEqualTo(allPOs.get(1).getId());
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

        @Test
        @DisplayName("should roll back when phase3bPublisher throws — updateById never called")
        void publishImportRollbackWhenPhase3bFails() {
            ExchangeImportPO po = ExchangeImportPO.builder()
                    .id("test-id")
                    .metadataId("test-ontology")
                    .validationStatus("passed")
                    .rawDocument(VALID_MINIMAL_JSON)
                    .build();

            when(mapper.selectById("test-id")).thenReturn(po);
            when(phase3bPublisher.publish(anyString(), any())).thenThrow(new RuntimeException("Phase3b failure"));

            assertThatThrownBy(() -> service.publishImport("test-id"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Phase3b failure");

            // In a @Transactional context the update would be rolled back;
            // at the mock level we verify updateById was never reached.
            verify(mapper, never()).updateById(any());
            // Subsequent publishers should never be called either
            verify(phase3cPublisher, never()).publish(anyString(), any(), anyString());
            verify(phase3cLifecyclePublisher, never()).publish(anyString(), any(), anyString());
            verify(phase3dPublisher, never()).publish(anyString(), any(), anyString());
        }

        @Test
        @DisplayName("should roll back when phase3cPublisher throws mid-publish — no dirty update")
        void publishImportRollbackWhenPhase3cFails() {
            ExchangeImportPO po = ExchangeImportPO.builder()
                    .id("test-id")
                    .metadataId("test-ontology")
                    .validationStatus("passed")
                    .rawDocument(VALID_MINIMAL_JSON)
                    .build();

            when(mapper.selectById("test-id")).thenReturn(po);
            when(phase3bPublisher.publish(anyString(), any())).thenReturn(java.util.Map.of());
            when(phase3cPublisher.publish(anyString(), any(), anyString())).thenThrow(new RuntimeException("Phase3c failure"));

            assertThatThrownBy(() -> service.publishImport("test-id"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Phase3c failure");

            // updateById must not be called — no dirty "published" status
            verify(mapper, never()).updateById(any());
            // Later publishers should never be called
            verify(phase3cLifecyclePublisher, never()).publish(anyString(), any(), anyString());
            verify(phase3dPublisher, never()).publish(anyString(), any(), anyString());
        }

        @Test
        @DisplayName("should roll back when phase3dPublisher throws after previous publishers succeed")
        void publishImportRollbackWhenPhase3dFails() {
            ExchangeImportPO po = ExchangeImportPO.builder()
                    .id("test-id")
                    .metadataId("test-ontology")
                    .validationStatus("passed")
                    .rawDocument(VALID_MINIMAL_JSON)
                    .build();

            when(mapper.selectById("test-id")).thenReturn(po);
            when(phase3bPublisher.publish(anyString(), any())).thenReturn(java.util.Map.of());
            when(phase3cPublisher.publish(anyString(), any(), anyString())).thenReturn(java.util.Map.of());
            when(phase3cLifecyclePublisher.publish(anyString(), any(), anyString())).thenReturn(java.util.Map.of());
            when(phase3dPublisher.publish(anyString(), any(), anyString())).thenThrow(new RuntimeException("Phase3d failure"));

            assertThatThrownBy(() -> service.publishImport("test-id"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Phase3d failure");

            // updateById must not be called — no dirty "published" status
            verify(mapper, never()).updateById(any());
            // Verify execution order: phase3b → phase3c → phase3cLifecycle were all called before the crash
            InOrder inOrder = inOrder(phase3bPublisher, phase3cPublisher, phase3cLifecyclePublisher, phase3dPublisher);
            inOrder.verify(phase3bPublisher).publish(anyString(), any());
            inOrder.verify(phase3cPublisher).publish(anyString(), any(), anyString());
            inOrder.verify(phase3cLifecyclePublisher).publish(anyString(), any(), anyString());
            inOrder.verify(phase3dPublisher).publish(anyString(), any(), anyString());
            // No further ordered calls after the exception
            verify(mapper, never()).updateById(any());
        }
    }

    @Nested
    @DisplayName("@Transactional annotation verification")
    class TransactionalAnnotationTests {

        @Test
        @DisplayName("service class should be annotated with @Transactional")
        void classHasTransactional() {
            Transactional annotation = ExchangeImportService.class.getAnnotation(Transactional.class);
            assertThat(annotation).as("ExchangeImportService should have @Transactional at class level").isNotNull();
        }

        @Test
        @DisplayName("importExchange() should inherit class-level @Transactional")
        void importExchangeInheritsTransactional() throws Exception {
            Method method = ExchangeImportService.class.getMethod("importExchange", String.class, String.class);
            Transactional annotation = method.getAnnotation(Transactional.class);
            // No method-level annotation = inherits class-level @Transactional
            assertThat(annotation).as("importExchange should not have its own @Transactional — inherits class-level").isNull();
        }

        @Test
        @DisplayName("publishImport() should inherit class-level @Transactional")
        void publishImportInheritsTransactional() throws Exception {
            Method method = ExchangeImportService.class.getMethod("publishImport", String.class);
            Transactional annotation = method.getAnnotation(Transactional.class);
            // No method-level annotation = inherits class-level @Transactional
            assertThat(annotation).as("publishImport should not have its own @Transactional — inherits class-level").isNull();
        }

        @Test
        @DisplayName("getImportStatus() should have @Transactional(readOnly = true)")
        void getImportStatusHasReadOnlyTransactional() throws Exception {
            Method method = ExchangeImportService.class.getMethod("getImportStatus", String.class);
            Transactional annotation = method.getAnnotation(Transactional.class);
            assertThat(annotation).as("getImportStatus should have @Transactional(readOnly = true)").isNotNull();
            assertThat(annotation.readOnly()).as("getImportStatus should be readOnly=true").isTrue();
        }

        @Test
        @DisplayName("validateOnly() should have @Transactional(readOnly = true)")
        void validateOnlyHasReadOnlyTransactional() throws Exception {
            Method method = ExchangeImportService.class.getMethod("validateOnly", String.class, String.class);
            Transactional annotation = method.getAnnotation(Transactional.class);
            assertThat(annotation).as("validateOnly should have @Transactional(readOnly = true)").isNotNull();
            assertThat(annotation.readOnly()).as("validateOnly should be readOnly=true").isTrue();
        }

        @Test
        @DisplayName("all publishers should have @Transactional for same-transaction participation")
        void publishersHaveTransactional() {
            assertThat(ExchangePhase3bPublisher.class.getAnnotation(Transactional.class))
                    .as("ExchangePhase3bPublisher should have @Transactional").isNotNull();
            assertThat(ExchangePhase3cPublisher.class.getAnnotation(Transactional.class))
                    .as("ExchangePhase3cPublisher should have @Transactional").isNotNull();
            assertThat(ExchangePhase3cLifecyclePublisher.class.getAnnotation(Transactional.class))
                    .as("ExchangePhase3cLifecyclePublisher should have @Transactional").isNotNull();
            assertThat(ExchangePhase3dPublisher.class.getAnnotation(Transactional.class))
                    .as("ExchangePhase3dPublisher should have @Transactional").isNotNull();
        }
    }
}

package com.ontology.platform.application.service.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.dto.imports.ExchangeImportResponse;
import com.ontology.platform.infrastructure.persistence.ExchangeImportPO;
import com.ontology.platform.infrastructure.persistence.ExchangeImportPOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * End-to-end integration test for the full v2 exchange pipeline.
 * <p>
 * Reads the golden JSON fixture ({@code docs/import/manufacturing-exchange-v2.json})
 * and exercises the complete lifecycle:
 * <ol>
 *   <li>Import ({@link ExchangeImportService#importExchange(String, String)})</li>
 *   <li>Verify status ({@link ExchangeImportService#getImportStatus(String)})</li>
 *   <li>Publish ({@link ExchangeImportService#publishImport(String)})</li>
 * </ol>
 * <p>
 * Follows the project's established test pattern ({@code @ExtendWith(MockitoExtension.class)})
 * with {@link ExchangeImportPOMapper} mocked. The mapper is a thin MyBatis-Plus interface,
 * so mocking it isolates the service logic while exercising the full decision pipeline.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeImportService E2E Pipeline Test")
class ExchangeImportServiceE2ETest {

    /**
     * Path to the golden JSON fixture, relative to the project root.
     * When Maven runs tests from the module directory, we need to go up one level.
     */
    private static final String GOLDEN_JSON_PATH = "../docs/import/manufacturing-exchange-v2.json";

    /**
     * Cached golden JSON content — read once for the entire test class.
     */
    private static String goldenJson;

    @Mock
    private ExchangeImportPOMapper mapper;

    private ObjectMapper objectMapper;

    private ExchangeImportService service;

    @Captor
    private ArgumentCaptor<ExchangeImportPO> poCaptor;

    @Captor
    private ArgumentCaptor<ExchangeImportPO> updateCaptor;

    @BeforeEach
    void setUp() throws IOException {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        service = new ExchangeImportService(mapper, objectMapper);

        // Read the golden JSON fixture once and cache it
        if (goldenJson == null) {
            goldenJson = Files.readString(Path.of(GOLDEN_JSON_PATH));
        }
    }

    // ---------------------------------------------------------------
    // Full pipeline test
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Full v2 exchange pipeline: import → verify → publish")
    void fullExchangePipeline() {
        // ── Step 1: Import ──────────────────────────────────────
        when(mapper.insert(any())).thenReturn(1);

        ExchangeImportResponse importResponse = service.importExchange(goldenJson, "strict");

        // Verify import result
        assertThat(importResponse).isNotNull();
        assertThat(importResponse.getId()).isNotBlank();
        assertThat(importResponse.getStatus()).isEqualTo("passed");
        // The golden fixture contains exactly 6 entities:
        //   production-order, operation, material, bom, routing, vo-quantity
        assertThat(importResponse.getTotalEntities()).isEqualTo(6);
        assertThat(importResponse.getWarnings()).isEqualTo(0);

        // Capture the saved PO and verify its fields
        verify(mapper).insert(poCaptor.capture());
        ExchangeImportPO savedPo = poCaptor.getValue();
        assertThat(savedPo.getId()).isEqualTo(importResponse.getId());
        assertThat(savedPo.getMetadataId()).isEqualTo("manufacturing-ontology");
        assertThat(savedPo.getMetadataName()).isEqualTo("生产制造本体");
        assertThat(savedPo.getMetadataVersion()).isEqualTo("0.1.0");
        assertThat(savedPo.getMetadataSource()).isEqualTo("ontology-designer");
        assertThat(savedPo.getMetadataStatus()).isEqualTo("draft");
        assertThat(savedPo.getProjectId()).isEqualTo("manufacturing-project");
        assertThat(savedPo.getProjectName()).isEqualTo("生产制造本体");
        assertThat(savedPo.getRawDocument()).isEqualTo(goldenJson);
        assertThat(savedPo.getValidationStatus()).isEqualTo("passed");
        assertThat(savedPo.getValidationReport()).contains("\"totalEntities\":6");
        assertThat(savedPo.getImportedAt()).isNotNull();
        assertThat(savedPo.getPublishedAt()).isNull();

        // ── Step 2: Verify status ───────────────────────────────
        ExchangeImportPO statusPo = ExchangeImportPO.builder()
                .id(importResponse.getId())
                .metadataId("manufacturing-ontology")
                .metadataName("生产制造本体")
                .metadataVersion("0.1.0")
                .metadataSource("ontology-designer")
                .metadataStatus("draft")
                .projectId("manufacturing-project")
                .projectName("生产制造本体")
                .rawDocument(goldenJson)
                .validationStatus("passed")
                .validationReport("{\"totalEntities\":6,\"warnings\":0}")
                .build();

        when(mapper.selectById(importResponse.getId())).thenReturn(statusPo);

        ExchangeImportResponse statusResponse = service.getImportStatus(importResponse.getId());

        assertThat(statusResponse.getId()).isEqualTo(importResponse.getId());
        assertThat(statusResponse.getStatus()).isEqualTo("passed");
        assertThat(statusResponse.getTotalEntities()).isEqualTo(6);
        assertThat(statusResponse.getWarnings()).isEqualTo(0);

        // ── Step 3: Publish ─────────────────────────────────────
        // The status query already configured mapper.selectById for this ID;
        // publishImport also calls selectById, then updateById.
        when(mapper.updateById(any())).thenReturn(1);

        ExchangeImportResponse publishResponse = service.publishImport(importResponse.getId());

        assertThat(publishResponse.getId()).isEqualTo(importResponse.getId());
        assertThat(publishResponse.getStatus()).isEqualTo("published");
        assertThat(publishResponse.getTotalEntities()).isEqualTo(6);
        assertThat(publishResponse.getWarnings()).isEqualTo(0);

        // Verify the update set the correct fields
        verify(mapper).updateById(updateCaptor.capture());
        ExchangeImportPO updatedPo = updateCaptor.getValue();
        assertThat(updatedPo.getMetadataStatus()).isEqualTo("published");
        assertThat(updatedPo.getPublishedAt()).isNotNull();
        assertThat(updatedPo.getUpdatedAt()).isNotNull();
    }

    // ---------------------------------------------------------------
    // Error-path tests using the golden JSON
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Should reject publish of non-existent import")
    void publishImportNotFound() {
        when(mapper.selectById("nonexistent")).thenReturn(null);

        assertThatThrownBy(() -> service.publishImport("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ExchangeImport");
    }

    @Test
    @DisplayName("Should reject publish of failed import")
    void publishImportFailed() {
        ExchangeImportPO failedPo = ExchangeImportPO.builder()
                .id("test-id")
                .validationStatus("failed")
                .build();

        when(mapper.selectById("test-id")).thenReturn(failedPo);

        assertThatThrownBy(() -> service.publishImport("test-id"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot publish");
    }

    @Test
    @DisplayName("Should throw when golden JSON is not found at expected path")
    void goldenJsonFileNotFound() {
        assertThatThrownBy(() -> Files.readString(Path.of("docs/import/nonexistent-file.json")))
                .isInstanceOf(IOException.class);
    }
}

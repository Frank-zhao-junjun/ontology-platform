package com.ontology.platform.infrastructure.imports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P2-I04 / P2 #6: 端到端 Exchange 导入测试
 *
 * <p>验证 {@link ExcelExchangeMapper#toJson(OntologyExchangeDocument)} 和
 * {@link ExcelExchangeMapper#mapFromParsedData(String)} 的完整序列化/反序列化链路。
 *
 * <p>注：实际 Excel 文件解析已由 {@code ExcelBImportAdapterTest} 等覆盖，
 * 本测试专注于 ExcelExchangeMapper 的 JSON 管道完整性。
 */
class ExcelExchangeE2ETest {

    private ExcelExchangeMapper mapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mapper = new ExcelExchangeMapper(
                new ExcelOntologyImportAdapter(objectMapper),
                new ExcelBImportAdapter(objectMapper),
                new ExcelCImportAdapter(objectMapper),
                objectMapper
        );
    }

    @Test
    @DisplayName("E2E-EXCEL-1: OntologyExchangeDocument → JSON → 反序列化完整")
    void roundTrip_shouldPreserveStructure() throws IOException {
        var entity = OntologyExchangeDocument.Entity.builder()
                .id("OT-001")
                .name("生产订单")
                .entityRole("aggregate_root")
                .description("生产订单聚合根")
                .build();

        var dataModel = OntologyExchangeDocument.DataModel.builder()
                .id("test-data")
                .name("Test DataModel")
                .version("0.1.0")
                .entities(java.util.List.of(entity))
                .build();

        var project = OntologyExchangeDocument.OntologyProject.builder()
                .id("test-project")
                .name("测试项目")
                .description("端到端测试")
                .dataModel(dataModel)
                .build();

        var doc = OntologyExchangeDocument.builder()
                .apiVersion("ontology.platform/v2")
                .kind("OntologyExchange")
                .metadata(OntologyExchangeDocument.Metadata.builder()
                        .id("e2e-test")
                        .version("0.1.0")
                        .name("端到端测试")
                        .source("excel-import")
                        .status("draft")
                        .projectId("test-project")
                        .exportedAt("2026-06-20T00:00:00Z")
                        .build())
                .spec(OntologyExchangeDocument.Spec.builder()
                        .project(project)
                        .build())
                .build();

        // 序列化
        String json = mapper.toJson(doc);
        assertNotNull(json);
        assertFalse(json.isBlank());
        assertTrue(json.contains("生产订单"), "JSON should contain entity name");

        // 反序列化
        OntologyExchangeDocument back = mapper.mapFromParsedData(json);
        assertNotNull(back);
        assertEquals("e2e-test", back.getMetadata().getId());
        assertNotNull(back.getSpec());
        assertNotNull(back.getSpec().getProject());
        assertNotNull(back.getSpec().getProject().getDataModel());
        assertEquals(1, back.getSpec().getProject().getDataModel().getEntities().size());
        assertEquals("生产订单", back.getSpec().getProject().getDataModel().getEntities().get(0).getName());
    }
}

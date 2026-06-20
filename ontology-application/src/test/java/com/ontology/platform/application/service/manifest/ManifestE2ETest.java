package com.ontology.platform.application.service.manifest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.manifest.*;
import com.ontology.platform.domain.vo.manifest.ManifestDocument;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * P2 #6 — 端到端导入测试（端到端测试）
 *
 * <p>使用项目1格式的真实 Manifest JSON fixture，测试全生命周期：
 * import → preview → publish → export
 *
 * <p>ManifestService 使用内存存储，无需 Docker/PostgreSQL。
 */
@TestMethodOrder(OrderAnnotation.class)
class ManifestE2ETest {

    private static ManifestService manifestService;
    private static String draftId;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeAll
    static void setUp() throws Exception {
        manifestService = new ManifestServiceImpl();
        // Verify fixture is parseable
        Path fixture = Path.of("src/test/resources/fixtures/manifest-e2e.json");
        assertThat(fixture).exists();
        String content = Files.readString(fixture);
        ManifestDocument doc = MAPPER.readValue(content, ManifestDocument.class);
        assertThat(doc.getMetadata()).isNotNull();
        assertThat(doc.getMetadata().getId()).isEqualTo("it-001");
    }

    @Test
    @Order(1)
    @DisplayName("E2E-1: 导入 Manifest → 校验结果")
    void importManifest() throws Exception {
        String rawContent = Files.readString(
                Path.of("src/test/resources/fixtures/manifest-e2e.json"));

        ImportManifestRequest request = ImportManifestRequest.builder()
                .sourceFormat("JSON")
                .rawContent(rawContent)
                .createdBy("e2e-test")
                .build();

        ImportManifestResponse response = manifestService.importManifest(request);

        assertThat(response.isValid()).isTrue();
        assertThat(response.getDraftId()).isNotBlank();
        assertThat(response.getErrors()).isNullOrEmpty();

        // 验证导入计数
        assertThat(response.getImportedCounts())
                .containsEntry("objectTypes", 2)  // production-order + operation
                .containsEntry("actions", 2)       // release-order + complete-order
                .containsEntry("events", 2)        // order-released + order-completed
                .containsEntry("rules", 1)         // qty-positive
                .containsEntry("stateMachines", 1); // sm-production-order

        draftId = response.getDraftId();
    }

    @Test
    @Order(2)
    @DisplayName("E2E-2: 预览导入 → 返回变更记录")
    void previewManifest() {
        assertThat(draftId).as("需要先执行导入").isNotBlank();

        ManifestPreviewResponse preview = manifestService.preview(draftId);

        assertThat(preview.getImportId()).isEqualTo(draftId);
        assertThat(preview.getChanges()).isNotEmpty();
        assertThat(preview.getChanges().get(0).getChange()).contains("new import");
    }

    @Test
    @Order(3)
    @DisplayName("E2E-3: 发布 Manifest → 返回版本号")
    void publishManifest() {
        assertThat(draftId).as("需要先执行导入").isNotBlank();

        ManifestPublishResponse publish = manifestService.publish(draftId);

        assertThat(publish.getVersion()).isEqualTo("0.2.0");
        assertThat(publish.getPublishedAt()).isNotNull();
    }

    @Test
    @Order(4)
    @DisplayName("E2E-4: 导出已发布版本 → 返回完整 ManifestDocument")
    void exportManifest() {
        ManifestDocument doc = manifestService.export("0.2.0", "json");

        assertThat(doc).isNotNull();
        assertThat(doc.getMetadata().getId()).isEqualTo("it-001");
        assertThat(doc.getObjectTypes()).hasSize(2);
        assertThat(doc.getActions()).hasSize(2);
        assertThat(doc.getEvents()).hasSize(2);
        assertThat(doc.getRules()).hasSize(1);
        assertThat(doc.getStateMachines()).hasSize(1);
        assertThat(doc.getSpec().getDataSources()).hasSize(1);
        assertThat(doc.getSpec().getEpc()).hasSize(1);
    }

    @Test
    @Order(5)
    @DisplayName("E2E-5: 最少必须字段 Manifest → 校验通过")
    void importMinimalManifest() {
        String minimalJson = """
                {
                  "apiVersion": "ontology.platform/v1",
                  "kind": "OntologyManifest",
                  "metadata": {"id": "min-001", "name": "最小Manifest", "version": "1.0.0"},
                  "spec": {
                    "semantic": {
                      "objectTypes": [
                        {"id": "OT-001", "name": "最小实体", "kind": "aggregate_root"}
                      ]
                    }
                  }
                }
                """;

        ImportManifestResponse response = manifestService.importManifest(
                ImportManifestRequest.builder()
                        .sourceFormat("JSON")
                        .rawContent(minimalJson)
                        .createdBy("e2e-test")
                        .build());

        assertThat(response.isValid()).isTrue();
        assertThat(response.getImportedCounts())
                .containsEntry("objectTypes", 1);
    }

    @Test
    @Order(6)
    @DisplayName("E2E-6: 非法 JSON → valid=false + 错误信息")
    void importInvalidJson() {
        ImportManifestResponse response = manifestService.importManifest(
                ImportManifestRequest.builder()
                        .sourceFormat("JSON")
                        .rawContent("{not valid json")
                        .createdBy("e2e-test")
                        .build());

        assertThat(response.isValid()).isFalse();
        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors().get(0).getCode()).isEqualTo("PARSE");
    }
}

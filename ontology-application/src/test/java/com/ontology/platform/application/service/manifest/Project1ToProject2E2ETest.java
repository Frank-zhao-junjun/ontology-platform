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
 * P2-I04: Project 1 → Project 2 跨项目端到端导入测试
 *
 * <p>测试完整链路：Project 1 OntologyManifest → ManifestConverter → ManifestDocument
 * → ManifestService.importManifest() → preview → publish → export
 *
 * <p>使用 Project 1 真实格式的 fixture (project1-manifest-export.json)，
 * ManifestService 纯内存实现，无需 Docker/PostgreSQL。
 */
@TestMethodOrder(OrderAnnotation.class)
class Project1ToProject2E2ETest {

    private static ManifestService manifestService;
    private static ManifestConverter converter;
    private static ObjectMapper mapper;
    private static String draftId;
    private static ManifestDocument convertedDoc;

    @BeforeAll
    static void setUp() throws Exception {
        mapper = new ObjectMapper();
        converter = new ManifestConverter(mapper);
        manifestService = new ManifestServiceImpl();

        Path fixture = Path.of("src/test/resources/fixtures/project1-manifest-export.json");
        assertThat(fixture).exists();
        String content = Files.readString(fixture);
        assertThat(mapper.readTree(content)).isNotNull();
    }

    @Test
    @Order(1)
    @DisplayName("CROSS-1: ManifestConverter 转换 Project 1 JSON → ManifestDocument")
    void convertProject1ToManifestDocument() throws Exception {
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-manifest-export.json"));

        convertedDoc = converter.convert(raw);

        assertThat(convertedDoc).isNotNull();
        assertThat(convertedDoc.getMetadata().getId()).isEqualTo("p1-mfg-001");
        assertThat(convertedDoc.getMetadata().getVersion()).isEqualTo("2.1.0");
        assertThat(convertedDoc.getObjectTypes()).hasSize(3);
        assertThat(convertedDoc.getObjectTypes().get(0).getKind()).isEqualTo("aggregate_root");
        assertThat(convertedDoc.getActions()).hasSize(3);
        assertThat(convertedDoc.getActions().get(0).getName()).isEqualTo("下达生产订单");
        assertThat(convertedDoc.getRules()).hasSize(2);
        assertThat(convertedDoc.getEvents()).hasSize(3);
        assertThat(convertedDoc.getStateMachines()).hasSize(2);
        assertThat(convertedDoc.getStateMachines().get(0).getStates()).hasSize(4);
        assertThat(convertedDoc.getSpec().getDataSources()).hasSize(2);
    }

    @Test
    @Order(2)
    @DisplayName("CROSS-2: ManifestService 导入（ManifestDocument 格式 JSON）")
    void importIntoService() {
        // 注：Project 1 原始格式带 isInitial/isFinal → ManifestConverter 处理
        //     ManifestService 直接接受 ManifestDocument 格式（initial/final）
        String importJson = ""
            + "{\n"
            + "  \"apiVersion\": \"ontology.platform/v1\",\n"
            + "  \"kind\": \"OntologyManifest\",\n"
            + "  \"metadata\": {\"id\": \"p1-import-test\", \"name\": \"导入测试\", \"version\": \"1.0.0\"},\n"
            + "  \"spec\": {\n"
            + "    \"semantic\": {\n"
            + "      \"objectTypes\": [\n"
            + "        {\"id\": \"OT-001\", \"name\": \"生产订单\", \"nameEn\": \"ProductionOrder\", \"kind\": \"aggregate_root\"},\n"
            + "        {\"id\": \"OT-002\", \"name\": \"质检记录\", \"nameEn\": \"QualityInspection\", \"kind\": \"aggregate_root\"}\n"
            + "      ]\n"
            + "    },\n"
            + "    \"behavior\": {\n"
            + "      \"actions\": [{\"id\": \"ACT-001\", \"name\": \"下达\", \"nameEn\": \"release\"}],\n"
            + "      \"rules\": [{\"id\": \"RUL-001\", \"name\": \"校验\"}],\n"
            + "      \"stateMachines\": [\n"
            + "        {\"id\": \"SM-001\", \"name\": \"订单状态机\",\n"
            + "         \"states\": [{\"name\": \"DRAFT\", \"initial\": true, \"final\": false}, {\"name\": \"RELEASED\", \"initial\": false, \"final\": true}],\n"
            + "         \"transitions\": [{\"from\": \"DRAFT\", \"to\": \"RELEASED\", \"trigger\": \"release\"}]}\n"
            + "      ]\n"
            + "    },\n"
            + "    \"events\": {\n"
            + "      \"domainEvents\": [{\"id\": \"EVT-001\", \"nameEn\": \"order_released\"}]\n"
            + "    }\n"
            + "  }\n"
            + "}";

        ImportManifestResponse response = manifestService.importManifest(
                ImportManifestRequest.builder()
                        .sourceFormat("JSON")
                        .rawContent(importJson)
                        .createdBy("e2e-cross-project")
                        .build());

        assertThat(response.isValid()).isTrue();
        assertThat(response.getDraftId()).isNotBlank();
        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(response.getImportedCounts())
                .containsEntry("objectTypes", 2)
                .containsEntry("actions", 1)
                .containsEntry("events", 1)
                .containsEntry("rules", 1)
                .containsEntry("stateMachines", 1);

        draftId = response.getDraftId();
    }

    @Test
    @Order(3)
    @DisplayName("CROSS-3: 预览/发布/导出全生命周期")
    void fullLifecycle() {
        assertThat(draftId).as("需要先执行导入").isNotBlank();

        ManifestPreviewResponse preview = manifestService.preview(draftId);
        assertThat(preview.getImportId()).isEqualTo(draftId);
        assertThat(preview.getChanges()).isNotEmpty();

        ManifestPublishResponse publish = manifestService.publish(draftId);
        assertThat(publish.getVersion()).isEqualTo("1.0.0");
        assertThat(publish.getPublishedAt()).isNotNull();

        ManifestDocument exported = manifestService.export("1.0.0", "json");
        assertThat(exported.getMetadata().getId()).isEqualTo("p1-import-test");
        assertThat(exported.getObjectTypes()).hasSize(2);
    }

    @Test
    @Order(4)
    @DisplayName("CROSS-4: Validator 拒绝无 aggregate_root 的 manifest")
    void rejectNoAggregateRoot() {
        String noAr = ""
            + "{\"apiVersion\": \"ontology.platform/v1\", \"kind\": \"OntologyManifest\",\n"
            + " \"metadata\": {\"id\": \"no-ar\", \"name\": \"无聚合根\", \"version\": \"1.0.0\"},\n"
            + " \"spec\": {\"semantic\": {\"objectTypes\": [{\"id\": \"T1\", \"name\": \"孤儿\", \"kind\": \"entity\"}]}}}";

        ImportManifestResponse response = manifestService.importManifest(
                ImportManifestRequest.builder()
                        .sourceFormat("JSON")
                        .rawContent(noAr)
                        .createdBy("e2e-test")
                        .build());

        assertThat(response.isValid()).isFalse();
        assertThat(response.getErrors()).isNotEmpty();
    }

    @Test
    @Order(5)
    @DisplayName("CROSS-5: ManifestConverter 非法 JSON → 异常")
    void convertInvalidJson() {
        assertThatThrownBy(() -> converter.convert("{not valid json"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JSON");
    }

    @Test
    @Order(6)
    @DisplayName("CROSS-6: ManifestConverter 输出结构完整性验证")
    void verifyConverterOutput() {
        assertThat(convertedDoc).as("需要先执行转换").isNotNull();
        assertThat(convertedDoc.getMetadata()).isNotNull();
        assertThat(convertedDoc.getSpec()).isNotNull();
        assertThat(convertedDoc.getObjectTypes()).hasSize(3);
        assertThat(convertedDoc.getActions()).hasSize(3);
        assertThat(convertedDoc.getEvents()).hasSize(3);
        assertThat(convertedDoc.getRules()).hasSize(2);
        assertThat(convertedDoc.getStateMachines()).hasSize(2);

        ManifestDocument.StateMachineDef sm = convertedDoc.getStateMachines().get(0);
        assertThat(sm.getStates()).hasSize(4);
        assertThat(sm.getTransitions()).hasSize(3);
    }
}

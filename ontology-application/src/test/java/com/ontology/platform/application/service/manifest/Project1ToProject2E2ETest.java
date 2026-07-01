package com.ontology.platform.application.service.manifest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.manifest.*;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
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

    // ==================== Fixture-2: Minimal Ontology Tests ====================

    @Test
    @Order(7)
    @DisplayName("CROSS-7: 最小 fixture 通过 ManifestConverter 转换")
    void convertMinimalFixture() throws Exception {
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-minimal-fixture.json"));

        ManifestDocument doc = converter.convert(raw);

        assertThat(doc).isNotNull();
        assertThat(doc.getMetadata().getId()).isEqualTo("p1-minimal-001");
        assertThat(doc.getMetadata().getVersion()).isEqualTo("1.0.0");
        assertThat(doc.getMetadata().getName()).isEqualTo("最小本体");

        // Only 1 aggregate_root
        assertThat(doc.getObjectTypes()).hasSize(1);
        assertThat(doc.getObjectTypes().get(0).getKind()).isEqualTo("aggregate_root");
        assertThat(doc.getObjectTypes().get(0).getId()).isEqualTo("customer");

        // 1 stateMachine with 2 states and 1 transition
        assertThat(doc.getStateMachines()).hasSize(1);
        ManifestDocument.StateMachineDef sm = doc.getStateMachines().get(0);
        assertThat(sm.getId()).isEqualTo("sm-customer-lifecycle");
        assertThat(sm.getStates()).hasSize(2);
        assertThat(sm.getTransitions()).hasSize(1);

        // 1 action
        assertThat(doc.getActions()).hasSize(1);
        assertThat(doc.getActions().get(0).getId()).isEqualTo("deactivate-customer");

        // 1 rule
        assertThat(doc.getRules()).hasSize(1);
        assertThat(doc.getRules().get(0).getId()).isEqualTo("rule-no-pending-orders");

        // 1 event
        assertThat(doc.getEvents()).hasSize(1);
        assertThat(doc.getEvents().get(0).getId()).isEqualTo("evt-customer-deactivated");
    }

    @Test
    @Order(8)
    @DisplayName("CROSS-8: 最小 fixture 通过 ManifestService 导入并验证计数")
    void importMinimalFixture() throws Exception {
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-minimal-fixture.json"));

        // 先通过 converter 转成 ManifestDocument，再序列化为 ManifestService 可接受的格式
        ManifestDocument converted = converter.convert(raw);
        String manifestDocJson = mapper.writeValueAsString(converted);

        ImportManifestResponse response = manifestService.importManifest(
                ImportManifestRequest.builder()
                        .sourceFormat("JSON")
                        .rawContent(manifestDocJson)
                        .createdBy("e2e-minimal-fixture")
                        .build());

        assertThat(response.isValid()).isTrue();
        assertThat(response.getDraftId()).isNotBlank();
        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(response.getImportedCounts())
                .containsEntry("objectTypes", 1)
                .containsEntry("actions", 1)
                .containsEntry("events", 1)
                .containsEntry("rules", 1)
                .containsEntry("stateMachines", 1);
    }

    @Test
    @Order(9)
    @DisplayName("CROSS-9: 最小 fixture 全生命周期（预览→发布→导出→比对）")
    void minimalFixtureFullLifecycle() throws Exception {
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-minimal-fixture.json"));

        ManifestDocument converted = converter.convert(raw);
        String manifestDocJson = mapper.writeValueAsString(converted);

        // Import
        ImportManifestResponse importResp = manifestService.importManifest(
                ImportManifestRequest.builder()
                        .sourceFormat("JSON")
                        .rawContent(manifestDocJson)
                        .createdBy("e2e-minimal-lifecycle")
                        .build());
        assertThat(importResp.isValid()).isTrue();
        String draftId = importResp.getDraftId();

        // Preview
        ManifestPreviewResponse preview = manifestService.preview(draftId);
        assertThat(preview.getImportId()).isEqualTo(draftId);
        assertThat(preview.getChanges()).isNotEmpty();

        // Publish
        ManifestPublishResponse publish = manifestService.publish(draftId);
        assertThat(publish.getVersion()).isEqualTo("1.0.0");
        assertThat(publish.getPublishedAt()).isNotNull();

        // Export
        ManifestDocument exported = manifestService.export("1.0.0", "json");
        assertThat(exported).isNotNull();
        assertThat(exported.getMetadata().getId()).isEqualTo("p1-minimal-001");
        assertThat(exported.getObjectTypes()).hasSize(1);
        assertThat(exported.getActions()).hasSize(1);
        assertThat(exported.getEvents()).hasSize(1);

        // Round-trip: exported JSON should re-parse to equivalent document
        String exportedJson = mapper.writeValueAsString(exported);
        ManifestDocument reParsed = mapper.readValue(exportedJson, ManifestDocument.class);
        assertThat(reParsed.getMetadata().getId()).isEqualTo(exported.getMetadata().getId());
        assertThat(reParsed.getObjectTypes()).hasSize(exported.getObjectTypes().size());
    }

    // ==================== Fixture-3: Full Domain (V12+V13+V14) Tests ====================

    @Test
    @Order(10)
    @DisplayName("CROSS-10: 全领域 fixture 直接解析为 ManifestDocument → 验证所有域(V12+V13+V14)")
    void parseFullDomainFixtureAsManifestDocument() throws Exception {
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-full-domain-fixture.json"));

        // 全领域 fixture 使用 Project 2 ManifestDocument 格式，可直接解析
        ManifestDocument doc = mapper.readValue(raw, ManifestDocument.class);

        assertThat(doc).isNotNull();
        assertThat(doc.getMetadata().getId()).isEqualTo("p1-full-domain-001");
        assertThat(doc.getMetadata().getVersion()).isEqualTo("3.0.0");
        assertThat(doc.getMetadata().getBoundedContext()).isEqualTo("enterprise");
        assertThat(doc.getMetadata().getDomainTags()).contains("epc", "organization", "semantic");

        // Semantic: boundedContext + businessScenarios + valueObjects + 4 objectTypes
        assertThat(doc.getSpec().getSemantic()).isNotNull();
        assertThat(doc.getSpec().getSemantic().getBoundedContext()).isNotNull();
        assertThat(doc.getSpec().getSemantic().getBoundedContext().getId()).isEqualTo("bc-enterprise");
        assertThat(doc.getSpec().getSemantic().getBusinessScenarios()).hasSize(2);
        assertThat(doc.getSpec().getSemantic().getValueObjects()).hasSize(2);

        // Object types: 4 total (3 aggregate_root + 1 child_entity)
        assertThat(doc.getObjectTypes()).hasSize(4);
        long aggRootCount = doc.getObjectTypes().stream()
                .filter(ot -> "aggregate_root".equals(ot.getKind())).count();
        assertThat(aggRootCount).isEqualTo(3);
        // Each object type has properties (V13 attribute model)
        assertThat(doc.getObjectTypes().get(0).getProperties()).isNotEmpty();

        // Behavior: 4 actions, 3 rules, 2 stateMachines
        assertThat(doc.getActions()).hasSize(4);
        assertThat(doc.getActions().get(0).getName()).isEqualTo("下达生产订单");
        assertThat(doc.getRules()).hasSize(3);
        assertThat(doc.getStateMachines()).hasSize(2);

        // State machine details
        ManifestDocument.StateMachineDef smProd = doc.getStateMachines().get(0);
        assertThat(smProd.getStates()).hasSize(4);
        assertThat(smProd.getTransitions()).hasSize(3);

        ManifestDocument.StateMachineDef smInsp = doc.getStateMachines().get(1);
        assertThat(smInsp.getStates()).hasSize(3);
        assertThat(smInsp.getTransitions()).hasSize(2);

        // Events: 4 events + 2 causalities
        assertThat(doc.getEvents()).hasSize(4);
        assertThat(doc.getSpec().getEvents().getCausalities()).hasSize(2);

        // Governance: 3 roles + 2 agentPolicies (V12 organization model)
        assertThat(doc.getSpec().getGovernance()).isNotNull();
        assertThat(doc.getSpec().getGovernance().getRoles()).hasSize(3);
        assertThat(doc.getSpec().getGovernance().getRoles().get(0).getId()).isEqualTo("role-prod-manager");
        assertThat(doc.getSpec().getGovernance().getAgentPolicies()).hasSize(2);

        // DataSources: 3
        assertThat(doc.getSpec().getDataSources()).hasSize(3);
        assertThat(doc.getSpec().getDataSources().get(0).getId()).isEqualTo("ds-sap");
        assertThat(doc.getSpec().getDataSources().get(0).getConnectionConfig()).isNotNull();

        // EPC: 2 orchestrations (V14 EPC chains)
        assertThat(doc.getSpec().getEpc()).hasSize(2);
        assertThat(doc.getSpec().getEpc().get(0).getFlowName()).contains("V14 EPC链");
        assertThat(doc.getSpec().getEpc().get(0).getSteps()).hasSize(3);
        assertThat(doc.getSpec().getEpc().get(1).getSteps()).hasSize(2);
    }

    @Test
    @Order(11)
    @DisplayName("CROSS-11: 全领域 fixture 通过 ManifestService 导入并验证所有计数")
    void importFullDomainFixture() throws Exception {
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-full-domain-fixture.json"));

        // 全领域 fixture 使用的是 Project 2 ManifestDocument 格式，可直接导入
        ImportManifestResponse response = manifestService.importManifest(
                ImportManifestRequest.builder()
                        .sourceFormat("JSON")
                        .rawContent(raw)
                        .createdBy("e2e-full-domain")
                        .build());

        assertThat(response.isValid()).isTrue();
        assertThat(response.getDraftId()).isNotBlank();
        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(response.getImportedCounts())
                .containsEntry("objectTypes", 4)
                .containsEntry("actions", 4)
                .containsEntry("events", 4)
                .containsEntry("rules", 3)
                .containsEntry("stateMachines", 2);
    }

    @Test
    @Order(12)
    @DisplayName("CROSS-12: 全领域 fixture 全生命周期 + 导出内容与导入前等效比对")
    void fullDomainFixtureFullLifecycle() throws Exception {
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-full-domain-fixture.json"));

        // Import
        ImportManifestResponse importResp = manifestService.importManifest(
                ImportManifestRequest.builder()
                        .sourceFormat("JSON")
                        .rawContent(raw)
                        .createdBy("e2e-full-lifecycle")
                        .build());
        assertThat(importResp.isValid()).isTrue();
        String draftId = importResp.getDraftId();

        // Preview
        ManifestPreviewResponse preview = manifestService.preview(draftId);
        assertThat(preview.getImportId()).isEqualTo(draftId);

        // Publish
        ManifestPublishResponse publish = manifestService.publish(draftId);
        assertThat(publish.getVersion()).isEqualTo("3.0.0");
        assertThat(publish.getPublishedAt()).isNotNull();

        // Export
        ManifestDocument exported = manifestService.export("3.0.0", "json");
        assertThat(exported).isNotNull();
        assertThat(exported.getMetadata().getId()).isEqualTo("p1-full-domain-001");
        assertThat(exported.getObjectTypes()).hasSize(4);
        assertThat(exported.getActions()).hasSize(4);
        assertThat(exported.getEvents()).hasSize(4);
        assertThat(exported.getRules()).hasSize(3);
        assertThat(exported.getStateMachines()).hasSize(2);

        // Verify V12-V14 sections preserved in round-trip
        assertThat(exported.getSpec().getGovernance()).isNotNull();
        assertThat(exported.getSpec().getGovernance().getRoles()).hasSize(3);
        assertThat(exported.getSpec().getGovernance().getAgentPolicies()).hasSize(2);
        assertThat(exported.getSpec().getDataSources()).hasSize(3);
        assertThat(exported.getSpec().getEpc()).hasSize(2);

        // Round-trip serialization equivalence
        String exportedJson = mapper.writeValueAsString(exported);
        ManifestDocument reParsed = mapper.readValue(exportedJson, ManifestDocument.class);
        assertThat(reParsed.getMetadata().getId()).isEqualTo(exported.getMetadata().getId());
        assertThat(reParsed.getObjectTypes().get(0).getId())
                .isEqualTo(exported.getObjectTypes().get(0).getId());
    }

    // ==================== Fixture-4: Tags Field Import Test ====================
    //
    // 背景: 项目1 有 tags 字段，但项目2 的 ManifestDocument.Metadata 无对应字段
    // 映射: tags → 无直接对应 — ❌ 可存入 extended_data (见 project1-to-project2-mapping.md)
    // 现状: ManifestConverter 在 metadata 中仅映射 id/name/displayName/version/description
    //       其余字段（含 tags/domainTags）被静默丢弃。
    // 结论: tags 导入目前是已知差距（known gap），需未来在 Metadata 中增加 extendedData
    //       或 tags 字段后修复。

    @Test
    @Order(13)
    @DisplayName("CROSS-13: tags 字段导入验证 — 已知差距（标签在转换中被丢弃）")
    void tagsFieldIsKnownGap() throws Exception {
        // 读取含 tags 的 Project 1 格式 fixture
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-tags-fixture.json"));

        // 转换
        ManifestDocument doc = converter.convert(raw);

        // 基本转换验证 — 核心字段仍正常映射
        assertThat(doc).isNotNull();
        assertThat(doc.getMetadata().getId()).isEqualTo("p1-tags-test-001");
        assertThat(doc.getMetadata().getVersion()).isEqualTo("1.0.0");
        assertThat(doc.getMetadata().getName()).isEqualTo("标签导入测试");
        assertThat(doc.getObjectTypes()).hasSize(1);
        assertThat(doc.getObjectTypes().get(0).getKind()).isEqualTo("aggregate_root");
        assertThat(doc.getActions()).hasSize(1);
        assertThat(doc.getRules()).hasSize(1);
        assertThat(doc.getEvents()).hasSize(1);
        assertThat(doc.getStateMachines()).hasSize(1);

        // 已知差距验证: tags/domainTags 未被保留
        // ManifestDocument.Metadata 当前没有 tags 或 extendedData 字段，
        // ManifestConverter 也跳过 tags/domainTags 的映射
        assertThat(doc.getMetadata().getDomainTags())
                .as("KNOWN GAP: tags/domainTags 在转换中被丢弃 — ManifestConverter 未映射 tags 字段，" +
                    "ManifestDocument.Metadata 也无对应存储字段（expect null or empty）")
                .isNullOrEmpty();
    }

    @Test
    @Order(14)
    @DisplayName("CROSS-14: tags fixture 通过 ManifestService 导入 — 验证即使标签丢失，导入仍可用")
    void tagsFixtureImportStillWorks() throws Exception {
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-tags-fixture.json"));

        // 通过 converter 转换后再导入
        ManifestDocument converted = converter.convert(raw);
        String manifestDocJson = mapper.writeValueAsString(converted);

        ImportManifestResponse response = manifestService.importManifest(
                ImportManifestRequest.builder()
                        .sourceFormat("JSON")
                        .rawContent(manifestDocJson)
                        .createdBy("e2e-tags-import")
                        .build());

        // 导入仍然有效 — 标签丢失不影响核心导入功能
        assertThat(response.isValid()).isTrue();
        assertThat(response.getDraftId()).isNotBlank();
        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(response.getImportedCounts())
                .containsEntry("objectTypes", 1)
                .containsEntry("actions", 1)
                .containsEntry("events", 1)
                .containsEntry("rules", 1)
                .containsEntry("stateMachines", 1);
    }

    @Test
    @Order(15)
    @DisplayName("CROSS-15: tags fixture 全生命周期 — 预览→发布→导出→比对")
    void tagsFixtureFullLifecycle() throws Exception {
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-tags-fixture.json"));

        ManifestDocument converted = converter.convert(raw);
        String manifestDocJson = mapper.writeValueAsString(converted);

        // Import
        ImportManifestResponse importResp = manifestService.importManifest(
                ImportManifestRequest.builder()
                        .sourceFormat("JSON")
                        .rawContent(manifestDocJson)
                        .createdBy("e2e-tags-lifecycle")
                        .build());
        assertThat(importResp.isValid()).isTrue();
        String draftId = importResp.getDraftId();

        // Preview
        ManifestPreviewResponse preview = manifestService.preview(draftId);
        assertThat(preview.getImportId()).isEqualTo(draftId);
        assertThat(preview.getChanges()).isNotEmpty();

        // Publish
        ManifestPublishResponse publish = manifestService.publish(draftId);
        assertThat(publish.getVersion()).isEqualTo("1.0.0");
        assertThat(publish.getPublishedAt()).isNotNull();

        // Export
        ManifestDocument exported = manifestService.export("1.0.0", "json");
        assertThat(exported).isNotNull();
        assertThat(exported.getMetadata().getId()).isEqualTo("p1-tags-test-001");
        assertThat(exported.getObjectTypes()).hasSize(1);
        assertThat(exported.getActions()).hasSize(1);
        assertThat(exported.getEvents()).hasSize(1);
        assertThat(exported.getRules()).hasSize(1);
        assertThat(exported.getStateMachines()).hasSize(1);

        // 额外验证: domainTags 在导出的文档中也为空（标签丢失持续到导出）
        assertThat(exported.getMetadata().getDomainTags())
                .as("KNOWN GAP: 标签信息在全生命周期中均丢失")
                .isNullOrEmpty();

        // Round-trip equivalence
        String exportedJson = mapper.writeValueAsString(exported);
        ManifestDocument reParsed = mapper.readValue(exportedJson, ManifestDocument.class);
        assertThat(reParsed.getMetadata().getId()).isEqualTo(exported.getMetadata().getId());
        assertThat(reParsed.getObjectTypes().get(0).getId())
                .isEqualTo(exported.getObjectTypes().get(0).getId());
    }

    // ==================== Fixture-5: Scenario/SubDomain Field Import Test ====================
    //
    // 背景: 项目1 的 Entity 有 scenario/subDomain 字段，但项目2 的 ManifestDocument.ObjectType 原无对应字段
    // 映射: scenario → ❌ 无直接对应; subDomain → ❌ 可用 parent_id 模拟层级
    //       (见 project1-to-project2-mapping.md)
    // 修复: ManifestDocument.ObjectType 新增 scenario/subDomain 字段，
    //       ManifestConverter 读取并保留这些字段，避免静默丢失。
    // 验证: CROSS-16/17/18 验证字段被保留且可通过全生命周期传递。

    @Test
    @Order(16)
    @DisplayName("CROSS-16: scenario/subDomain 字段被 ManifestConverter 保留")
    void scenarioSubDomainFieldsArePreserved() throws Exception {
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-scenario-subdomain-fixture.json"));

        ManifestDocument doc = converter.convert(raw);

        // 基本转换验证
        assertThat(doc).isNotNull();
        assertThat(doc.getMetadata().getId()).isEqualTo("p1-scenario-subdomain-001");
        assertThat(doc.getMetadata().getVersion()).isEqualTo("1.0.0");
        assertThat(doc.getMetadata().getName()).isEqualTo("场景与子域导入测试");

        // 3 objectTypes with scenario/subDomain
        assertThat(doc.getObjectTypes()).hasSize(3);

        // Entity 1: both scenario and subDomain
        ManifestDocument.ObjectType ot1 = doc.getObjectTypes().get(0);
        assertThat(ot1.getId()).isEqualTo("scenario-entity-001");
        assertThat(ot1.getScenario()).isEqualTo("production-management");
        assertThat(ot1.getSubDomain()).isEqualTo("manufacturing-execution");

        // Entity 2: only scenario
        ManifestDocument.ObjectType ot2 = doc.getObjectTypes().get(1);
        assertThat(ot2.getId()).isEqualTo("scenario-entity-002");
        assertThat(ot2.getScenario()).isEqualTo("quality-control");
        assertThat(ot2.getSubDomain()).isNull();

        // Entity 3: only subDomain
        ManifestDocument.ObjectType ot3 = doc.getObjectTypes().get(2);
        assertThat(ot3.getId()).isEqualTo("scenario-entity-003");
        assertThat(ot3.getScenario()).isNull();
        assertThat(ot3.getSubDomain()).isEqualTo("inspection");

        // Other fields intact
        assertThat(doc.getActions()).hasSize(1);
        assertThat(doc.getRules()).hasSize(1);
        assertThat(doc.getEvents()).hasSize(1);
        assertThat(doc.getStateMachines()).hasSize(1);
    }

    @Test
    @Order(17)
    @DisplayName("CROSS-17: scenario/subDomain fixture 通过 ManifestService 导入")
    void scenarioSubDomainFixtureImport() throws Exception {
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-scenario-subdomain-fixture.json"));

        ManifestDocument converted = converter.convert(raw);
        String manifestDocJson = mapper.writeValueAsString(converted);

        ImportManifestResponse response = manifestService.importManifest(
                ImportManifestRequest.builder()
                        .sourceFormat("JSON")
                        .rawContent(manifestDocJson)
                        .createdBy("e2e-scenario-subdomain-import")
                        .build());

        assertThat(response.isValid()).isTrue();
        assertThat(response.getDraftId()).isNotBlank();
        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(response.getImportedCounts())
                .containsEntry("objectTypes", 3)
                .containsEntry("actions", 1)
                .containsEntry("events", 1)
                .containsEntry("rules", 1)
                .containsEntry("stateMachines", 1);
    }

    @Test
    @Order(18)
    @DisplayName("CROSS-18: scenario/subDomain fixture 全生命周期 — 预览→发布→导出→比对")
    void scenarioSubDomainFixtureFullLifecycle() throws Exception {
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-scenario-subdomain-fixture.json"));

        ManifestDocument converted = converter.convert(raw);
        String manifestDocJson = mapper.writeValueAsString(converted);

        // Import
        ImportManifestResponse importResp = manifestService.importManifest(
                ImportManifestRequest.builder()
                        .sourceFormat("JSON")
                        .rawContent(manifestDocJson)
                        .createdBy("e2e-scenario-lifecycle")
                        .build());
        assertThat(importResp.isValid()).isTrue();
        String draftId = importResp.getDraftId();

        // Preview
        ManifestPreviewResponse preview = manifestService.preview(draftId);
        assertThat(preview.getImportId()).isEqualTo(draftId);
        assertThat(preview.getChanges()).isNotEmpty();

        // Publish
        ManifestPublishResponse publish = manifestService.publish(draftId);
        assertThat(publish.getVersion()).isEqualTo("1.0.0");
        assertThat(publish.getPublishedAt()).isNotNull();

        // Export
        ManifestDocument exported = manifestService.export("1.0.0", "json");
        assertThat(exported).isNotNull();
        assertThat(exported.getMetadata().getId()).isEqualTo("p1-scenario-subdomain-001");
        assertThat(exported.getObjectTypes()).hasSize(3);
        assertThat(exported.getActions()).hasSize(1);
        assertThat(exported.getEvents()).hasSize(1);
        assertThat(exported.getRules()).hasSize(1);
        assertThat(exported.getStateMachines()).hasSize(1);

        // 验证 scenario/subDomain 在导出后仍然被保留
        ManifestDocument.ObjectType exportedOt1 = exported.getObjectTypes().get(0);
        assertThat(exportedOt1.getScenario())
                .as("scenario 应在全生命周期中保留")
                .isEqualTo("production-management");
        assertThat(exportedOt1.getSubDomain())
                .as("subDomain 应在全生命周期中保留")
                .isEqualTo("manufacturing-execution");

        ManifestDocument.ObjectType exportedOt2 = exported.getObjectTypes().get(1);
        assertThat(exportedOt2.getScenario()).isEqualTo("quality-control");
        assertThat(exportedOt2.getSubDomain()).isNull();

        ManifestDocument.ObjectType exportedOt3 = exported.getObjectTypes().get(2);
        assertThat(exportedOt3.getScenario()).isNull();
        assertThat(exportedOt3.getSubDomain()).isEqualTo("inspection");

        // Round-trip serialization equivalence
        String exportedJson = mapper.writeValueAsString(exported);
        ManifestDocument reParsed = mapper.readValue(exportedJson, ManifestDocument.class);
        assertThat(reParsed.getMetadata().getId()).isEqualTo(exported.getMetadata().getId());
        assertThat(reParsed.getObjectTypes().get(0).getId())
                .isEqualTo(exported.getObjectTypes().get(0).getId());
        // 验证反序列化后 scenario/subDomain 仍保留
        assertThat(reParsed.getObjectTypes().get(0).getScenario())
                .isEqualTo("production-management");
        assertThat(reParsed.getObjectTypes().get(0).getSubDomain())
                .isEqualTo("manufacturing-execution");
    }

    // ==================== Fixture-6: Entity Role Mapping Regression Test ====================
    //
    // 背景: P0#3 — 差距分析指出实体角色枚举不一致：
    //       项目1 使用 `child_entity`，项目2 使用 `entity`（see docs/shared/项目1-项目2对接差距分析.md）
    //       已修复: v2 全链路统一使用 `aggregate_root`/`child_entity`
    //       回归验证: ManifestConverter 直接透传 kind 字段，不做枚举映射，
    //                确保 child_entity 不会被错误映射为 entity 或丢失。
    //
    // 验证:
    //   1. ManifestConverter 保留 kind 字段的 aggregate_root / child_entity
    //   2. ManifestService 可导入含两种角色类型的 manifest
    //   3. 全生命周期（预览→发布→导出）角色信息不丢失
    //   4. 显式验证 child_entity 的 kind 值正确 === "child_entity"（而非 "entity"）

    @Test
    @Order(19)
    @DisplayName("CROSS-19: Entity 角色映射 — ManifestConverter 保留 aggregate_root / child_entity")
    void entityRoleMappingPreservedByConverter() throws Exception {
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-entity-role-fixture.json"));

        ManifestDocument doc = converter.convert(raw);

        // 基本转换验证
        assertThat(doc).isNotNull();
        assertThat(doc.getMetadata().getId()).isEqualTo("p1-entity-role-001");
        assertThat(doc.getMetadata().getVersion()).isEqualTo("1.0.0");
        assertThat(doc.getMetadata().getName()).isEqualTo("实体角色映射回归测试");

        // 4 objectTypes: 2 aggregate_root + 2 child_entity
        assertThat(doc.getObjectTypes()).hasSize(4);

        // 验证 aggregate_root 角色
        ManifestDocument.ObjectType ot1 = doc.getObjectTypes().get(0);
        assertThat(ot1.getId()).isEqualTo("order");
        assertThat(ot1.getKind()).isEqualTo("aggregate_root");

        ManifestDocument.ObjectType ot3 = doc.getObjectTypes().get(2);
        assertThat(ot3.getId()).isEqualTo("customer");
        assertThat(ot3.getKind()).isEqualTo("aggregate_root");

        // 验证 child_entity 角色 — 回归项：确保是 "child_entity" 而非 "entity"
        ManifestDocument.ObjectType ot2 = doc.getObjectTypes().get(1);
        assertThat(ot2.getId()).isEqualTo("order-line");
        assertThat(ot2.getKind())
                .as("P0#3 回归: child_entity 角色应保持为 'child_entity'，不会被映射为 'entity' 或丢失")
                .isEqualTo("child_entity");

        ManifestDocument.ObjectType ot4 = doc.getObjectTypes().get(3);
        assertThat(ot4.getId()).isEqualTo("contact");
        assertThat(ot4.getKind())
                .as("P0#3 回归: child_entity 角色应保持为 'child_entity'")
                .isEqualTo("child_entity");

        // 注: ManifestConverter 当前未读取 aggregateRootId 字段，
        //     这是 scope 外已知行为（非映射修复的一部分）
        //     角色映射回归验证聚焦于 kind 字段的 child_entity ↔ entity 一致性

        // 其他字段正常
        assertThat(doc.getActions()).hasSize(2);
        assertThat(doc.getEvents()).hasSize(2);
        assertThat(doc.getRules()).hasSize(1);
    }

    @Test
    @Order(20)
    @DisplayName("CROSS-20: Entity 角色映射 — ManifestService 导入含 aggregate_root + child_entity 的 manifest")
    void entityRoleMappingImport() throws Exception {
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-entity-role-fixture.json"));

        ManifestDocument converted = converter.convert(raw);
        String manifestDocJson = mapper.writeValueAsString(converted);

        ImportManifestResponse response = manifestService.importManifest(
                ImportManifestRequest.builder()
                        .sourceFormat("JSON")
                        .rawContent(manifestDocJson)
                        .createdBy("e2e-entity-role-import")
                        .build());

        assertThat(response.isValid()).isTrue();
        assertThat(response.getDraftId()).isNotBlank();
        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(response.getImportedCounts())
                .containsEntry("objectTypes", 4)
                .containsEntry("actions", 2)
                .containsEntry("events", 2)
                .containsEntry("rules", 1)
                .containsEntry("stateMachines", 0);
    }

    @Test
    @Order(21)
    @DisplayName("CROSS-21: Entity 角色映射 — 全生命周期（预览→发布→导出）角色信息不丢失")
    void entityRoleMappingFullLifecycle() throws Exception {
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-entity-role-fixture.json"));

        ManifestDocument converted = converter.convert(raw);
        String manifestDocJson = mapper.writeValueAsString(converted);

        // Import
        ImportManifestResponse importResp = manifestService.importManifest(
                ImportManifestRequest.builder()
                        .sourceFormat("JSON")
                        .rawContent(manifestDocJson)
                        .createdBy("e2e-entity-role-lifecycle")
                        .build());
        assertThat(importResp.isValid()).isTrue();
        String draftId = importResp.getDraftId();

        // Preview
        ManifestPreviewResponse preview = manifestService.preview(draftId);
        assertThat(preview.getImportId()).isEqualTo(draftId);
        assertThat(preview.getChanges()).isNotEmpty();

        // Publish
        ManifestPublishResponse publish = manifestService.publish(draftId);
        assertThat(publish.getVersion()).isEqualTo("1.0.0");
        assertThat(publish.getPublishedAt()).isNotNull();

        // Export
        ManifestDocument exported = manifestService.export("1.0.0", "json");
        assertThat(exported).isNotNull();
        assertThat(exported.getMetadata().getId()).isEqualTo("p1-entity-role-001");
        assertThat(exported.getObjectTypes()).hasSize(4);
        assertThat(exported.getActions()).hasSize(2);
        assertThat(exported.getEvents()).hasSize(2);

        // 验证导出后所有 4 个 objectTypes 的角色信息保留
        // aggregate_root 验证
        ManifestDocument.ObjectType exportedOt1 = exported.getObjectTypes().get(0);
        assertThat(exportedOt1.getId()).isEqualTo("order");
        assertThat(exportedOt1.getKind())
                .as("aggregate_root 角色在全生命周期中应保留")
                .isEqualTo("aggregate_root");

        ManifestDocument.ObjectType exportedOt2 = exported.getObjectTypes().get(1);
        assertThat(exportedOt2.getId()).isEqualTo("order-line");
        assertThat(exportedOt2.getKind())
                .as("P0#3 回归: child_entity 角色在全生命周期中应保留为 'child_entity'")
                .isEqualTo("child_entity");

        ManifestDocument.ObjectType exportedOt3 = exported.getObjectTypes().get(2);
        assertThat(exportedOt3.getId()).isEqualTo("customer");
        assertThat(exportedOt3.getKind()).isEqualTo("aggregate_root");

        ManifestDocument.ObjectType exportedOt4 = exported.getObjectTypes().get(3);
        assertThat(exportedOt4.getId()).isEqualTo("contact");
        assertThat(exportedOt4.getKind())
                .as("P0#3 回归: child_entity 角色在全生命周期中应保留为 'child_entity'")
                .isEqualTo("child_entity");

        // Round-trip serialization equivalence
        String exportedJson = mapper.writeValueAsString(exported);
        ManifestDocument reParsed = mapper.readValue(exportedJson, ManifestDocument.class);
        assertThat(reParsed.getMetadata().getId()).isEqualTo(exported.getMetadata().getId());
        assertThat(reParsed.getObjectTypes().get(0).getId())
                .isEqualTo(exported.getObjectTypes().get(0).getId());
        // 验证反序列化后 child_entity 角色仍保留
        assertThat(reParsed.getObjectTypes().get(1).getKind())
                .as("反序列化后 child_entity 角色应保留")
                .isEqualTo("child_entity");
    }

    // ==================== Fixture-7: EntityLifecycle + AgentSemanticLayer Tests ====================
    //
    // 背景: 项目1 现已支持模块化导出 EntityLifecycle JSON 和 AgentSemanticLayer JSON。
    //       项目2 需要能够处理这些新格式。对应的 v2 OntologyExchangeDocument 已定义
    //       Spec.lifecycle (LifecycleSpec) 和 OntologyProject.agentSemanticLayer 字段。
    //
    // 验证:
    //   1. CROSS-22: EntityLifecycle 数据可从 v2 交换格式正确解析和验证
    //   2. CROSS-23: AgentSemanticLayer 数据可从 v2 交换格式正确解析和验证
    //   3. CROSS-24: v2 文档全生命周期（序列化→反序列化）数据不丢失

    @Test
    @Order(22)
    @DisplayName("CROSS-22: EntityLifecycle 数据解析验证")
    void parseEntityLifecycleFromV2Document() throws Exception {
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-lifecycle-semantic-fixture.json"));

        OntologyExchangeDocument doc = mapper.readValue(raw, OntologyExchangeDocument.class);

        assertThat(doc).isNotNull();
        assertThat(doc.getApiVersion()).isEqualTo("ontology.platform/v2");
        assertThat(doc.getKind()).isEqualTo("OntologyExchange");
        assertThat(doc.getMetadata().getId()).isEqualTo("p1-lifecycle-semantic-001");

        // Verify lifecycle exists
        assertThat(doc.getSpec()).isNotNull();
        assertThat(doc.getSpec().getLifecycle()).isNotNull();
        assertThat(doc.getSpec().getLifecycle().getByEntityId()).hasSize(2);

        // Verify production-order lifecycle
        OntologyExchangeDocument.EntityLifecycleEntry prodLifecycle =
                doc.getSpec().getLifecycle().getByEntityId().get("production-order");
        assertThat(prodLifecycle).isNotNull();
        assertThat(prodLifecycle.getEntityId()).isEqualTo("production-order");
        assertThat(prodLifecycle.getEntityNameEn()).isEqualTo("ProductionOrder");
        assertThat(prodLifecycle.getStatusField()).isEqualTo("status");

        // Verify state machine in lifecycle
        assertThat(prodLifecycle.getStateMachine()).isNotNull();
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> sm = (java.util.Map<String, Object>) prodLifecycle.getStateMachine();
        assertThat(sm.get("id")).isEqualTo("sm-order-lifecycle");

        @SuppressWarnings("unchecked")
        java.util.List<Object> states = (java.util.List<Object>) sm.get("states");
        assertThat(states).hasSize(5); // DRAFT, RELEASED, IN_PROGRESS, COMPLETED, CANCELLED

        @SuppressWarnings("unchecked")
        java.util.List<Object> transitions = (java.util.List<Object>) sm.get("transitions");
        assertThat(transitions).hasSize(4);

        // Verify actionsByState
        assertThat(prodLifecycle.getActionsByState()).isNotNull();
        assertThat(prodLifecycle.getActionsByState()).containsKeys("DRAFT", "RELEASED", "IN_PROGRESS", "COMPLETED", "CANCELLED");

        // Verify rulesByState
        assertThat(prodLifecycle.getRulesByState()).containsKeys("RELEASED", "IN_PROGRESS");

        // Verify eventsByState
        assertThat(prodLifecycle.getEventsByState()).containsKeys("RELEASED", "COMPLETED", "CANCELLED");

        // Verify rolesByState
        assertThat(prodLifecycle.getRolesByState()).containsKeys("DRAFT", "RELEASED", "IN_PROGRESS", "COMPLETED", "CANCELLED");

        // Verify audit trail
        assertThat(prodLifecycle.getAuditTrail()).hasSize(2);

        // Verify stats
        assertThat(prodLifecycle.getStats()).isNotNull();
        assertThat(prodLifecycle.getStats()).containsKey("avgTimeInDraft");

        // Verify quality-inspection lifecycle
        OntologyExchangeDocument.EntityLifecycleEntry inspLifecycle =
                doc.getSpec().getLifecycle().getByEntityId().get("quality-inspection");
        assertThat(inspLifecycle).isNotNull();
        assertThat(inspLifecycle.getEntityId()).isEqualTo("quality-inspection");
        assertThat(inspLifecycle.getEntityNameEn()).isEqualTo("QualityInspection");
        assertThat(inspLifecycle.getStatusField()).isEqualTo("result");

        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> inspSm = (java.util.Map<String, Object>) inspLifecycle.getStateMachine();
        @SuppressWarnings("unchecked")
        java.util.List<Object> inspStates = (java.util.List<Object>) inspSm.get("states");
        assertThat(inspStates).hasSize(3); // PENDING, PASSED, FAILED

        @SuppressWarnings("unchecked")
        java.util.List<Object> inspTransitions = (java.util.List<Object>) inspSm.get("transitions");
        assertThat(inspTransitions).hasSize(2);
    }

    @Test
    @Order(23)
    @DisplayName("CROSS-23: AgentSemanticLayer 数据解析验证")
    void parseAgentSemanticLayerFromV2Document() throws Exception {
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-lifecycle-semantic-fixture.json"));

        OntologyExchangeDocument doc = mapper.readValue(raw, OntologyExchangeDocument.class);

        assertThat(doc).isNotNull();
        assertThat(doc.getSpec().getProject()).isNotNull();
        assertThat(doc.getSpec().getProject().getAgentSemanticLayer()).isNotNull();

        OntologyExchangeDocument.AgentSemanticLayer asl = doc.getSpec().getProject().getAgentSemanticLayer();

        // Verify intents
        assertThat(asl.getIntents()).hasSize(2);
        OntologyExchangeDocument.Intent releaseIntent = asl.getIntents().get(0);
        assertThat(releaseIntent.getId()).isEqualTo("int-release-order");
        assertThat(releaseIntent.getName()).isEqualTo("下达生产订单");
        assertThat(releaseIntent.getCategory()).isEqualTo("production");
        assertThat(releaseIntent.getTriggerPhrases()).contains("下达生产订单", "发布生产指令", "开始生产");
        assertThat(releaseIntent.getActionId()).isEqualTo("act-release-order");
        assertThat(releaseIntent.getPriority()).isEqualTo(10);
        assertThat(releaseIntent.getRequiresConfirmation()).isTrue();

        // Verify slot filling
        assertThat(releaseIntent.getSlotFilling()).isNotNull();
        assertThat(releaseIntent.getSlotFilling().getSlots()).hasSize(2);
        assertThat(releaseIntent.getSlotFilling().getRequiredSlots()).containsExactly("slot-order-id");
        assertThat(releaseIntent.getSlotFilling().getFillOrder()).containsExactly("slot-order-id", "slot-qty");

        OntologyExchangeDocument.IntentSlot orderIdSlot = releaseIntent.getSlotFilling().getSlots().get(0);
        assertThat(orderIdSlot.getId()).isEqualTo("slot-order-id");
        assertThat(orderIdSlot.getSlotType()).isEqualTo("string");
        assertThat(orderIdSlot.getRequired()).isTrue();

        // Verify second intent (no slot filling)
        OntologyExchangeDocument.Intent checkIntent = asl.getIntents().get(1);
        assertThat(checkIntent.getId()).isEqualTo("int-check-status");
        assertThat(checkIntent.getCategory()).isEqualTo("inquiry");
        assertThat(checkIntent.getRequiresConfirmation()).isFalse();

        // Verify business terms
        assertThat(asl.getBusinessTerms()).hasSize(3);
        OntologyExchangeDocument.BusinessTerm prodTerm = asl.getBusinessTerms().get(0);
        assertThat(prodTerm.getId()).isEqualTo("term-production-order");
        assertThat(prodTerm.getName()).isEqualTo("生产订单");
        assertThat(prodTerm.getNameEn()).isEqualTo("ProductionOrder");
        assertThat(prodTerm.getDefinition()).isNotBlank();
        assertThat(prodTerm.getSynonyms()).contains("工单", "制造订单", "生产指令");

        OntologyExchangeDocument.BusinessTerm statusTerm = asl.getBusinessTerms().get(2);
        assertThat(statusTerm.getId()).isEqualTo("term-order-status");
        assertThat(statusTerm.getSynonyms()).contains("状态", "阶段");

        // Verify semantic relations
        assertThat(asl.getSemanticRelations()).hasSize(2);
        OntologyExchangeDocument.SemanticRelation rel1 = asl.getSemanticRelations().get(0);
        assertThat(rel1.getId()).isEqualTo("rel-prod-insp");
        assertThat(rel1.getSourceTermId()).isEqualTo("term-production-order");
        assertThat(rel1.getTargetTermId()).isEqualTo("term-quality-inspection");
        assertThat(rel1.getRelationType()).isEqualTo("has_inspection");

        OntologyExchangeDocument.SemanticRelation rel2 = asl.getSemanticRelations().get(1);
        assertThat(rel2.getRelationType()).isEqualTo("has_status");

        // Verify agent policies
        assertThat(asl.getAgentPolicies()).hasSize(2);
        OntologyExchangeDocument.SemanticAgentPolicy prodManagerPolicy = asl.getAgentPolicies().get(0);
        assertThat(prodManagerPolicy.getId()).isEqualTo("ap-prod-manager");
        assertThat(prodManagerPolicy.getRoleId()).isEqualTo("role-prod-manager");
        assertThat(prodManagerPolicy.getAllowedMcpTools()).contains("release_order", "cancel_order", "query_order");
        assertThat(prodManagerPolicy.getAllowedAggregateRootIds()).containsExactly("production-order");
        assertThat(prodManagerPolicy.getDefaultDeny()).isFalse();

        // Verify error recoveries
        assertThat(asl.getErrorRecoveries()).hasSize(2);
        OntologyExchangeDocument.SemanticErrorRecovery er1 = asl.getErrorRecoveries().get(0);
        assertThat(er1.getId()).isEqualTo("er-release-fallback");
        assertThat(er1.getActionId()).isEqualTo("act-release-order");
        assertThat(er1.getErrorPattern()).isEqualTo("ORDER_NOT_FOUND");
        assertThat(er1.getRecoveryStrategy()).isEqualTo("retry_with_new_id");
        assertThat(er1.getMaxRetries()).isEqualTo(3);

        // Verify field mappings
        assertThat(asl.getFieldMappings()).hasSize(2);
        OntologyExchangeDocument.SemanticFieldMapping fm1 = asl.getFieldMappings().get(0);
        assertThat(fm1.getId()).isEqualTo("fm-order-id");
        assertThat(fm1.getEntityId()).isEqualTo("production-order");
        assertThat(fm1.getFieldNameEn()).isEqualTo("orderId");
        assertThat(fm1.getBusinessTermId()).isEqualTo("term-production-order");
        assertThat(fm1.getMappingType()).isEqualTo("direct");
    }

    @Test
    @Order(24)
    @DisplayName("CROSS-24: v2 交换文档全生命周期 — 序列化→反序列化→比对（含Lifecycle+SemanticLayer）")
    void v2DocumentFullLifecycleRoundTrip() throws Exception {
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-lifecycle-semantic-fixture.json"));

        // Parse → OntologyExchangeDocument
        OntologyExchangeDocument original = mapper.readValue(raw, OntologyExchangeDocument.class);

        // Serialize back to JSON
        String serializedJson = mapper.writeValueAsString(original);

        // Re-parse
        OntologyExchangeDocument reParsed = mapper.readValue(serializedJson, OntologyExchangeDocument.class);

        // === Metadata verification ===
        assertThat(reParsed.getApiVersion()).isEqualTo(original.getApiVersion());
        assertThat(reParsed.getKind()).isEqualTo(original.getKind());
        assertThat(reParsed.getMetadata().getId()).isEqualTo(original.getMetadata().getId());
        assertThat(reParsed.getMetadata().getVersion()).isEqualTo(original.getMetadata().getVersion());
        assertThat(reParsed.getMetadata().getName()).isEqualTo(original.getMetadata().getName());

        // === Lifecycle round-trip ===
        assertThat(reParsed.getSpec().getLifecycle()).isNotNull();
        assertThat(reParsed.getSpec().getLifecycle().getByEntityId()).hasSize(2);
        assertThat(reParsed.getSpec().getLifecycle().getByEntityId())
                .containsOnlyKeys("production-order", "quality-inspection");

        // Verify production-order lifecycle preserved
        OntologyExchangeDocument.EntityLifecycleEntry reParsedProd =
                reParsed.getSpec().getLifecycle().getByEntityId().get("production-order");
        assertThat(reParsedProd.getEntityId()).isEqualTo("production-order");
        assertThat(reParsedProd.getEntityNameEn()).isEqualTo("ProductionOrder");
        assertThat(reParsedProd.getStatusField()).isEqualTo("status");
        assertThat(reParsedProd.getStateMachine()).isNotNull();
        assertThat(reParsedProd.getActionsByState()).containsKeys("DRAFT", "RELEASED");
        assertThat(reParsedProd.getRulesByState()).containsKeys("RELEASED", "IN_PROGRESS");
        assertThat(reParsedProd.getEventsByState()).containsKeys("RELEASED", "COMPLETED", "CANCELLED");
        assertThat(reParsedProd.getRolesByState()).containsKeys("DRAFT", "RELEASED", "IN_PROGRESS");
        assertThat(reParsedProd.getAuditTrail()).hasSize(2);
        assertThat(reParsedProd.getStats()).containsKey("avgTimeInDraft");

        // Verify quality-inspection lifecycle preserved
        OntologyExchangeDocument.EntityLifecycleEntry reParsedInsp =
                reParsed.getSpec().getLifecycle().getByEntityId().get("quality-inspection");
        assertThat(reParsedInsp.getEntityId()).isEqualTo("quality-inspection");
        assertThat(reParsedInsp.getStatusField()).isEqualTo("result");

        // === AgentSemanticLayer round-trip ===
        assertThat(reParsed.getSpec().getProject()).isNotNull();
        assertThat(reParsed.getSpec().getProject().getAgentSemanticLayer()).isNotNull();

        OntologyExchangeDocument.AgentSemanticLayer originalAsl = original.getSpec().getProject().getAgentSemanticLayer();
        OntologyExchangeDocument.AgentSemanticLayer reParsedAsl = reParsed.getSpec().getProject().getAgentSemanticLayer();

        // Intents
        assertThat(reParsedAsl.getIntents()).hasSize(originalAsl.getIntents().size());
        assertThat(reParsedAsl.getIntents().get(0).getId()).isEqualTo("int-release-order");
        assertThat(reParsedAsl.getIntents().get(0).getTriggerPhrases())
                .containsExactlyElementsOf(originalAsl.getIntents().get(0).getTriggerPhrases());
        assertThat(reParsedAsl.getIntents().get(0).getPriority()).isEqualTo(originalAsl.getIntents().get(0).getPriority());

        // Slot filling preserved
        assertThat(reParsedAsl.getIntents().get(0).getSlotFilling()).isNotNull();
        assertThat(reParsedAsl.getIntents().get(0).getSlotFilling().getSlots()).hasSize(2);
        assertThat(reParsedAsl.getIntents().get(0).getSlotFilling().getRequiredSlots())
                .containsExactly("slot-order-id");

        // Business terms
        assertThat(reParsedAsl.getBusinessTerms()).hasSize(originalAsl.getBusinessTerms().size());
        assertThat(reParsedAsl.getBusinessTerms().get(0).getId()).isEqualTo("term-production-order");
        assertThat(reParsedAsl.getBusinessTerms().get(0).getSynonyms())
                .contains("工单", "制造订单", "生产指令");

        // Semantic relations
        assertThat(reParsedAsl.getSemanticRelations()).hasSize(originalAsl.getSemanticRelations().size());
        assertThat(reParsedAsl.getSemanticRelations().get(0).getRelationType())
                .isEqualTo(originalAsl.getSemanticRelations().get(0).getRelationType());

        // Agent policies
        assertThat(reParsedAsl.getAgentPolicies()).hasSize(originalAsl.getAgentPolicies().size());
        assertThat(reParsedAsl.getAgentPolicies().get(0).getAllowedMcpTools())
                .containsExactlyElementsOf(originalAsl.getAgentPolicies().get(0).getAllowedMcpTools());

        // Error recoveries
        assertThat(reParsedAsl.getErrorRecoveries()).hasSize(originalAsl.getErrorRecoveries().size());
        assertThat(reParsedAsl.getErrorRecoveries().get(0).getMaxRetries())
                .isEqualTo(originalAsl.getErrorRecoveries().get(0).getMaxRetries());

        // Field mappings
        assertThat(reParsedAsl.getFieldMappings()).hasSize(originalAsl.getFieldMappings().size());
        assertThat(reParsedAsl.getFieldMappings().get(0).getBusinessTermId())
                .isEqualTo(originalAsl.getFieldMappings().get(0).getBusinessTermId());
    }

    // ==================== CROSS-25: Round-Trip Consistency Test ====================
    //
    // 背景: 项目1 fixture → 项目2 ManifestService 导入 → 导出 → 再导入
    //       验证转换管线无损: 两次导入的结果结构等效。
    //       整个管线: Project1 JSON → ManifestConverter → ManifestDocument
    //       → JSON序列化 → importManifest → publish → export → JSON序列化
    //       → importManifest (第二次) → publish → export → 与第一次结果比对。
    //
    //       注意: ManifestServiceImpl.export() 返回 versionStore 中的对象引用。
    //       第二次导入创建全新的 ManifestDocument 对象（JSON 反序列化），
    //       因此两次对象的引用不同，比对可以真实验证序列化/反序列化无损性。

    @Test
    @Order(25)
    @DisplayName("CROSS-25: 项目1→项目2 反向导出 round-trip 一致性 — 导入→导出→再导入结果结构等效")
    void roundTripConsistencyForProject1Fixture() throws Exception {
        // ===== Phase 1: Import Project1 fixture into ManifestService =====
        String raw = Files.readString(
                Path.of("src/test/resources/fixtures/project1-manifest-export.json"));

        ManifestDocument converted = converter.convert(raw);
        String manifestDocJson = mapper.writeValueAsString(converted);

        ImportManifestResponse firstImport = manifestService.importManifest(
                ImportManifestRequest.builder()
                        .sourceFormat("JSON")
                        .rawContent(manifestDocJson)
                        .createdBy("e2e-roundtrip-p1")
                        .build());
        assertThat(firstImport.isValid()).isTrue();
        String firstDraftId = firstImport.getDraftId();

        // Preview
        ManifestPreviewResponse preview = manifestService.preview(firstDraftId);
        assertThat(preview.getImportId()).isEqualTo(firstDraftId);

        // Publish
        ManifestPublishResponse firstPublish = manifestService.publish(firstDraftId);
        String version = firstPublish.getVersion();
        assertThat(firstPublish.getPublishedAt()).isNotNull();

        // Export
        ManifestDocument exported = manifestService.export(version, "json");
        assertThat(exported).isNotNull();
        assertThat(exported.getMetadata().getId()).isEqualTo("p1-mfg-001");

        // ===== Phase 2: Re-import the exported JSON =====
        String exportedJson = mapper.writeValueAsString(exported);

        ImportManifestResponse secondImport = manifestService.importManifest(
                ImportManifestRequest.builder()
                        .sourceFormat("JSON")
                        .rawContent(exportedJson)
                        .createdBy("e2e-roundtrip-reimport")
                        .build());
        assertThat(secondImport.isValid()).isTrue();
        String secondDraftId = secondImport.getDraftId();

        // ===== Phase 3: Compare ImportManifestResponse — same externalId, same counts =====
        assertThat(secondImport.getExternalId())
                .as("两次导入的 externalId (metadata.id) 应相同 — 表明元数据无损传递")
                .isEqualTo(firstImport.getExternalId());
        assertThat(secondImport.getImportedCounts())
                .as("两次导入的计数应完全一致 — 表明所有维度的实体数量无损")
                .isEqualTo(firstImport.getImportedCounts());

        // ===== Phase 4: Publish second import and compare ManifestDocument objects =====
        ManifestPreviewResponse preview2 = manifestService.preview(secondDraftId);
        assertThat(preview2.getImportId()).isEqualTo(secondDraftId);

        ManifestPublishResponse secondPublish = manifestService.publish(secondDraftId);
        assertThat(secondPublish.getPublishedAt()).isNotNull();

        ManifestDocument reImported = manifestService.export(secondPublish.getVersion(), "json");
        assertThat(reImported).isNotNull();

        // Deep-compare via JsonNode tree (全字段结构等效)
        String firstSerialized = mapper.writeValueAsString(exported);
        String secondSerialized = mapper.writeValueAsString(reImported);
        assertThat(mapper.readTree(firstSerialized))
                .as("原始导出与再导入后的 ManifestDocument 应 JSON tree 全等 — 证明管线无损")
                .isEqualTo(mapper.readTree(secondSerialized));

        // === 分维度验证 ===

        // Metadata
        assertThat(reImported.getMetadata().getId()).isEqualTo(exported.getMetadata().getId());
        assertThat(reImported.getMetadata().getVersion()).isEqualTo(exported.getMetadata().getVersion());
        assertThat(reImported.getMetadata().getName()).isEqualTo(exported.getMetadata().getName());

        // Semantic: objectTypes (3 items: 2 aggregate_root + 1 child_entity)
        assertThat(reImported.getObjectTypes()).hasSize(exported.getObjectTypes().size());
        assertThat(reImported.getObjectTypes().get(0).getId())
                .isEqualTo(exported.getObjectTypes().get(0).getId());
        assertThat(reImported.getObjectTypes().get(0).getKind())
                .isEqualTo(exported.getObjectTypes().get(0).getKind());

        // Behavior: actions, rules, stateMachines
        assertThat(reImported.getActions()).hasSize(exported.getActions().size());
        assertThat(reImported.getActions().get(0).getId())
                .isEqualTo(exported.getActions().get(0).getId());
        assertThat(reImported.getRules()).hasSize(exported.getRules().size());
        assertThat(reImported.getEvents()).hasSize(exported.getEvents().size());
        assertThat(reImported.getStateMachines()).hasSize(exported.getStateMachines().size());

        // State machine details preserved
        for (int i = 0; i < exported.getStateMachines().size(); i++) {
            assertThat(reImported.getStateMachines().get(i).getStates())
                    .hasSize(exported.getStateMachines().get(i).getStates().size());
            assertThat(reImported.getStateMachines().get(i).getTransitions())
                    .hasSize(exported.getStateMachines().get(i).getTransitions().size());
        }

        // Governance preserved
        assertThat(reImported.getSpec().getGovernance()).isNotNull();
        assertThat(reImported.getSpec().getGovernance().getRoles())
                .hasSize(exported.getSpec().getGovernance().getRoles().size());

        // DataSources preserved
        assertThat(reImported.getSpec().getDataSources())
                .hasSize(exported.getSpec().getDataSources().size());
    }
}

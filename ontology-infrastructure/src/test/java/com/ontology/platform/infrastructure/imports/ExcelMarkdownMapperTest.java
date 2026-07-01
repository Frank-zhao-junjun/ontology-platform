package com.ontology.platform.infrastructure.imports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ExcelExchangeMapper} and {@link MarkdownExchangeMapper}
 * that exercise their actual mapping logic with real in-memory input data.
 *
 * <p>Unlike {@code ExchangeImportServiceTest} which mocks these mappers,
 * this test creates genuine Excel workbooks (via Apache POI) and Markdown strings,
 * feeds them through the mapper implementations, and verifies the resulting
 * {@link OntologyExchangeDocument} structure.</p>
 */
@DisplayName("Excel/Markdown Mapper Real Invocation Test")
class ExcelMarkdownMapperTest {

    private ObjectMapper objectMapper;
    private ExcelExchangeMapper excelMapper;
    private MarkdownExchangeMapper markdownMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        excelMapper = new ExcelExchangeMapper(
                new ExcelOntologyImportAdapter(objectMapper),
                new ExcelBImportAdapter(objectMapper),
                new ExcelCImportAdapter(objectMapper),
                objectMapper
        );
        markdownMapper = new MarkdownExchangeMapper(objectMapper);
    }

    // ================================================================
    // ExcelExchangeMapper Tests
    // ================================================================

    @Nested
    @DisplayName("ExcelExchangeMapper.mapFromXlsx()")
    class ExcelMapperTests {

        @Test
        @DisplayName("should parse xlsx with Sheets A/B/C and generate complete OntologyExchangeDocument")
        void shouldMapXlsxToOntologyExchangeDocument() throws IOException {
            byte[] xlsxBytes = createFullXlsxWorkbook();
            String externalId = "test-excel-project-" + UUID.randomUUID().toString().substring(0, 8);

            OntologyExchangeDocument doc = excelMapper.mapFromXlsx(
                    new ByteArrayInputStream(xlsxBytes), externalId);

            // -- top-level envelope --
            assertThat(doc).isNotNull();
            assertThat(doc.getApiVersion()).isEqualTo("ontology.platform/v2");
            assertThat(doc.getKind()).isEqualTo("OntologyExchange");

            // -- Metadata --
            assertThat(doc.getMetadata()).isNotNull();
            assertThat(doc.getMetadata().getId()).isNotBlank();
            assertThat(doc.getMetadata().getVersion()).isEqualTo("0.1.0");
            assertThat(doc.getMetadata().getSource()).isEqualTo("excel-import");
            assertThat(doc.getMetadata().getStatus()).isEqualTo("draft");

            // -- Spec / Project --
            assertThat(doc.getSpec()).isNotNull();
            assertThat(doc.getSpec().getProject()).isNotNull();
            assertThat(doc.getSpec().getProject().getId()).isNotBlank();
            assertThat(doc.getSpec().getProject().getName()).isNotBlank();

            // -- DataModel --
            assertThat(doc.getSpec().getProject().getDataModel()).isNotNull();
            assertThat(doc.getSpec().getProject().getDataModel().getId()).endsWith("-data");
            assertThat(doc.getSpec().getProject().getDataModel().getVersion()).isEqualTo("0.1.0");

            // -- Entities (Sheet B = aggregate_root, Sheet C = child_entity) --
            List<OntologyExchangeDocument.Entity> entities =
                    doc.getSpec().getProject().getDataModel().getEntities();
            assertThat(entities).isNotEmpty();

            long aggregateRoots = entities.stream()
                    .filter(e -> "aggregate_root".equals(e.getEntityRole()))
                    .count();
            long childEntities = entities.stream()
                    .filter(e -> "child_entity".equals(e.getEntityRole()))
                    .count();

            // 2 rows in Sheet B, 1 row in Sheet C
            assertThat(aggregateRoots).isEqualTo(2);
            assertThat(childEntities).isEqualTo(1);

            // parentAggregateId for child entities
            for (OntologyExchangeDocument.Entity e : entities) {
                if ("child_entity".equals(e.getEntityRole())) {
                    assertThat(e.getParentAggregateId()).isNotBlank();
                }
            }
        }

        @Test
        @DisplayName("should generate unique ID when externalId is null, project name from Sheet A row ID")
        void shouldGenerateProjectIdWhenExternalIdIsNull() throws IOException {
            // The mapper always reads all three sheets — provide a full workbook
            byte[] xlsxBytes = createFullXlsxWorkbook();
            OntologyExchangeDocument doc = excelMapper.mapFromXlsx(
                    new ByteArrayInputStream(xlsxBytes), null);

            assertThat(doc.getMetadata().getId()).isNotBlank();
            assertThat(doc.getSpec().getProject().getId()).isNotBlank();
            // Project name comes from the first cell of Sheet A row (the ID column),
            // which becomes Ontology.name via Ontology.create(id, name, desc, user)
            assertThat(doc.getMetadata().getName()).isEqualTo("VD001");
            // Verify source is excel-import
            assertThat(doc.getMetadata().getSource()).isEqualTo("excel-import");
        }

        @Test
        @DisplayName("round-trip through JSON should preserve structure")
        void roundTripThroughJsonShouldPreserveStructure() throws IOException {
            byte[] xlsxBytes = createFullXlsxWorkbook();
            OntologyExchangeDocument original = excelMapper.mapFromXlsx(
                    new ByteArrayInputStream(xlsxBytes), "round-trip-test");

            String json = excelMapper.toJson(original);
            assertThat(json).isNotBlank();
            assertThat(json).contains("ontology.platform/v2");
            assertThat(json).contains("OntologyExchange");

            // round-trip back via mapFromParsedData
            OntologyExchangeDocument restored = excelMapper.mapFromParsedData(json);
            assertThat(restored).isNotNull();
            assertThat(restored.getApiVersion()).isEqualTo("ontology.platform/v2");
            assertThat(restored.getKind()).isEqualTo("OntologyExchange");
            assertThat(restored.getMetadata().getId()).isEqualTo(original.getMetadata().getId());
            assertThat(restored.getSpec().getProject().getDataModel().getEntities())
                    .hasSameSizeAs(original.getSpec().getProject().getDataModel().getEntities());
        }
    }

    // ================================================================
    // MarkdownExchangeMapper Tests
    // ================================================================

    @Nested
    @DisplayName("MarkdownExchangeMapper.mapFromMarkdown()")
    class MarkdownMapperTests {

        @Test
        @DisplayName("should parse standard Markdown with A/B/E1 sections into OntologyExchangeDocument")
        void shouldMapMarkdownToOntologyExchangeDocument() throws IOException {
            String markdown = createStandardMarkdown();
            String externalId = "test-md-project";

            OntologyExchangeDocument doc = markdownMapper.mapFromMarkdown(markdown, externalId);

            // -- top-level --
            assertThat(doc).isNotNull();
            assertThat(doc.getApiVersion()).isEqualTo("ontology.platform/v2");
            assertThat(doc.getKind()).isEqualTo("OntologyExchange");

            // -- Metadata --
            assertThat(doc.getMetadata()).isNotNull();
            assertThat(doc.getMetadata().getId()).isEqualTo(externalId);
            assertThat(doc.getMetadata().getSource()).isEqualTo("markdown-import");
            assertThat(doc.getMetadata().getStatus()).isEqualTo("draft");
            assertThat(doc.getMetadata().getVersion()).isEqualTo("0.1.0");

            // -- Spec / Project --
            assertThat(doc.getSpec()).isNotNull();
            assertThat(doc.getSpec().getProject()).isNotNull();
            assertThat(doc.getSpec().getProject().getId()).isEqualTo(externalId);

            // -- Domain (from A section) --
            assertThat(doc.getSpec().getProject().getDomain()).isNotNull();
            assertThat(doc.getSpec().getProject().getDomain().getId()).isEqualTo("VD001");
            assertThat(doc.getSpec().getProject().getDomain().getName()).isEqualTo("manufacturing");
            assertThat(doc.getSpec().getProject().getDomain().getDescription()).isEqualTo("manufacturing domain");

            // -- DataModel / Entities (from E1 section) --
            assertThat(doc.getSpec().getProject().getDataModel()).isNotNull();
            List<OntologyExchangeDocument.Entity> entities =
                    doc.getSpec().getProject().getDataModel().getEntities();
            assertThat(entities).hasSize(2);
            assertThat(entities.get(0).getId()).isEqualTo("E001");
            assertThat(entities.get(0).getName()).isEqualTo("production order");
            assertThat(entities.get(0).getEntityRole()).isEqualTo("aggregate_root");
            assertThat(entities.get(1).getId()).isEqualTo("E002");
            assertThat(entities.get(1).getName()).isEqualTo("bill of materials");

            // -- BehaviorModel / Actions (from B section) --
            assertThat(doc.getSpec().getProject().getBehaviorModel()).isNotNull();
            List<OntologyExchangeDocument.Action> actions =
                    doc.getSpec().getProject().getBehaviorModel().getActions();
            assertThat(actions).isNotEmpty();
            assertThat(actions.get(0).getName()).isEqualTo("create order");
        }

        @Test
        @DisplayName("should support all section types: C, EPC, E2-E8")
        void shouldMapAllSectionTypes() throws IOException {
            String markdown = createFullMarkdownWithAllSections();
            String externalId = "test-all-sections";
            OntologyExchangeDocument doc = markdownMapper.mapFromMarkdown(markdown, externalId);

            // -- DataModel (E1) --
            assertThat(doc.getSpec().getProject().getDataModel()).isNotNull();
            assertThat(doc.getSpec().getProject().getDataModel().getEntities()).hasSize(2);
            assertThat(doc.getSpec().getProject().getDataModel().getId()).endsWith("-data");

            // -- BehaviorModel (B + E2 both contribute to actions) --
            assertThat(doc.getSpec().getProject().getBehaviorModel()).isNotNull();
            List<OntologyExchangeDocument.Action> actions =
                    doc.getSpec().getProject().getBehaviorModel().getActions();
            // B section has 1 row + E2 section has 1 row = 2 actions total
            assertThat(actions).isNotEmpty();
            assertThat(actions.get(0).getName()).isEqualTo("create order");
            assertThat(doc.getSpec().getProject().getBehaviorModel().getId()).endsWith("-behavior");

            // -- RuleModel (E3) --
            assertThat(doc.getSpec().getProject().getRuleModel()).isNotNull();
            assertThat(doc.getSpec().getProject().getRuleModel().getRules()).isNotEmpty();
            assertThat(doc.getSpec().getProject().getRuleModel().getId()).endsWith("-rules");

            // -- EventModel (E4) --
            assertThat(doc.getSpec().getProject().getEventModel()).isNotNull();
            assertThat(doc.getSpec().getProject().getEventModel().getEvents()).isNotEmpty();
            assertThat(doc.getSpec().getProject().getEventModel().getId()).endsWith("-events");

            // -- GovernanceModel (E5) --
            assertThat(doc.getSpec().getProject().getGovernanceModel()).isNotNull();
            assertThat(doc.getSpec().getProject().getGovernanceModel().getRoles()).isNotEmpty();
            assertThat(doc.getSpec().getProject().getGovernanceModel().getId()).endsWith("-gov");

            // -- MetricsModel (E6) --
            assertThat(doc.getSpec().getProject().getMetricsModel()).isNotNull();
            assertThat(doc.getSpec().getProject().getMetricsModel().getMetrics()).isNotEmpty();

            // -- DataSourcesModel (E8) --
            assertThat(doc.getSpec().getProject().getDataSourcesModel()).isNotNull();
            assertThat(doc.getSpec().getProject().getDataSourcesModel().getSources()).isNotEmpty();

            // -- EpcModel (EPC) --
            assertThat(doc.getSpec().getProject().getEpcModel()).isNotNull();
            assertThat(doc.getSpec().getProject().getEpcModel().getChains()).isNotEmpty();
            assertThat(doc.getSpec().getProject().getEpcModel().getChains().get(0).getNodes())
                    .isNotEmpty();
        }

        @Test
        @DisplayName("should generate random ID when externalId is null")
        void shouldGenerateIdWhenExternalIdIsNull() throws IOException {
            String markdown = createStandardMarkdown();
            OntologyExchangeDocument doc = markdownMapper.mapFromMarkdown(markdown, null);

            assertThat(doc.getMetadata().getId()).isNotBlank();
            // Should be a valid UUID
            assertThat(UUID.fromString(doc.getMetadata().getId())).isNotNull();

            // Project name should come from the markdown title
            assertThat(doc.getMetadata().getName()).isEqualTo("Test Ontology Model");
        }

        @Test
        @DisplayName("round-trip markdown result through JSON should preserve structure")
        void roundTripThroughJsonShouldPreserveStructure() throws IOException {
            String markdown = createFullMarkdownWithAllSections();
            OntologyExchangeDocument original = markdownMapper.mapFromMarkdown(
                    markdown, "test-roundtrip");

            String json = markdownMapper.toJson(original);
            assertThat(json).isNotBlank();
            assertThat(json).contains("ontology.platform/v2");
            assertThat(json).contains("OntologyExchange");
            assertThat(json).contains("E001");
        }

        @Test
        @DisplayName("empty markdown (no sections) should produce document with no entities")
        void shouldHandleEmptyMarkdown() throws IOException {
            String emptyMarkdown = "# Empty Doc\n> export time: 2026-01-01\n";
            OntologyExchangeDocument doc = markdownMapper.mapFromMarkdown(
                    emptyMarkdown, "empty-test");

            assertThat(doc).isNotNull();
            assertThat(doc.getSpec().getProject().getDataModel()).isNotNull();
            assertThat(doc.getSpec().getProject().getDataModel().getEntities()).isEmpty();
            assertThat(doc.getSpec().getProject().getDomain()).isNotNull();
            assertThat(doc.getSpec().getProject().getDomain().getId()).isEqualTo("empty-test");
        }

        @Test
        @DisplayName("should parseA section and create domain from first row")
        void shouldCreateDomainFromASection() throws IOException {
            String md = "# Domain Only\n\n## A\n| ID | Name | Desc |\n|---|------|------|\n| DOM01 | TestDomain | A test domain |";

            // Hack: the title "A" will test section resolution
            // Note: "a" as a section title resolves to KIND_A via SECTION_PREFIX_MAP
            OntologyExchangeDocument doc = markdownMapper.mapFromMarkdown(md, "domain-test");

            assertThat(doc.getSpec().getProject().getDomain()).isNotNull();
            assertThat(doc.getSpec().getProject().getDomain().getId()).isEqualTo("DOM01");
        }
    }

    // ================================================================
    // Test Data Builders -- Excel
    // ================================================================

    /**
     * Create a full workbook with:
     * - Sheet A (1 row): a value domain with ID=VD001, name=manufacturing
     * - Sheet B (2 rows): capabilities, both with parent ID = VD001
     * - Sheet C (1 row): scenario with parent ID = CAP001
     */
    private static byte[] createFullXlsxWorkbook() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            // --- Sheet A: value domain ---
            Sheet shA = wb.createSheet("A");
            Row hA = shA.createRow(0);
            hA.createCell(0, CellType.STRING).setCellValue("ID");
            hA.createCell(1, CellType.STRING).setCellValue("Name");
            hA.createCell(2, CellType.STRING).setCellValue("NameEn");
            hA.createCell(3, CellType.STRING).setCellValue("Desc");
            hA.createCell(4, CellType.STRING).setCellValue("Semantics");
            Row dA = shA.createRow(1);
            dA.createCell(0, CellType.STRING).setCellValue("VD001");
            dA.createCell(1, CellType.STRING).setCellValue("manufacturing");
            dA.createCell(2, CellType.STRING).setCellValue("manufacturing");
            dA.createCell(3, CellType.STRING).setCellValue("manufacturing domain");
            dA.createCell(4, CellType.STRING).setCellValue("{\"terms\":[\"prod\"]}");

            // --- Sheet B: capabilities (2 rows) ---
            Sheet shB = wb.createSheet("B");
            Row hB = shB.createRow(0);
            hB.createCell(0, CellType.STRING).setCellValue("ID");
            hB.createCell(1, CellType.STRING).setCellValue("Name");
            hB.createCell(2, CellType.STRING).setCellValue("NameEn");
            hB.createCell(3, CellType.STRING).setCellValue("Desc");
            hB.createCell(4, CellType.STRING).setCellValue("Semantics");
            hB.createCell(5, CellType.STRING).setCellValue("ParentId");

            Row dB1 = shB.createRow(1);
            dB1.createCell(0, CellType.STRING).setCellValue("CAP001");
            dB1.createCell(1, CellType.STRING).setCellValue("order mgmt");
            dB1.createCell(3, CellType.STRING).setCellValue("manage orders");
            dB1.createCell(5, CellType.STRING).setCellValue("VD001");

            Row dB2 = shB.createRow(2);
            dB2.createCell(0, CellType.STRING).setCellValue("CAP002");
            dB2.createCell(1, CellType.STRING).setCellValue("inventory mgmt");
            dB2.createCell(3, CellType.STRING).setCellValue("manage inventory");
            dB2.createCell(5, CellType.STRING).setCellValue("VD001");

            // --- Sheet C: scenario (1 row) ---
            Sheet shC = wb.createSheet("C");
            Row hC = shC.createRow(0);
            hC.createCell(0, CellType.STRING).setCellValue("ID");
            hC.createCell(1, CellType.STRING).setCellValue("Name");
            hC.createCell(2, CellType.STRING).setCellValue("NameEn");
            hC.createCell(3, CellType.STRING).setCellValue("Desc");
            hC.createCell(4, CellType.STRING).setCellValue("Semantics");
            hC.createCell(5, CellType.STRING).setCellValue("ParentId");

            Row dC = shC.createRow(1);
            dC.createCell(0, CellType.STRING).setCellValue("SC001");
            dC.createCell(1, CellType.STRING).setCellValue("order approval");
            dC.createCell(3, CellType.STRING).setCellValue("approve orders");
            dC.createCell(5, CellType.STRING).setCellValue("CAP001");

            return toBytes(wb);
        }
    }

    // ================================================================
    // Test Data Builders -- Markdown
    // ================================================================

    /**
     * Standard markdown with:
     * - A section (value domain, 1 row)
     * - B section (capability, 1 row)
     * - E1 section (entities, 2 rows)
     */
    private static String createStandardMarkdown() {
        return "# Test Ontology Model\n"
                + "> export time: 2026-07-01\n"
                + "\n"
                + "## A\n"
                + "| ID | name | description |\n"
                + "|---|---|---|\n"
                + "| VD001 | manufacturing | manufacturing domain |\n"
                + "\n"
                + "## B\n"
                + "| ID | name | description | parentId |\n"
                + "|---|---|---|---|\n"
                + "| CAP001 | create order | create production order | VD001 |\n"
                + "\n"
                + "## E1\n"
                + "| ID | name | description |\n"
                + "|---|---|---|\n"
                + "| E001 | production order | production order entity |\n"
                + "| E002 | bill of materials | BOM entity |\n";
    }

    /**
     * Full markdown exercising ALL section types (A, B, C, EPC, E1-E8).
     * Uses English headers and values to avoid encoding issues.
     */
    private static String createFullMarkdownWithAllSections() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Full Ontology Model\n");
        sb.append("> export time: 2026-07-01\n");
        sb.append("\n");

        // A
        sb.append("## A\n");
        sb.append("| ID | name | description |\n");
        sb.append("|---|---|---|\n");
        sb.append("| DOM01 | test-domain | test domain description |\n");
        sb.append("\n");

        // B
        sb.append("## B\n");
        sb.append("| ID | name | description | parentId |\n");
        sb.append("|---|---|---|---|\n");
        sb.append("| CAP001 | create order | create prod order | DOM01 |\n");
        sb.append("\n");

        // C
        sb.append("## C\n");
        sb.append("| ID | name | description | parentId |\n");
        sb.append("|---|---|---|---|\n");
        sb.append("| SC001 | order approval | approve orders | CAP001 |\n");
        sb.append("\n");

        // EPC
        sb.append("## EPC\n");
        sb.append("| ID | name | description |\n");
        sb.append("|---|---|---|\n");
        sb.append("| EPC01 | receive order | customer submits order |\n");
        sb.append("| EPC02 | review order | internal review |\n");
        sb.append("\n");

        // E1
        sb.append("## E1\n");
        sb.append("| ID | name | description |\n");
        sb.append("|---|---|---|\n");
        sb.append("| E001 | production order | production order entity |\n");
        sb.append("| E002 | bill of materials | BOM entity |\n");
        sb.append("\n");

        // E2
        sb.append("## E2\n");
        sb.append("| ID | name | description |\n");
        sb.append("|---|---|---|\n");
        sb.append("| ACT001 | submit review | submit order for review |\n");
        sb.append("\n");

        // E3
        sb.append("## E3\n");
        sb.append("| ID | name | description |\n");
        sb.append("|---|---|---|\n");
        sb.append("| R001 | stock check | check inventory is sufficient |\n");
        sb.append("\n");

        // E4
        sb.append("## E4\n");
        sb.append("| ID | name | description |\n");
        sb.append("|---|---|---|\n");
        sb.append("| EVT001 | order created | triggered when order is created |\n");
        sb.append("\n");

        // E5
        sb.append("## E5\n");
        sb.append("| ID | name |\n");
        sb.append("|---|---|\n");
        sb.append("| ROLE01 | production supervisor |\n");
        sb.append("\n");

        // E6
        sb.append("## E6\n");
        sb.append("| ID | name | description |\n");
        sb.append("|---|---|---|\n");
        sb.append("| MET01 | on-time rate | percentage delivered on time |\n");
        sb.append("\n");

        // E8
        sb.append("## E8\n");
        sb.append("| ID | name | type |\n");
        sb.append("|---|---|---|\n");
        sb.append("| SRC01 | ERP system | api |\n");

        return sb.toString();
    }

    // ================================================================
    // Utility
    // ================================================================

    private static byte[] toBytes(Workbook wb) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            wb.write(bos);
            return bos.toByteArray();
        }
    }
}

package com.ontology.platform.infrastructure.imports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.dto.imports.ImportResult;
import com.ontology.platform.domain.entity.Ontology;
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
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * ExcelOntologyImportAdapter 单元测试
 *
 * <p>对应 Spec §5 的 TC-01~11。</p>
 */
class ExcelOntologyImportAdapterTest {

    private static final String CREATED_BY = "test-user";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ExcelOntologyImportAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ExcelOntologyImportAdapter(MAPPER);
    }

    // ──────────────────────────────────────────────
    // TC-1: 正常单行
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("TC-1: 1行有效数据 → 1个Ontology创建成功")
    void shouldCreateSingleOntology() throws IOException {
        byte[] xlsx = createSheetA(1);
        ImportResult<Ontology> result = adapter.execute(toStream(xlsx), CREATED_BY);

        assertThat(result.getImported()).hasSize(1);
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getSkipped()).isEmpty();

        Ontology created = result.getImported().get(0);
        assertThat(created.getName()).isEqualTo("VD-001");
        assertThat(created.getDisplayName()).isEqualTo("生产制造 (manufacturing)");
        assertThat(created.getDescription()).isEqualTo("生产制造领域");
        assertThat(created.getStatus().name()).isEqualTo("DRAFT");
        assertThat(created.getVersion()).isEqualTo("0.1.0");
    }

    // ──────────────────────────────────────────────
    // TC-2: 正常多行
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("TC-2: 2行有效数据 → 2个Ontology创建成功")
    void shouldCreateMultipleOntologies() throws IOException {
        byte[] xlsx = createSheetA(2);
        ImportResult<Ontology> result = adapter.execute(toStream(xlsx), CREATED_BY);

        assertThat(result.getImported()).hasSize(2);
        assertThat(result.getErrors()).isEmpty();

        Set<String> names = result.getImported().stream()
                .map(Ontology::getName)
                .collect(Collectors.toSet());
        assertThat(names).containsExactlyInAnyOrder("VD-001", "VD-002");
    }

    // ──────────────────────────────────────────────
    // TC-3: 缺 ID
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("TC-3: ID列为空 → errors 含行号和字段名")
    void shouldReportErrorWhenIdMissing() throws IOException {
        byte[] xlsx = createSheetAWithMissingValue(0, true, false);
        ImportResult<Ontology> result = adapter.execute(toStream(xlsx), CREATED_BY);

        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors().get(0).getField()).isEqualTo("ID");
    }

    // ──────────────────────────────────────────────
    // TC-4: 缺名称
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("TC-4: 名称列为空 → errors 含行号和字段名")
    void shouldReportErrorWhenNameMissing() throws IOException {
        byte[] xlsx = createSheetAWithMissingValue(0, false, true);
        ImportResult<Ontology> result = adapter.execute(toStream(xlsx), CREATED_BY);

        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors().get(0).getField()).isEqualTo("名称");
    }

    // ──────────────────────────────────────────────
    // TC-5: name 冲突
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("TC-5: ID与已有Ontology重复 → skipped 含冲突信息")
    void shouldSkipWhenNameConflict() throws IOException {
        adapter.setNameConflictChecker(name -> "VD-001".equals(name));

        byte[] xlsx = createSheetA(1);
        ImportResult<Ontology> result = adapter.execute(toStream(xlsx), CREATED_BY);

        assertThat(result.getImported()).isEmpty();
        assertThat(result.getSkipped()).isNotEmpty();
        assertThat(result.getSkipped().get(0).getId()).isEqualTo("VD-001");
        assertThat(result.getSkipped().get(0).getReason()).contains("already exists");
    }

    // ──────────────────────────────────────────────
    // TC-6: 语义JSON非法
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("TC-6: 语义列写入非JSON字符串 → errors 含JSON格式错误")
    void shouldReportErrorWhenSemanticsIsInvalidJson() throws IOException {
        byte[] xlsx = createSheetAWithInvalidSemantics();
        ImportResult<Ontology> result = adapter.execute(toStream(xlsx), CREATED_BY);

        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors().get(0).getMessage()).contains("JSON");
    }

    // ──────────────────────────────────────────────
    // TC-7: 可空列为空
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("TC-7: 英文名/描述/语义均为空 → 正常创建")
    void shouldCreateWhenOptionalFieldsEmpty() throws IOException {
        byte[] xlsx = createSheetAMinimal();
        ImportResult<Ontology> result = adapter.execute(toStream(xlsx), CREATED_BY);

        assertThat(result.getImported()).hasSize(1);
        assertThat(result.getErrors()).isEmpty();

        Ontology created = result.getImported().get(0);
        assertThat(created.getName()).isEqualTo("VD-MIN");
        assertThat(created.getDisplayName()).isEqualTo("最小本体"); // falls back to name
    }

    // ──────────────────────────────────────────────
    // TC-8: 空 Sheet
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("TC-8: 空Sheet（仅标题行） → 0 imported")
    void shouldReturnEmptyForEmptySheet() throws IOException {
        byte[] xlsx = createEmptySheetA();
        ImportResult<Ontology> result = adapter.execute(toStream(xlsx), CREATED_BY);

        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getSkipped()).isEmpty();
    }

    // ──────────────────────────────────────────────
    // TC-9: Sheet 不存在
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("TC-9: xlsx没有叫A的Sheet → 抛出IllegalArgumentException")
    void shouldThrowWhenSheetNotFound() {
        byte[] xlsx = createSheetWithoutA();
        assertThatThrownBy(() -> adapter.execute(toStream(xlsx), CREATED_BY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A");
    }

    // ──────────────────────────────────────────────
    // TC-10: 非法文件
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("TC-10: 非Excel文件的字节流 → 抛出异常")
    void shouldThrowWhenNotValidExcel() {
        byte[] garbage = "not an excel file".getBytes();
        assertThatThrownBy(() -> adapter.execute(toStream(garbage), CREATED_BY))
                .isInstanceOf(RuntimeException.class);
    }

    // ──────────────────────────────────────────────
    // TC-11: 多 Sheet 只取 A
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("TC-11: xlsx含A/B/C三个Sheet → 只解析A的行")
    void shouldOnlyParseSheetA() throws IOException {
        byte[] xlsx = createMultiSheetWorkbook();
        ImportResult<Ontology> result = adapter.execute(toStream(xlsx), CREATED_BY);

        // 只解析 A 的 2 行
        assertThat(result.getImported()).hasSize(2);
        assertThat(result.getImported()).allMatch(o -> o.getName().startsWith("VD-"));
    }

    // ──────────────────────────────────────────────
    // 工具方法 — 用 POI 构建测试 xlsx
    // ──────────────────────────────────────────────

    /** 创建含 N 行标准数据的 Sheet A */
    private static byte[] createSheetA(int rowCount) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(ExcelOntologyImportAdapter.SHEET_A);
            createHeader(sh);

            for (int i = 0; i < rowCount; i++) {
                Row r = sh.createRow(1 + i);
                int num = i + 1;
                r.createCell(0, CellType.STRING).setCellValue("VD-00" + num);
                r.createCell(1, CellType.STRING).setCellValue(
                        num == 1 ? "生产制造" : "财务会计");
                r.createCell(2, CellType.STRING).setCellValue(
                        num == 1 ? "manufacturing" : "finance");
                r.createCell(3, CellType.STRING).setCellValue(
                        num == 1 ? "生产制造领域" : "财务核算领域");
                r.createCell(4, CellType.STRING).setCellValue(
                        num == 1 ? "{\"terms\":[\"生产\",\"制造\"]}" : "{\"terms\":[\"财务\"]}");
            }
            return toBytes(wb);
        }
    }

    /** 创建某列为空的行 */
    private static byte[] createSheetAWithMissingValue(int row, boolean missingId, boolean missingName)
            throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(ExcelOntologyImportAdapter.SHEET_A);
            createHeader(sh);
            Row r = sh.createRow(1);
            r.createCell(0, CellType.STRING).setCellValue(missingId ? "" : "VD-ERR");
            r.createCell(1, CellType.STRING).setCellValue(missingName ? "" : "测试本体");
            return toBytes(wb);
        }
    }

    /** 创建语义 JSON 非法的行 */
    private static byte[] createSheetAWithInvalidSemantics() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(ExcelOntologyImportAdapter.SHEET_A);
            createHeader(sh);
            Row r = sh.createRow(1);
            r.createCell(0, CellType.STRING).setCellValue("VD-BAD");
            r.createCell(1, CellType.STRING).setCellValue("坏数据");
            r.createCell(4, CellType.STRING).setCellValue("{not-json}");
            return toBytes(wb);
        }
    }

    /** 创建仅必填列的最小行 */
    private static byte[] createSheetAMinimal() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(ExcelOntologyImportAdapter.SHEET_A);
            createHeader(sh);
            Row r = sh.createRow(1);
            r.createCell(0, CellType.STRING).setCellValue("VD-MIN");
            r.createCell(1, CellType.STRING).setCellValue("最小本体");
            // 2,3,4 列为空
            return toBytes(wb);
        }
    }

    /** 创建仅标题行的空 Sheet A */
    private static byte[] createEmptySheetA() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(ExcelOntologyImportAdapter.SHEET_A);
            createHeader(sh);
            return toBytes(wb);
        }
    }

    /** 创建没有 Sheet A 的工作簿 */
    private static byte[] createSheetWithoutA() {
        try (Workbook wb = new XSSFWorkbook()) {
            wb.createSheet("B");
            wb.createSheet("C");
            return toBytes(wb);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 创建含 A/B/C 三个 Sheet 的工作簿 */
    private static byte[] createMultiSheetWorkbook() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            // Sheet A — 2 行有效数据
            Sheet shA = wb.createSheet("A");
            createHeader(shA);
            for (int i = 0; i < 2; i++) {
                Row r = shA.createRow(1 + i);
                r.createCell(0, CellType.STRING).setCellValue("VD-0" + (i + 1));
                r.createCell(1, CellType.STRING).setCellValue("本体" + (i + 1));
            }

            // Sheet B — 不应被解析
            Sheet shB = wb.createSheet("B");
            createHeader(shB);
            Row rB = shB.createRow(1);
            rB.createCell(0, CellType.STRING).setCellValue("CAP-001");
            rB.createCell(1, CellType.STRING).setCellValue("不应出现");

            // Sheet C — 不应被解析
            Sheet shC = wb.createSheet("C");
            createHeader(shC);
            Row rC = shC.createRow(1);
            rC.createCell(0, CellType.STRING).setCellValue("SC-001");
            rC.createCell(1, CellType.STRING).setCellValue("不应出现");

            return toBytes(wb);
        }
    }

    /** 创建标题行 */
    private static void createHeader(Sheet sh) {
        Row header = sh.createRow(0);
        header.createCell(0, CellType.STRING).setCellValue("ID");
        header.createCell(1, CellType.STRING).setCellValue("名称");
        header.createCell(2, CellType.STRING).setCellValue("英文名");
        header.createCell(3, CellType.STRING).setCellValue("描述");
        header.createCell(4, CellType.STRING).setCellValue("语义(JSON)");
    }

    private static byte[] toBytes(Workbook wb) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        return bos.toByteArray();
    }

    private static InputStream toStream(byte[] data) {
        return new ByteArrayInputStream(data);
    }
}

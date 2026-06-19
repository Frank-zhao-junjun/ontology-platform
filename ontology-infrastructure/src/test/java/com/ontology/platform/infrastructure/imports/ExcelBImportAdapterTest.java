package com.ontology.platform.infrastructure.imports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.dto.imports.ImportResult;
import com.ontology.platform.domain.entity.ObjectType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * ExcelBImportAdapter 单元测试
 *
 * <p>对应 Spec §5 的 TC-01~07。</p>
 */
class ExcelBImportAdapterTest {

    private static final String ONTOLOGY_ID = "test-ontology-id";
    private static final String CREATED_BY = "test-user";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ExcelBImportAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ExcelBImportAdapter(MAPPER);
    }

    @Test
    @DisplayName("TC-1: 1行有效B数据 → 创建成功，字段映射正确")
    void shouldCreateSingleObjectType() throws IOException {
        byte[] xlsx = createSheetB(1);
        ImportResult<ObjectType> result = adapter.execute(toStream(xlsx), ONTOLOGY_ID, CREATED_BY);

        assertThat(result.getImported()).hasSize(1);
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getSkipped()).isEmpty();

        ObjectType ot = result.getImported().get(0);
        assertThat(ot.getName()).isEqualTo("CAP-001");
        assertThat(ot.getDisplayName()).isEqualTo("计划能力");
        assertThat(ot.getDescription()).isEqualTo("生产计划相关能力");
        assertThat(ot.getParentId()).isEqualTo("VD-001");
        assertThat(ot.getOntologyId()).isEqualTo(ONTOLOGY_ID);
    }

    @Test
    @DisplayName("TC-2: 3行有效数据 → 全部创建成功")
    void shouldCreateMultiple() throws IOException {
        byte[] xlsx = createSheetB(3);
        ImportResult<ObjectType> result = adapter.execute(toStream(xlsx), ONTOLOGY_ID, CREATED_BY);

        assertThat(result.getImported()).hasSize(3);
        assertThat(result.getErrors()).isEmpty();

        Set<String> names = result.getImported().stream()
                .map(ObjectType::getName)
                .collect(Collectors.toSet());
        assertThat(names).containsExactlyInAnyOrder("CAP-001", "CAP-002", "CAP-003");
    }

    @Test
    @DisplayName("TC-3: ID列为空 → errors")
    void shouldReportErrorWhenIdMissing() throws IOException {
        byte[] xlsx = createSheetBWithMissing(0, true, false, false);
        ImportResult<ObjectType> result = adapter.execute(toStream(xlsx), ONTOLOGY_ID, CREATED_BY);

        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors().get(0).getField()).isEqualTo("ID");
    }

    @Test
    @DisplayName("TC-4: 父节点ID为空 → errors")
    void shouldReportErrorWhenParentIdMissing() throws IOException {
        byte[] xlsx = createSheetBWithMissing(0, false, false, true);
        ImportResult<ObjectType> result = adapter.execute(toStream(xlsx), ONTOLOGY_ID, CREATED_BY);

        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors().get(0).getField()).isEqualTo("父节点ID");
    }

    @Test
    @DisplayName("TC-5: name冲突 → skipped")
    void shouldSkipWhenNameConflict() throws IOException {
        adapter.setNameConflictChecker(name -> "CAP-001".equals(name));

        byte[] xlsx = createSheetB(1);
        ImportResult<ObjectType> result = adapter.execute(toStream(xlsx), ONTOLOGY_ID, CREATED_BY);

        assertThat(result.getImported()).isEmpty();
        assertThat(result.getSkipped()).isNotEmpty();
        assertThat(result.getSkipped().get(0).getId()).isEqualTo("CAP-001");
    }

    @Test
    @DisplayName("TC-6: 空Sheet（仅标题行）→ 0 imported")
    void shouldHandleEmptySheet() throws IOException {
        byte[] xlsx = createEmptySheetB();
        ImportResult<ObjectType> result = adapter.execute(toStream(xlsx), ONTOLOGY_ID, CREATED_BY);

        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("TC-7: Sheet B 不存在 → 异常")
    void shouldThrowWhenSheetNotFound() {
        byte[] xlsx = createSheetWithoutB();
        assertThatThrownBy(() -> adapter.execute(toStream(xlsx), ONTOLOGY_ID, CREATED_BY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("B");
    }

    @Test
    @DisplayName("TC-8: 非法文件 → 异常")
    void shouldThrowWhenNotValidExcel() {
        byte[] garbage = "not an excel file".getBytes();
        assertThatThrownBy(() -> adapter.execute(toStream(garbage), ONTOLOGY_ID, CREATED_BY))
                .isInstanceOf(RuntimeException.class);
    }

    // ==================== 测试夹具 ====================

    private static byte[] createSheetB(int rowCount) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(ExcelBImportAdapter.SHEET_B);
            createHeader(sh);
            for (int i = 0; i < rowCount; i++) {
                Row r = sh.createRow(1 + i);
                int num = i + 1;
                r.createCell(0, CellType.STRING).setCellValue("CAP-00" + num);
                r.createCell(1, CellType.STRING).setCellValue(
                        num == 1 ? "计划能力" : "执行能力");
                r.createCell(2, CellType.STRING).setCellValue(
                        num == 1 ? "planning" : "execution");
                r.createCell(3, CellType.STRING).setCellValue(
                        num == 1 ? "生产计划相关能力" : "生产执行相关能力");
                r.createCell(5, CellType.STRING).setCellValue("VD-001");
            }
            return toBytes(wb);
        }
    }

    private static byte[] createSheetBWithMissing(int row, boolean missingId, boolean missingName, boolean missingParentId)
            throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(ExcelBImportAdapter.SHEET_B);
            createHeader(sh);
            Row r = sh.createRow(1);
            r.createCell(0, CellType.STRING).setCellValue(missingId ? "" : "CAP-ERR");
            r.createCell(1, CellType.STRING).setCellValue(missingName ? "" : "错误能力");
            r.createCell(5, CellType.STRING).setCellValue(missingParentId ? "" : "VD-001");
            return toBytes(wb);
        }
    }

    private static byte[] createEmptySheetB() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            wb.createSheet(ExcelBImportAdapter.SHEET_B);
            createHeader(wb.getSheet(ExcelBImportAdapter.SHEET_B));
            return toBytes(wb);
        }
    }

    private static byte[] createSheetWithoutB() {
        try (Workbook wb = new XSSFWorkbook()) {
            wb.createSheet("A");
            wb.createSheet("C");
            return toBytes(wb);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createHeader(Sheet sh) {
        Row header = sh.createRow(0);
        header.createCell(0, CellType.STRING).setCellValue("ID");
        header.createCell(1, CellType.STRING).setCellValue("名称");
        header.createCell(2, CellType.STRING).setCellValue("英文名");
        header.createCell(3, CellType.STRING).setCellValue("描述");
        header.createCell(4, CellType.STRING).setCellValue("语义(JSON)");
        header.createCell(5, CellType.STRING).setCellValue("父节点ID");
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

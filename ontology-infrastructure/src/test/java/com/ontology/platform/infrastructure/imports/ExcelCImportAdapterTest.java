package com.ontology.platform.infrastructure.imports;

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

class ExcelCImportAdapterTest {

    private static final String ONTOLOGY_ID = "test-ontology-id";
    private static final String CREATED_BY = "test-user";

    private ExcelCImportAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ExcelCImportAdapter(new com.fasterxml.jackson.databind.ObjectMapper());
    }

    @Test
    @DisplayName("TC-1: 1行有效数据 → 创建成功")
    void shouldCreateSingle() throws IOException {
        byte[] xlsx = createSheetC(1);
        ImportResult<ObjectType> result = adapter.execute(toStream(xlsx), ONTOLOGY_ID, CREATED_BY);

        assertThat(result.getImported()).hasSize(1);
        assertThat(result.getErrors()).isEmpty();

        ObjectType ot = result.getImported().get(0);
        assertThat(ot.getName()).isEqualTo("SC-001");
        assertThat(ot.getDisplayName()).isEqualTo("排产场景");
        assertThat(ot.getParentId()).isEqualTo("CAP-001");
        assertThat(ot.getOntologyId()).isEqualTo(ONTOLOGY_ID);
    }

    @Test
    @DisplayName("TC-2: 3行有效数据 → 全部创建")
    void shouldCreateMultiple() throws IOException {
        byte[] xlsx = createSheetC(3);
        ImportResult<ObjectType> result = adapter.execute(toStream(xlsx), ONTOLOGY_ID, CREATED_BY);

        assertThat(result.getImported()).hasSize(3);
        Set<String> names = result.getImported().stream().map(ObjectType::getName).collect(Collectors.toSet());
        assertThat(names).containsExactlyInAnyOrder("SC-001", "SC-002", "SC-003");
    }

    @Test
    @DisplayName("TC-3: 缺ID → errors")
    void shouldReportErrorWhenIdMissing() throws IOException {
        byte[] xlsx = createSheetCWithMissing(true, false, false);
        ImportResult<ObjectType> result = adapter.execute(toStream(xlsx), ONTOLOGY_ID, CREATED_BY);
        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors().get(0).getField()).isEqualTo("ID");
    }

    @Test
    @DisplayName("TC-4: 缺父节点ID → errors")
    void shouldReportErrorWhenParentIdMissing() throws IOException {
        byte[] xlsx = createSheetCWithMissing(false, false, true);
        ImportResult<ObjectType> result = adapter.execute(toStream(xlsx), ONTOLOGY_ID, CREATED_BY);
        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors().get(0).getField()).isEqualTo("父节点ID");
    }

    @Test
    @DisplayName("TC-5: name冲突 → skipped")
    void shouldSkipWhenNameConflict() throws IOException {
        adapter.setNameConflictChecker(name -> "SC-001".equals(name));
        byte[] xlsx = createSheetC(1);
        ImportResult<ObjectType> result = adapter.execute(toStream(xlsx), ONTOLOGY_ID, CREATED_BY);
        assertThat(result.getImported()).isEmpty();
        assertThat(result.getSkipped()).isNotEmpty();
        assertThat(result.getSkipped().get(0).getId()).isEqualTo("SC-001");
    }

    @Test
    @DisplayName("TC-6: 空Sheet → 0 imported")
    void shouldHandleEmptySheet() throws IOException {
        byte[] xlsx = createEmptySheetC();
        ImportResult<ObjectType> result = adapter.execute(toStream(xlsx), ONTOLOGY_ID, CREATED_BY);
        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("TC-7: Sheet C 不存在 → 异常")
    void shouldThrowWhenSheetNotFound() {
        byte[] xlsx = createSheetWithoutC();
        assertThatThrownBy(() -> adapter.execute(toStream(xlsx), ONTOLOGY_ID, CREATED_BY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("C");
    }

    @Test
    @DisplayName("TC-8: 非法文件 → 异常")
    void shouldThrowWhenNotValidExcel() {
        assertThatThrownBy(() -> adapter.execute(toStream("garbage".getBytes()), ONTOLOGY_ID, CREATED_BY))
                .isInstanceOf(RuntimeException.class);
    }

    // ==================== 夹具 ====================

    private static byte[] createSheetC(int rowCount) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(ExcelCImportAdapter.SHEET_C);
            createHeader(sh);
            for (int i = 0; i < rowCount; i++) {
                Row r = sh.createRow(1 + i);
                int n = i + 1;
                r.createCell(0, CellType.STRING).setCellValue("SC-00" + n);
                r.createCell(1, CellType.STRING).setCellValue(n == 1 ? "排产场景" : "采购场景");
                r.createCell(3, CellType.STRING).setCellValue(n == 1 ? "MTS排产" : "原材料采购");
                r.createCell(5, CellType.STRING).setCellValue("CAP-001");
            }
            return toBytes(wb);
        }
    }

    private static byte[] createSheetCWithMissing(boolean missingId, boolean missingName, boolean missingParentId) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(ExcelCImportAdapter.SHEET_C);
            createHeader(sh);
            Row r = sh.createRow(1);
            r.createCell(0, CellType.STRING).setCellValue(missingId ? "" : "SC-ERR");
            r.createCell(1, CellType.STRING).setCellValue(missingName ? "" : "错误场景");
            r.createCell(5, CellType.STRING).setCellValue(missingParentId ? "" : "CAP-001");
            return toBytes(wb);
        }
    }

    private static byte[] createEmptySheetC() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            wb.createSheet(ExcelCImportAdapter.SHEET_C);
            createHeader(wb.getSheet(ExcelCImportAdapter.SHEET_C));
            return toBytes(wb);
        }
    }

    private static byte[] createSheetWithoutC() {
        try (Workbook wb = new XSSFWorkbook()) { wb.createSheet("A"); return toBytes(wb); }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    private static void createHeader(Sheet sh) {
        Row h = sh.createRow(0);
        h.createCell(0, CellType.STRING).setCellValue("ID");
        h.createCell(1, CellType.STRING).setCellValue("名称");
        h.createCell(2, CellType.STRING).setCellValue("英文名");
        h.createCell(3, CellType.STRING).setCellValue("描述");
        h.createCell(4, CellType.STRING).setCellValue("语义(JSON)");
        h.createCell(5, CellType.STRING).setCellValue("父节点ID");
    }

    private static byte[] toBytes(Workbook wb) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        return bos.toByteArray();
    }

    private static InputStream toStream(byte[] data) { return new ByteArrayInputStream(data); }
}

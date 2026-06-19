package com.ontology.platform.infrastructure.imports;

import com.ontology.platform.domain.dto.imports.ExcelEDimRow;
import com.ontology.platform.domain.dto.imports.ImportResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

class ExcelEDimImportAdapterTest {

    private final ExcelEDimImportAdapter adapter = new ExcelEDimImportAdapter();

    @Test
    @DisplayName("TC-1: E1 sheet 1行 → 解析成功")
    void shouldParseE1() throws IOException {
        byte[] xlsx = createEDimSheet("E1", 1);
        ImportResult<ExcelEDimRow> result = adapter.execute(toStream(xlsx), "E1");

        assertThat(result.getImported()).hasSize(1);
        ExcelEDimRow row = result.getImported().get(0);
        assertThat(row.getElementId()).isEqualTo("ENT-001");
        assertThat(row.getName()).isEqualTo("生产订单");
        assertThat(row.getDimension()).isEqualTo("E1");
        assertThat(row.getDescription()).isEqualTo("生产订单描述");
    }

    @Test
    @DisplayName("TC-2: E2 sheet 1行 → 解析成功（验证维度值）")
    void shouldParseE2() throws IOException {
        byte[] xlsx = createEDimSheet("E2", 1);
        ImportResult<ExcelEDimRow> result = adapter.execute(toStream(xlsx), "E2");

        assertThat(result.getImported()).hasSize(1);
        assertThat(result.getImported().get(0).getDimension()).isEqualTo("E2");
    }

    @Test
    @DisplayName("E1+E3+E5 3个sheet各1行 → 全部可独立解析")
    void shouldParseMultipleSheetsIndependently() throws IOException {
        byte[] xlsx = createMultiESheet();
        assertThat(adapter.execute(toStream(xlsx), "E1").getImported()).hasSize(1);
        assertThat(adapter.execute(toStream(xlsx), "E3").getImported()).hasSize(1);
        assertThat(adapter.execute(toStream(xlsx), "E5").getImported()).hasSize(1);
    }

    @Test
    @DisplayName("TC-3: 缺ID → errors")
    void shouldReportErrorWhenIdMissing() throws IOException {
        byte[] xlsx = createEDimSheetWithMissing("E1", true, false, false);
        ImportResult<ExcelEDimRow> result = adapter.execute(toStream(xlsx), "E1");
        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors().get(0).getField()).isEqualTo("ID");
    }

    @Test
    @DisplayName("TC-4: 缺名称 → errors")
    void shouldReportErrorWhenNameMissing() throws IOException {
        byte[] xlsx = createEDimSheetWithMissing("E1", false, true, false);
        ImportResult<ExcelEDimRow> result = adapter.execute(toStream(xlsx), "E1");
        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors().get(0).getField()).isEqualTo("名称");
    }

    @Test
    @DisplayName("TC-5: 缺维度 → errors")
    void shouldReportErrorWhenDimensionMissing() throws IOException {
        byte[] xlsx = createEDimSheetWithMissing("E1", false, false, true);
        ImportResult<ExcelEDimRow> result = adapter.execute(toStream(xlsx), "E1");
        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors().get(0).getField()).isEqualTo("维度");
    }

    @Test
    @DisplayName("维度值非法（非E1~E8）→ errors")
    void shouldRejectInvalidDimension() throws IOException {
        byte[] xlsx = createEDimSheetWithInvalidDimension("E1", "E99");
        ImportResult<ExcelEDimRow> result = adapter.execute(toStream(xlsx), "E1");
        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    @DisplayName("TC-6: 可见性枚举值非法 → errors")
    void shouldRejectInvalidVisibility() throws IOException {
        byte[] xlsx = createEDimSheetWithInvalidVisibility("E1", "invalid_vis");
        ImportResult<ExcelEDimRow> result = adapter.execute(toStream(xlsx), "E1");
        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    @DisplayName("TC-7: 空Sheet → 0 imported")
    void shouldHandleEmptySheet() throws IOException {
        byte[] xlsx = createEmptyEDimSheet("E3");
        ImportResult<ExcelEDimRow> result = adapter.execute(toStream(xlsx), "E3");
        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("TC-8: Sheet不存在 → 异常")
    void shouldThrowWhenSheetNotFound() throws IOException {
        byte[] xlsx = createEmptyEDimSheet("A");
        assertThatThrownBy(() -> adapter.execute(toStream(xlsx), "E5"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("E5");
    }

    @Test
    @DisplayName("可选列（可见性/描述/英文名）为空 → 正常创建")
    void shouldHandleOptionalFields() throws IOException {
        byte[] xlsx = createMinimalEDimSheet("E4");
        ImportResult<ExcelEDimRow> result = adapter.execute(toStream(xlsx), "E4");
        assertThat(result.getImported()).hasSize(1);
        ExcelEDimRow row = result.getImported().get(0);
        assertThat(row.getVisibility()).isNull();
        assertThat(row.getDescription()).isNull();
        assertThat(row.getNameEn()).isNull();
    }

    // ==================== 夹具 ====================

    private static byte[] createEDimSheet(String dim, int rowCount) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(dim);
            createHeader(sh);
            for (int i = 0; i < rowCount; i++) {
                Row r = sh.createRow(1 + i);
                int n = i + 1;
                String prefix = switch (dim) {
                    case "E1" -> "ENT"; case "E2" -> "ACT";
                    case "E3" -> "EVT"; case "E4" -> "RUL";
                    case "E5" -> "ROL"; case "E6" -> "MET";
                    case "E7" -> "GRD"; case "E8" -> "API";
                    default -> "EL";
                };
                r.createCell(0, CellType.STRING).setCellValue(prefix + "-00" + n);
                String name = switch (dim) {
                    case "E1" -> "生产订单"; case "E2" -> "下达动作";
                    case "E3" -> "订单事件"; case "E4" -> "校验规则";
                    case "E5" -> "生产主管"; case "E6" -> "产量指标";
                    case "E7" -> "库存约束"; case "E8" -> "SAP接口";
                    default -> "要素" + n;
                };
                r.createCell(1, CellType.STRING).setCellValue(name);
                r.createCell(2, CellType.STRING).setCellValue(name + "_en");
                r.createCell(3, CellType.STRING).setCellValue(dim);
                r.createCell(4, CellType.STRING).setCellValue("project");
                r.createCell(5, CellType.STRING).setCellValue(name + "描述");
            }
            return toBytes(wb);
        }
    }

    private static byte[] createEDimSheetWithMissing(String dim, boolean missingId, boolean missingName, boolean missingDim) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(dim);
            createHeader(sh);
            Row r = sh.createRow(1);
            r.createCell(0, CellType.STRING).setCellValue(missingId ? "" : "EL-001");
            r.createCell(1, CellType.STRING).setCellValue(missingName ? "" : "测试要素");
            r.createCell(3, CellType.STRING).setCellValue(missingDim ? "" : dim);
            return toBytes(wb);
        }
    }

    private static byte[] createEDimSheetWithInvalidDimension(String dim, String badDim) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(dim);
            createHeader(sh);
            Row r = sh.createRow(1);
            r.createCell(0, CellType.STRING).setCellValue("EL-BAD");
            r.createCell(1, CellType.STRING).setCellValue("错误维度");
            r.createCell(3, CellType.STRING).setCellValue(badDim);
            return toBytes(wb);
        }
    }

    private static byte[] createEDimSheetWithInvalidVisibility(String dim, String badVis) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(dim);
            createHeader(sh);
            Row r = sh.createRow(1);
            r.createCell(0, CellType.STRING).setCellValue("EL-BAD");
            r.createCell(1, CellType.STRING).setCellValue("错误可见性");
            r.createCell(3, CellType.STRING).setCellValue(dim);
            r.createCell(4, CellType.STRING).setCellValue(badVis);
            return toBytes(wb);
        }
    }

    private static byte[] createMultiESheet() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            for (String dim : new String[]{"E1", "E3", "E5"}) {
                Sheet sh = wb.createSheet(dim);
                createHeader(sh);
                Row r = sh.createRow(1);
                r.createCell(0, CellType.STRING).setCellValue(dim + "-001");
                r.createCell(1, CellType.STRING).setCellValue("要素" + dim);
                r.createCell(3, CellType.STRING).setCellValue(dim);
            }
            return toBytes(wb);
        }
    }

    private static byte[] createMinimalEDimSheet(String dim) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(dim);
            createHeader(sh);
            Row r = sh.createRow(1);
            r.createCell(0, CellType.STRING).setCellValue(dim + "-MIN");
            r.createCell(1, CellType.STRING).setCellValue("最小" + dim);
            r.createCell(3, CellType.STRING).setCellValue(dim);
            return toBytes(wb);
        }
    }

    private static byte[] createEmptyEDimSheet(String dim) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            wb.createSheet(dim);
            createHeader(wb.getSheet(dim));
            return toBytes(wb);
        }
    }

    private static void createHeader(Sheet sh) {
        Row h = sh.createRow(0);
        h.createCell(0, CellType.STRING).setCellValue("ID");
        h.createCell(1, CellType.STRING).setCellValue("名称");
        h.createCell(2, CellType.STRING).setCellValue("英文名");
        h.createCell(3, CellType.STRING).setCellValue("维度");
        h.createCell(4, CellType.STRING).setCellValue("可见性");
        h.createCell(5, CellType.STRING).setCellValue("描述");
    }

    private static byte[] toBytes(Workbook wb) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        return bos.toByteArray();
    }

    private static InputStream toStream(byte[] data) { return new ByteArrayInputStream(data); }
}

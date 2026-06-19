package com.ontology.platform.infrastructure.imports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.dto.imports.EpcParsedRow;
import com.ontology.platform.domain.dto.imports.EpcStepItem;
import com.ontology.platform.domain.dto.imports.ImportResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;

class ExcelEpcImportAdapterTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ExcelEpcImportAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ExcelEpcImportAdapter(MAPPER);
    }

    @Test
    @DisplayName("TC-1: 1行EPC数据含2个steps → 1 row / 2 steps")
    void shouldParseSingleRowWithTwoSteps() throws IOException {
        byte[] xlsx = createEpcSheet(1, 2);
        ImportResult<EpcParsedRow> result = adapter.execute(toStream(xlsx));

        assertThat(result.getImported()).hasSize(1);
        assertThat(result.getErrors()).isEmpty();

        EpcParsedRow row = result.getImported().get(0);
        assertThat(row.getEpcId()).isEqualTo("EPC-001");
        assertThat(row.getFlowName()).isEqualTo("订单下达流程");
        assertThat(row.getParentId()).isEqualTo("SC-001");
        assertThat(row.getScenarioId()).isEqualTo("SC-001");
        assertThat(row.getSteps()).hasSize(2);

        // 验证第一个步骤
        EpcStepItem step0 = row.getSteps().get(0);
        assertThat(step0.getStepId()).isEqualTo("s1");
        assertThat(step0.getStepName()).isEqualTo("接收订单");
        assertThat(step0.getDimension()).isEqualTo("E3");
        assertThat(step0.getElementId()).isEqualTo("EVT-001");
        assertThat(step0.getVersionPin()).isEqualTo("latest_confirmed");
        assertThat(step0.getStepOrder()).isEqualTo(0);

        // 验证第二个步骤
        EpcStepItem step1 = row.getSteps().get(1);
        assertThat(step1.getStepId()).isEqualTo("s2");
        assertThat(step1.getStepName()).isEqualTo("校验库存");
        assertThat(step1.getDimension()).isEqualTo("E2");
        assertThat(step1.getElementId()).isEqualTo("ACT-002");
        assertThat(step1.getStepOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("TC-2: 步骤 elementRef 完整 → 正确解析")
    void shouldParseElementRefCorrectly() throws IOException {
        String stepsJson = "[{\"id\":\"s1\",\"name\":\"下达\",\"elementRef\":{\"dimension\":\"E2\",\"elementId\":\"ACT-001\",\"versionPin\":\"latest_confirmed\"}}]";
        byte[] xlsx = createEpcSheetWithSteps(stepsJson);
        ImportResult<EpcParsedRow> result = adapter.execute(toStream(xlsx));

        assertThat(result.getImported()).hasSize(1);
        EpcStepItem step = result.getImported().get(0).getSteps().get(0);
        assertThat(step.getDimension()).isEqualTo("E2");
        assertThat(step.getElementId()).isEqualTo("ACT-001");
        assertThat(step.getVersionPin()).isEqualTo("latest_confirmed");
    }

    @Test
    @DisplayName("TC-3: 缺ID → errors")
    void shouldReportErrorWhenIdMissing() throws IOException {
        byte[] xlsx = createEpcSheetWithMissing("id");
        ImportResult<EpcParsedRow> result = adapter.execute(toStream(xlsx));
        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors().get(0).getField()).isEqualTo("ID");
    }

    @Test
    @DisplayName("TC-4: 缺步骤JSON → errors")
    void shouldReportErrorWhenStepsMissing() throws IOException {
        byte[] xlsx = createEpcSheetWithMissing("steps");
        ImportResult<EpcParsedRow> result = adapter.execute(toStream(xlsx));
        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors().get(0).getField()).isEqualTo("步骤(JSON)");
    }

    @Test
    @DisplayName("TC-5: 步骤不是JSON数组（非法JSON）→ errors")
    void shouldReportErrorWhenStepsNotArray() throws IOException {
        byte[] xlsx = createEpcSheetWithSteps("{\"not\":\"an array\"}");
        ImportResult<EpcParsedRow> result = adapter.execute(toStream(xlsx));
        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors().get(0).getMessage()).contains("必须");
    }

    @Test
    @DisplayName("TC-6: 步骤缺id字段 → errors")
    void shouldReportErrorWhenStepMissingId() throws IOException {
        String badStepsJson = "[{\"name\":\"无名步骤\"}]";
        byte[] xlsx = createEpcSheetWithSteps(badStepsJson);
        ImportResult<EpcParsedRow> result = adapter.execute(toStream(xlsx));
        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors().get(0).getField()).isEqualTo("步骤(JSON)");
    }

    @Test
    @DisplayName("TC-7: 空Sheet → 0 imported")
    void shouldHandleEmptySheet() throws IOException {
        byte[] xlsx = createEmptyEpcSheet();
        ImportResult<EpcParsedRow> result = adapter.execute(toStream(xlsx));
        assertThat(result.getImported()).isEmpty();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("TC-8: Sheet不存在 → 异常")
    void shouldThrowWhenSheetNotFound() {
        byte[] xlsx = createSheetWithoutEpc();
        assertThatThrownBy(() -> adapter.execute(toStream(xlsx)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EPC");
    }

    // ==================== 夹具 ====================

    private static byte[] createEpcSheet(int rowCount, int stepsPerRow) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(ExcelEpcImportAdapter.SHEET_EPC);
            createHeader(sh);
            for (int i = 0; i < rowCount; i++) {
                Row r = sh.createRow(1 + i);
                int n = i + 1;
                r.createCell(0, CellType.STRING).setCellValue("EPC-00" + n);
                r.createCell(1, CellType.STRING).setCellValue("订单下达流程");
                r.createCell(3, CellType.STRING).setCellValue("订单处理主流程");
                r.createCell(5, CellType.STRING).setCellValue("SC-001");
                r.createCell(6, CellType.STRING).setCellValue("SC-001");
                r.createCell(7, CellType.STRING).setCellValue(buildStepsJson(stepsPerRow));
            }
            return toBytes(wb);
        }
    }

    private static byte[] createEpcSheetWithSteps(String stepsJson) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(ExcelEpcImportAdapter.SHEET_EPC);
            createHeader(sh);
            Row r = sh.createRow(1);
            r.createCell(0, CellType.STRING).setCellValue("EPC-TEST");
            r.createCell(1, CellType.STRING).setCellValue("测试流程");
            r.createCell(5, CellType.STRING).setCellValue("SC-001");
            r.createCell(6, CellType.STRING).setCellValue("SC-001");
            r.createCell(7, CellType.STRING).setCellValue(stepsJson);
            return toBytes(wb);
        }
    }

    private static byte[] createEpcSheetWithMissing(String missingField) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(ExcelEpcImportAdapter.SHEET_EPC);
            createHeader(sh);
            Row r = sh.createRow(1);
            r.createCell(0, CellType.STRING).setCellValue("id".equals(missingField) ? "" : "EPC-ERR");
            r.createCell(1, CellType.STRING).setCellValue("错误流程");
            r.createCell(5, CellType.STRING).setCellValue("SC-001");
            r.createCell(6, CellType.STRING).setCellValue("SC-001");
            r.createCell(7, CellType.STRING).setCellValue("steps".equals(missingField) ? "" : "[{\"id\":\"s1\"}]");
            return toBytes(wb);
        }
    }

    private static byte[] createEmptyEpcSheet() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            wb.createSheet(ExcelEpcImportAdapter.SHEET_EPC);
            createHeader(wb.getSheet(ExcelEpcImportAdapter.SHEET_EPC));
            return toBytes(wb);
        }
    }

    private static byte[] createSheetWithoutEpc() {
        try (Workbook wb = new XSSFWorkbook()) { wb.createSheet("A"); return toBytes(wb); }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    private static String buildStepsJson(int count) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(",");
            String dim = i == 0 ? "E3" : "E2";
            String eid = i == 0 ? "EVT-001" : "ACT-002";
            sb.append("{\"id\":\"s").append(i + 1).append("\",")
                    .append("\"name\":\"").append(i == 0 ? "接收订单" : "校验库存").append("\",")
                    .append("\"elementRef\":{\"dimension\":\"").append(dim).append("\",")
                    .append("\"elementId\":\"").append(eid).append("\",")
                    .append("\"versionPin\":\"latest_confirmed\"}}");
        }
        sb.append("]");
        return sb.toString();
    }

    private static void createHeader(Sheet sh) {
        Row h = sh.createRow(0);
        h.createCell(0, CellType.STRING).setCellValue("ID");
        h.createCell(1, CellType.STRING).setCellValue("名称");
        h.createCell(2, CellType.STRING).setCellValue("英文名");
        h.createCell(3, CellType.STRING).setCellValue("描述");
        h.createCell(4, CellType.STRING).setCellValue("语义(JSON)");
        h.createCell(5, CellType.STRING).setCellValue("父节点ID");
        h.createCell(6, CellType.STRING).setCellValue("归属场景ID");
        h.createCell(7, CellType.STRING).setCellValue("步骤(JSON)");
    }

    private static byte[] toBytes(Workbook wb) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        return bos.toByteArray();
    }

    private static InputStream toStream(byte[] data) { return new ByteArrayInputStream(data); }
}

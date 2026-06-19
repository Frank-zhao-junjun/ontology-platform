package com.ontology.platform.infrastructure.imports;

import com.ontology.platform.domain.dto.imports.ExcelEDimRow;
import com.ontology.platform.domain.dto.imports.ImportResult;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Excel E1~E8 维度要素通用导入适配器
 *
 * <p>读取项目1导出的 xlsx 文件中任意 E-dimension Sheet（E1~E8）的数据行，
 * 校验并映射为 ExcelEDimRow DTO。</p>
 *
 * <p>8 个维度的列结构完全相同：ID | 名称 | 英文名 | 维度 | 可见性 | 描述</p>
 */
@Component
@RequiredArgsConstructor
public class ExcelEDimImportAdapter {

    private static final int DATA_START_ROW = 1;

    private static final int COL_ID = 0;
    private static final int COL_NAME = 1;
    private static final int COL_NAME_EN = 2;
    private static final int COL_DIMENSION = 3;
    private static final int COL_VISIBILITY = 4;
    private static final int COL_DESC = 5;

    private static final Set<String> VALID_DIMENSIONS = Set.of(
            "E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8"
    );
    private static final Set<String> VALID_VISIBILITIES = Set.of(
            "project", "domain_scoped", "private_draft"
    );

    /**
     * 执行导入
     *
     * @param xlsxStream 项目1导出的 xlsx 文件流
     * @param sheetName  工作表名称（E1 ~ E8）
     */
    public ImportResult<ExcelEDimRow> execute(InputStream xlsxStream, String sheetName)
            throws IOException {

        ImportResult<ExcelEDimRow> result = new ImportResult<>();

        try (Workbook workbook = new XSSFWorkbook(xlsxStream)) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException(
                        "Excel 文件中未找到工作表 \"" + sheetName + "\"");
            }

            for (int rowIdx = DATA_START_ROW; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;

                ExcelEDimRow parsed = parseRow(row, rowIdx, result);
                if (parsed != null) {
                    result.addImported(parsed);
                }
            }
        }

        return result;
    }

    // ==================== 解析 ====================

    private ExcelEDimRow parseRow(Row row, int rowIdx, ImportResult<ExcelEDimRow> result) {
        String id = getCellString(row, COL_ID);
        String name = getCellString(row, COL_NAME);
        String dimension = getCellString(row, COL_DIMENSION);

        if (id == null || id.isBlank()) {
            result.addError(rowIdx + 1, "ID", "ID 为必填字段");
            return null;
        }
        if (name == null || name.isBlank()) {
            result.addError(rowIdx + 1, "名称", "名称为必填字段");
            return null;
        }
        if (dimension == null || dimension.isBlank()) {
            result.addError(rowIdx + 1, "维度", "维度为必填字段");
            return null;
        }
        if (!VALID_DIMENSIONS.contains(dimension.trim())) {
            result.addError(rowIdx + 1, "维度",
                    "维度值无效，必须为 E1~E8，实际值: " + dimension.trim());
            return null;
        }

        String visibility = getCellString(row, COL_VISIBILITY);
        String nameEn = getCellString(row, COL_NAME_EN);
        String description = getCellString(row, COL_DESC);
        if (visibility != null && !visibility.isBlank()
                && !VALID_VISIBILITIES.contains(visibility.trim())) {
            result.addError(rowIdx + 1, "可见性",
                    "可见性值无效，必须为 project/domain_scoped/private_draft，实际值: " + visibility.trim());
            return null;
        }

        return ExcelEDimRow.builder()
                .elementId(id.trim())
                .name(name.trim())
                .nameEn(nameEn != null ? nameEn.trim() : null)
                .dimension(dimension.trim())
                .visibility(visibility != null ? visibility.trim() : null)
                .description(description != null ? description.trim() : null)
                .build();
    }

    // ==================== 读取 ====================

    private static String getCellString(Row row, int colIdx) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                yield v == Math.floor(v) && !Double.isInfinite(v) ? String.valueOf((long) v) : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield cell.getStringCellValue(); }
                catch (Exception e) {
                    try { yield String.valueOf(cell.getNumericCellValue()); }
                    catch (Exception e2) { yield ""; }
                }
            }
            default -> null;
        };
    }
}

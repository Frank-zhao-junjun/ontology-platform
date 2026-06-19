package com.ontology.platform.infrastructure.imports;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.dto.imports.ImportResult;
import com.ontology.platform.domain.entity.ObjectType;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Predicate;

/**
 * Excel Sheet C（Scenario）导入适配器
 *
 * <p>读取项目1导出的 xlsx 文件中 Sheet C 的数据行，
 * 校验并映射为项目2的 ObjectType 实体。</p>
 *
 * <p>列定义：ID | 名称 | 英文名 | 描述 | 语义(JSON) | 父节点ID</p>
 */
@Component
@RequiredArgsConstructor
public class ExcelCImportAdapter {

    public static final String SHEET_C = "C";
    private static final int DATA_START_ROW = 1;

    private static final int COL_ID = 0;
    private static final int COL_NAME = 1;
    private static final int COL_NAME_EN = 2;
    private static final int COL_DESC = 3;
    private static final int COL_SEMANTICS = 4;
    private static final int COL_PARENT_ID = 5;

    private final ObjectMapper objectMapper;
    private Predicate<String> nameConflictChecker = name -> false;

    public ImportResult<ObjectType> execute(InputStream xlsxStream, String ontologyId, String createdBy)
            throws IOException {

        ImportResult<ObjectType> result = new ImportResult<>();

        try (Workbook workbook = new XSSFWorkbook(xlsxStream)) {
            Sheet sheet = workbook.getSheet(SHEET_C);
            if (sheet == null) {
                throw new IllegalArgumentException(
                        "Excel 文件中未找到工作表 \"" + SHEET_C + "\"");
            }

            for (int rowIdx = DATA_START_ROW; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;

                ParsedRow parsed = parseRow(row, rowIdx, result);
                if (parsed == null) continue;

                if (nameConflictChecker.test(parsed.id)) {
                    result.addSkipped(parsed.id,
                            "ObjectType name \"" + parsed.id + "\" already exists in ontology " + ontologyId);
                    continue;
                }

                ObjectType objectType = ObjectType.create(
                        ontologyId, parsed.id, parsed.name,
                        parsed.description != null ? parsed.description : "",
                        null);
                if (parsed.parentId != null) {
                    objectType.setParent(parsed.parentId);
                }
                result.addImported(objectType);
            }
        }
        return result;
    }

    public void setNameConflictChecker(Predicate<String> checker) {
        this.nameConflictChecker = checker != null ? checker : name -> false;
    }

    private record ParsedRow(String id, String name, String nameEn, String description, String semantics, String parentId) {}

    private ParsedRow parseRow(Row row, int rowIdx, ImportResult<ObjectType> result) {
        String id = getCellString(row, COL_ID);
        String name = getCellString(row, COL_NAME);
        String parentId = getCellString(row, COL_PARENT_ID);

        if (id == null || id.isBlank()) { result.addError(rowIdx + 1, "ID", "ID 为必填字段"); return null; }
        if (name == null || name.isBlank()) { result.addError(rowIdx + 1, "名称", "名称为必填字段"); return null; }
        if (parentId == null || parentId.isBlank()) { result.addError(rowIdx + 1, "父节点ID", "父节点ID 为必填字段"); return null; }

        String nameEn = getCellString(row, COL_NAME_EN);
        String description = getCellString(row, COL_DESC);
        String semantics = getCellString(row, COL_SEMANTICS);
        if (semantics != null && !semantics.isBlank()) {
            try { objectMapper.readTree(semantics); }
            catch (JsonProcessingException e) {
                result.addError(rowIdx + 1, "语义(JSON)", "JSON 格式错误: " + e.getMessage());
                return null;
            }
        }

        return new ParsedRow(id.trim(), name.trim(),
                nameEn != null ? nameEn.trim() : null,
                description != null ? description.trim() : null,
                semantics != null ? semantics.trim() : null,
                parentId.trim());
    }

    private static String getCellString(Row row, int colIdx) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> { double v = cell.getNumericCellValue(); yield v == Math.floor(v) && !Double.isInfinite(v) ? String.valueOf((long) v) : String.valueOf(v); }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> { try { yield cell.getStringCellValue(); } catch (Exception e) { try { yield String.valueOf(cell.getNumericCellValue()); } catch (Exception e2) { yield ""; } } }
            default -> null;
        };
    }
}

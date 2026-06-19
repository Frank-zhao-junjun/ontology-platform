package com.ontology.platform.infrastructure.imports;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.dto.imports.ExcelOntologyRow;
import com.ontology.platform.domain.dto.imports.ImportResult;
import com.ontology.platform.domain.entity.Ontology;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;

/**
 * Excel Sheet A（ValueDomain）导入适配器
 *
 * <p>读取项目1导出的 xlsx 文件中 Sheet A 的数据行，
 * 校验并映射为项目2的 Ontology 实体。</p>
 *
 * <p>不直接依赖 Spring Repository，通过 {@link #nameConflictChecker} 回调
 * 由调用方注入冲突检测逻辑，便于单元测试。</p>
 */
@Component
@RequiredArgsConstructor
public class ExcelOntologyImportAdapter {

    /** 期望的工作表名称 */
    public static final String SHEET_A = "A";

    /** 数据起始行索引（0-based）— 第0行为标题行 */
    private static final int DATA_START_ROW = 1;

    /** 列索引映射（0-based） */
    private static final int COL_ID = 0;
    private static final int COL_NAME = 1;
    private static final int COL_NAME_EN = 2;
    private static final int COL_DESC = 3;
    private static final int COL_SEMANTICS = 4;

    private final ObjectMapper objectMapper;

    /**
     * 名称冲突检测器 — 由调用方注入
     *
     * <p>test(name) → true 表示该 name 已存在（冲突）</p>
     */
    private Predicate<String> nameConflictChecker = name -> false;

    /**
     * 执行导入
     *
     * @param xlsxStream 项目1导出的 xlsx 文件流
     * @param createdBy  导入操作的用户
     * @return 导入结果
     * @throws IOException 文件读取/解析异常
     * @throws IllegalArgumentException Sheet "A" 不存在
     */
    public ImportResult<Ontology> execute(InputStream xlsxStream, String createdBy)
            throws IOException {

        ImportResult<Ontology> result = new ImportResult<>();

        try (Workbook workbook = new XSSFWorkbook(xlsxStream)) {
            Sheet sheet = workbook.getSheet(SHEET_A);
            if (sheet == null) {
                throw new IllegalArgumentException(
                        "Excel 文件中未找到工作表 \"" + SHEET_A + "\"");
            }

            for (int rowIdx = DATA_START_ROW; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;

                // 解析行
                ExcelOntologyRow parsed = parseRow(row, rowIdx, result);
                if (parsed == null) continue;

                // 冲突检测
                if (nameConflictChecker.test(parsed.getId())) {
                    result.addSkipped(parsed.getId(),
                            "Ontology name \"" + parsed.getId() + "\" already exists");
                    continue;
                }

                // 映射为 Ontology 实体
                Ontology ontology = Ontology.create(
                        parsed.getId(),
                        parsed.getName(),
                        parsed.getDescription(),
                        createdBy
                );

                // 可选字段
                if (parsed.getNameEn() != null && !parsed.getNameEn().isBlank()) {
                    ontology.setDisplayName(parsed.getName() + " (" + parsed.getNameEn() + ")");
                }

                result.addImported(ontology);
            }
        }

        return result;
    }

    /**
     * 设置名称冲突检测器
     */
    public void setNameConflictChecker(Predicate<String> checker) {
        this.nameConflictChecker = checker != null ? checker : name -> false;
    }

    // ==================== 私有方法 ====================

    /**
     * 解析单行数据
     *
     * @return 解析成功的行，或 null（该行有错误已记录到 result）
     */
    private ExcelOntologyRow parseRow(Row row, int rowIdx, ImportResult<Ontology> result) {
        String id = getCellString(row, COL_ID);
        String name = getCellString(row, COL_NAME);

        // 必填字段校验
        if (id == null || id.isBlank()) {
            result.addError(rowIdx + 1, "ID", "ID 为必填字段");
            return null;
        }
        if (name == null || name.isBlank()) {
            result.addError(rowIdx + 1, "名称", "名称为必填字段");
            return null;
        }

        String nameEn = getCellString(row, COL_NAME_EN);
        String description = getCellString(row, COL_DESC);
        String semantics = getCellString(row, COL_SEMANTICS);

        // 语义 JSON 格式校验
        if (semantics != null && !semantics.isBlank()) {
            try {
                objectMapper.readTree(semantics);
            } catch (JsonProcessingException e) {
                result.addError(rowIdx + 1, "语义(JSON)", "语义(JSON) JSON 格式错误: " + e.getMessage());
                return null;
            }
        }

        return ExcelOntologyRow.builder()
                .id(id.trim())
                .name(name.trim())
                .nameEn(nameEn != null ? nameEn.trim() : null)
                .description(description != null ? description.trim() : null)
                .semantics(semantics != null ? semantics.trim() : null)
                .build();
    }

    /**
     * 安全获取单元格字符串值
     */
    private static String getCellString(Row row, int colIdx) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                yield val == Math.floor(val) && !Double.isInfinite(val)
                        ? String.valueOf((long) val)
                        : String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        yield String.valueOf(cell.getNumericCellValue());
                    } catch (Exception e2) {
                        yield "";
                    }
                }
            }
            default -> null;
        };
    }
}

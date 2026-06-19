package com.ontology.platform.infrastructure.imports;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.dto.imports.EpcParsedRow;
import com.ontology.platform.domain.dto.imports.EpcStepItem;
import com.ontology.platform.domain.dto.imports.ImportResult;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Excel Sheet EPC（EpcProcess）导入适配器
 *
 * <p>读取项目1导出的 xlsx 文件中 Sheet EPC 的数据行，
 * 解析流程步骤 JSON，返回结构化 EpcParsedRow 列表。</p>
 *
 * <p>列定义（来自项目1 excel-schema.ts）：
 * <pre>
 * | ID | 名称 | 英文名 | 描述 | 语义(JSON) | 父节点ID | 归属场景ID | 步骤(JSON) |
 * </pre>
 * </p>
 *
 * <p>步骤 JSON 格式示例：
 * <pre>
 * [{"id":"s1","name":"下达","elementRef":{"dimension":"E2","elementId":"ACT-001","versionPin":"latest_confirmed"}}]
 * </pre>
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ExcelEpcImportAdapter {

    public static final String SHEET_EPC = "EPC";
    private static final int DATA_START_ROW = 1;

    private static final int COL_ID = 0;
    private static final int COL_NAME = 1;
    private static final int COL_NAME_EN = 2;
    private static final int COL_DESC = 3;
    private static final int COL_SEMANTICS = 4;
    private static final int COL_PARENT_ID = 5;
    private static final int COL_SCENARIO_ID = 6;
    private static final int COL_STEPS = 7;

    private final ObjectMapper objectMapper;

    /**
     * 执行导入解析
     *
     * @param xlsxStream 项目1导出的 xlsx 文件流
     * @return 解析结果（不含持久化）
     */
    public ImportResult<EpcParsedRow> execute(InputStream xlsxStream) throws IOException {
        ImportResult<EpcParsedRow> result = new ImportResult<>();

        try (Workbook workbook = new XSSFWorkbook(xlsxStream)) {
            Sheet sheet = workbook.getSheet(SHEET_EPC);
            if (sheet == null) {
                throw new IllegalArgumentException(
                        "Excel 文件中未找到工作表 \"" + SHEET_EPC + "\"");
            }

            for (int rowIdx = DATA_START_ROW; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;

                EpcParsedRow parsed = parseRow(row, rowIdx, result);
                if (parsed != null) {
                    result.addImported(parsed);
                }
            }
        }

        return result;
    }

    // ==================== 解析 ====================

    private EpcParsedRow parseRow(Row row, int rowIdx, ImportResult<EpcParsedRow> result) {
        String id = getCellString(row, COL_ID);
        String name = getCellString(row, COL_NAME);
        String parentId = getCellString(row, COL_PARENT_ID);
        String scenarioId = getCellString(row, COL_SCENARIO_ID);
        String stepsRaw = getCellString(row, COL_STEPS);

        // 必填字段校验
        if (id == null || id.isBlank()) {
            result.addError(rowIdx + 1, "ID", "ID 为必填字段");
            return null;
        }
        if (name == null || name.isBlank()) {
            result.addError(rowIdx + 1, "名称", "名称为必填字段");
            return null;
        }
        if (parentId == null || parentId.isBlank()) {
            result.addError(rowIdx + 1, "父节点ID", "父节点ID 为必填字段");
            return null;
        }
        if (scenarioId == null || scenarioId.isBlank()) {
            result.addError(rowIdx + 1, "归属场景ID", "归属场景ID 为必填字段");
            return null;
        }
        if (stepsRaw == null || stepsRaw.isBlank()) {
            result.addError(rowIdx + 1, "步骤(JSON)", "步骤(JSON) 为必填字段");
            return null;
        }

        // 解析步骤 JSON
        List<EpcStepItem> steps = parseSteps(stepsRaw, rowIdx, result);
        if (steps == null) return null;

        // 可选字段
        String description = getCellString(row, COL_DESC);
        String semantics = getCellString(row, COL_SEMANTICS);
        if (semantics != null && !semantics.isBlank()) {
            try {
                objectMapper.readTree(semantics);
            } catch (JsonProcessingException e) {
                result.addError(rowIdx + 1, "语义(JSON)", "JSON 格式错误: " + e.getMessage());
                return null;
            }
        }

        return EpcParsedRow.builder()
                .epcId(id.trim())
                .flowName(name.trim())
                .description(description != null ? description.trim() : null)
                .parentId(parentId.trim())
                .scenarioId(scenarioId.trim())
                .steps(steps)
                .build();
    }

    /**
     * 解析步骤 JSON 数组
     *
     * <p>预期格式：
     * <pre>
     * [{"id":"s1","name":"下达","elementRef":{"dimension":"E2","elementId":"ACT-001","versionPin":"latest_confirmed"}}]
     * </pre>
     * </p>
     */
    private List<EpcStepItem> parseSteps(String json, int rowIdx, ImportResult<EpcParsedRow> result) {
        List<JsonNode> stepNodes;
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!root.isArray()) {
                result.addError(rowIdx + 1, "步骤(JSON)", "步骤数据必须是 JSON 数组");
                return null;
            }
            stepNodes = new ArrayList<>();
            for (JsonNode node : root) {
                stepNodes.add(node);
            }
        } catch (JsonProcessingException e) {
            result.addError(rowIdx + 1, "步骤(JSON)", "JSON 解析失败: " + e.getMessage());
            return null;
        }

        List<EpcStepItem> steps = new ArrayList<>();
        for (int i = 0; i < stepNodes.size(); i++) {
            JsonNode node = stepNodes.get(i);

            // 每步必须含 id
            JsonNode idNode = node.get("id");
            if (idNode == null || idNode.isNull() || idNode.asText().isBlank()) {
                result.addError(rowIdx + 1, "步骤(JSON)",
                        "步骤 #" + (i + 1) + " 缺少 id 字段");
                return null;
            }

            String stepId = idNode.asText();
            String stepName = node.has("name") ? node.get("name").asText() : "";
            String dimension = null;
            String elementId = null;
            String versionPin = null;

            JsonNode ref = node.get("elementRef");
            if (ref != null && !ref.isNull()) {
                dimension = ref.has("dimension") && !ref.get("dimension").isNull() ? ref.get("dimension").asText() : null;
                elementId = ref.has("elementId") && !ref.get("elementId").isNull() ? ref.get("elementId").asText() : null;
                versionPin = ref.has("versionPin") && !ref.get("versionPin").isNull() ? ref.get("versionPin").asText() : null;
            }

            steps.add(EpcStepItem.builder()
                    .stepId(stepId)
                    .stepName(stepName)
                    .dimension(dimension)
                    .elementId(elementId)
                    .versionPin(versionPin)
                    .stepOrder(i)
                    .build());
        }

        if (steps.isEmpty()) {
            result.addError(rowIdx + 1, "步骤(JSON)", "步骤数组不能为空");
            return null;
        }

        return steps;
    }

    // ==================== 单元格读取 ====================

    private static String getCellString(Row row, int colIdx) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                yield v == Math.floor(v) && !Double.isInfinite(v)
                        ? String.valueOf((long) v) : String.valueOf(v);
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

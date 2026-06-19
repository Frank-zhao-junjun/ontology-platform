package com.ontology.platform.domain.dto.imports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Excel Sheet A（ValueDomain）行数据 DTO
 *
 * <p>映射项目1 Excel 导出的 Sheet A 列定义：
 * <pre>
 * | ID | 名称 | 英文名 | 描述 | 语义(JSON) |
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelOntologyRow {

    /** ValueDomain 唯一标识（必填）→ Ontology.name */
    private String id;

    /** 中文名称（必填）→ Ontology.displayName */
    private String name;

    /** 英文名称（可选） */
    private String nameEn;

    /** 业务描述（可选）→ Ontology.description */
    private String description;

    /** 语义块 JSON 字符串（可选） */
    private String semantics;
}

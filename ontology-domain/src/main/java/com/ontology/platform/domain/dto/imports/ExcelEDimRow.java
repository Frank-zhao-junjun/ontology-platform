package com.ontology.platform.domain.dto.imports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * E1~E8 维度要素行 DTO
 *
 * <p>对应项目1 Excel Sheet E1~E8 的数据行。
 * 8 个维度列结构完全相同：ID | 名称 | 英文名 | 维度 | 可见性 | 描述</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelEDimRow {

    private String elementId;
    private String name;
    private String nameEn;
    private String dimension;
    private String visibility;
    private String description;
}

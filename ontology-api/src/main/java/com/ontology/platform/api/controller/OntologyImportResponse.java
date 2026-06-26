package com.ontology.platform.api.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OntologyImportResponse {
    /** 导入记录 ID（等同于 draftId，兼容旧字段） */
    private String draftId;
    private String externalId;
    /** 校验状态: passed / failed / published */
    private String status;
    /** 领域模型实体总数 */
    private Integer totalEntities;
    /** 校验警告数 */
    private Integer warnings;
    /** 各类模型计数（向后兼容） */
    private Map<String, Integer> importedCounts;
}

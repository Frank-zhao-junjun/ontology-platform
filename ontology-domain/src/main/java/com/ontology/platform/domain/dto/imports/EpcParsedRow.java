package com.ontology.platform.domain.dto.imports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * EPC 解析行 DTO
 *
 * <p>对应项目1 Excel Sheet EPC 的一行数据，
 * 包含流程基本信息及其步骤列表。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpcParsedRow {

    /** EPC 流程 ID（如 EPC-001） */
    private String epcId;

    /** 流程名称 */
    private String flowName;

    /** 描述 */
    private String description;

    /** 父节点 ID（指向 C 节点的 ID） */
    private String parentId;

    /** 归属场景 ID */
    private String scenarioId;

    /** 步骤列表 */
    private List<EpcStepItem> steps;
}

package com.ontology.platform.domain.dto.imports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * EPC 步骤项 DTO
 *
 * <p>对应项目1 EpcStep 中的每一步，
 * 包含步骤 ID、名称、要素引用信息。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpcStepItem {

    /** 步骤内部 ID */
    private String stepId;

    /** 步骤名称 */
    private String stepName;

    /** 引用的要素维度（E1-E8） */
    private String dimension;

    /** 引用的要素 ID */
    private String elementId;

    /** 版本锁定方式 */
    private String versionPin;

    /** 步骤序号（从 0 开始） */
    private int stepOrder;
}

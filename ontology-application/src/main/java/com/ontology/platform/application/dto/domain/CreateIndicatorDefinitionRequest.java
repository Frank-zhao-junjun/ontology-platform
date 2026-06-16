package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建IndicatorDefinition请求")
public class CreateIndicatorDefinitionRequest {
    @Schema(description = "指标名称")
    private String indicatorName;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "公式")
    private String formula;
    @Schema(description = "目标值")
    private String targetValue;
    @Schema(description = "单位")
    private String unit;
    @Schema(description = "警告阈值")
    private String warningThreshold;
    @Schema(description = "严重阈值")
    private String criticalThreshold;
    @Schema(description = "聚合类型")
    private String aggregationType;
    @Schema(description = "频率(秒)")
    private Integer frequencySec;
    @Schema(description = "是否启用")
    private Boolean enabled;
    @Schema(description = "扩展数据")
    private String extendedData;
}

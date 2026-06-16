package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("indicator_definition")
public class IndicatorDefinitionPO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("ontology_id")
    private String ontologyId;
    @TableField("indicator_name")
    private String indicatorName;
    @TableField("description")
    private String description;
    @TableField("formula")
    private String formula;
    @TableField("target_value")
    private String targetValue;
    @TableField("unit")
    private String unit;
    @TableField("warning_threshold")
    private String warningThreshold;
    @TableField("critical_threshold")
    private String criticalThreshold;
    @TableField("aggregation_type")
    private String aggregationType;
    @TableField("frequency_sec")
    private Integer frequencySec;
    @TableField("enabled")
    private Boolean enabled;
    @TableField("extended_data")
    private String extendedData;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;

    @TableField("deleted")
    private Boolean deleted;
}

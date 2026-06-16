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
@TableName("probe_definition")
public class ProbeDefinitionPO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("ontology_id")
    private String ontologyId;
    @TableField("probe_name")
    private String probeName;
    @TableField("description")
    private String description;
    @TableField("target")
    private String target;
    @TableField("probe_type")
    private String probeType;
    @TableField("frequency_sec")
    private Integer frequencySec;
    @TableField("timeout_ms")
    private Integer timeoutMs;
    @TableField("alert_condition")
    private String alertCondition;
    @TableField("alert_severity")
    private String alertSeverity;
    @TableField("enabled")
    private Boolean enabled;
    @TableField("config")
    private String config;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;

    @TableField("deleted")
    private Boolean deleted;
}

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
@TableName("compute_definition")
public class ComputeDefinitionPO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("ontology_id")
    private String ontologyId;
    @TableField("compute_name")
    private String computeName;
    @TableField("description")
    private String description;
    @TableField("input_schema")
    private String inputSchema;
    @TableField("formula")
    private String formula;
    @TableField("output_type")
    private String outputType;
    @TableField("output_schema")
    private String outputSchema;
    @TableField("timeout_ms")
    private Integer timeoutMs;
    @TableField("enabled")
    private Boolean enabled;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;

    @TableField("deleted")
    private Boolean deleted;
}

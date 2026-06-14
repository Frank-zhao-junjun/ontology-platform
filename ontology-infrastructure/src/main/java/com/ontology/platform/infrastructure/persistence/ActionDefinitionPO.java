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
@TableName("action_definition")
public class ActionDefinitionPO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("ontology_id")
    private String ontologyId;

    @TableField("entity_id")
    private String entityId;

    @TableField("name")
    private String name;

    @TableField("display_name")
    private String displayName;

    @TableField("description")
    private String description;

    @TableField("action_type")
    private String actionType;

    @TableField("input_schema")
    private String inputSchema;

    @TableField("output_schema")
    private String outputSchema;

    @TableField("pre_rules")
    private String preRules;

    @TableField("post_rules")
    private String postRules;

    @TableField("domain")
    private String domain;

    @TableField("risk_level")
    private String riskLevel;

    @TableField("is_async")
    private Boolean isAsync;

    @TableField("timeout_ms")
    private Integer timeoutMs;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;

    @TableField("deleted")
    private Boolean deleted;
}

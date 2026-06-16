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
@TableName("guardrail_rule")
public class GuardrailRulePO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("ontology_id")
    private String ontologyId;
    @TableField("rule_name")
    private String ruleName;
    @TableField("description")
    private String description;
    @TableField("condition_expr")
    private String conditionExpr;
    @TableField("action_type")
    private String actionType;
    @TableField("action_config")
    private String actionConfig;
    @TableField("priority")
    private Integer priority;
    @TableField("enabled")
    private Boolean enabled;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;

    @TableField("deleted")
    private Boolean deleted;
}

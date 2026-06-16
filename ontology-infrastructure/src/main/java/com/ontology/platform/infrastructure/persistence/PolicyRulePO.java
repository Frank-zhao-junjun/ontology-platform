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
@TableName("policy_rule")
public class PolicyRulePO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("ontology_id")
    private String ontologyId;
    @TableField("policy_name")
    private String policyName;
    @TableField("description")
    private String description;
    @TableField("policy_type")
    private String policyType;
    @TableField("rules")
    private String rules;
    @TableField("effect")
    private String effect;
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

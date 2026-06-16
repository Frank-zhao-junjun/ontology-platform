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
@TableName("validation_rule")
public class ValidationRulePO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("ontology_id")
    private String ontologyId;
    @TableField("entity_id")
    private String entityId;
    @TableField("field_name")
    private String fieldName;
    @TableField("rule_type")
    private String ruleType;
    @TableField("rule_name")
    private String ruleName;
    @TableField("description")
    private String description;
    @TableField("severity")
    private String severity;
    @TableField("expression")
    private String expression;
    @TableField("error_message")
    private String errorMessage;
    @TableField("enabled")
    private Boolean enabled;
    @TableField("sort_order")
    private Integer sortOrder;
    @TableField("extended_data")
    private String extendedData;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;

    @TableField("deleted")
    private Boolean deleted;
}

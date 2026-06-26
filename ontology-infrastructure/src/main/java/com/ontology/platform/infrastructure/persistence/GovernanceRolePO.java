package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@TableName("governance_role")
public class GovernanceRolePO {
    @TableId private String id;
    @TableField("ontology_id") private String ontologyId;
    @TableField("name") private String name;
    @TableField("description") private String description;
    @TableField("permissions") private String permissions;
    @TableField("created_at") private Instant createdAt;
    @TableField("updated_at") private Instant updatedAt;
}

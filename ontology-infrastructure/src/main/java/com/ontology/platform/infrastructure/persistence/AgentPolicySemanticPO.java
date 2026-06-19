package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@TableName("agent_policy_semantic")
public class AgentPolicySemanticPO {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    @TableField("name")
    private String name;
    @TableField("role_id")
    private String roleId;
    @TableField("intent_patterns")
    private String intentPatterns;
    @TableField("allow_actions")
    private String allowActions;
    @TableField("deny_actions")
    private String denyActions;
    @TableField("require_confirm")
    private String requireConfirm;
    @TableField("is_active")
    private Boolean isActive;
    @TableField("created_at")
    private Instant createdAt;
    @TableField("updated_at")
    private Instant updatedAt;
}

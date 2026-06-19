package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@TableName("error_recovery")
public class ErrorRecoveryPO {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    @TableField("action_id")
    private String actionId;
    @TableField("error_pattern")
    private String errorPattern;
    @TableField("recovery_strategy")
    private String recoveryStrategy;
    @TableField("max_retries")
    private Integer maxRetries;
    @TableField("fallback_action_id")
    private String fallbackActionId;
    @TableField("description")
    private String description;
    @TableField("created_at")
    private Instant createdAt;

}

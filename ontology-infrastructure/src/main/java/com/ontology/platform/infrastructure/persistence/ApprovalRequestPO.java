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
@TableName("approval_request")
public class ApprovalRequestPO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("agent_id")
    private String agentId;

    @TableField("action_id")
    private String actionId;

    @TableField("requested_op")
    private String requestedOp;

    @TableField("status")
    private String status;

    @TableField("reason")
    private String reason;

    @TableField("requested_at")
    private Instant requestedAt;

    @TableField("resolved_at")
    private Instant resolvedAt;

    @TableField("resolved_by")
    private String resolvedBy;
}

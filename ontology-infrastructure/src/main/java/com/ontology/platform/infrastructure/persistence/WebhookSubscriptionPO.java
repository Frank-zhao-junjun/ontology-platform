package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("webhook_subscription")
public class WebhookSubscriptionPO {

    @TableId(type = IdType.INPUT)
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @TableField("tenant_id")
    private String tenantId;

    @TableField("agent_id")
    private String agentId;

    @TableField("callback_url")
    private String callbackUrl;

    @TableField("event_types")
    private String eventTypes; // JSONB stored as String, default '["job.completed","job.failed"]'

    @TableField("secret")
    private String secret;

    @TableField("is_active")
    @Builder.Default
    private Boolean isActive = true;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;
}

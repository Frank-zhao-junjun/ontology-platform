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
@TableName("rate_limit_config")
public class RateLimitConfigPO {

    @TableId(type = IdType.INPUT)
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @TableField("scope_type")
    private String scopeType; // AGENT, TOOL, TENANT

    @TableField("scope_value")
    private String scopeValue;

    @TableField("window_seconds")
    @Builder.Default
    private Integer windowSeconds = 60;

    @TableField("max_requests")
    @Builder.Default
    private Integer maxRequests = 100;

    @TableField("burst_size")
    @Builder.Default
    private Integer burstSize = 20;

    @TableField("is_active")
    @Builder.Default
    private Boolean isActive = true;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;
}

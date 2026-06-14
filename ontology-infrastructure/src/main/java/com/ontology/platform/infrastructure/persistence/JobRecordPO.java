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
@TableName("job_record")
public class JobRecordPO {

    @TableId(type = IdType.INPUT)
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @TableField("job_type")
    private String jobType;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("agent_id")
    private String agentId;

    @TableField("idempotency_key")
    private String idempotencyKey;

    @TableField("status")
    @Builder.Default
    private String status = "QUEUED";

    @TableField("payload")
    private String payload; // JSONB stored as String

    @TableField("result")
    private String result; // JSONB stored as String

    @TableField("error_message")
    private String errorMessage;

    @TableField("retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @TableField("max_retries")
    @Builder.Default
    private Integer maxRetries = 3;

    @TableField("next_retry_at")
    private Instant nextRetryAt;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("started_at")
    private Instant startedAt;

    @TableField("completed_at")
    private Instant completedAt;
}

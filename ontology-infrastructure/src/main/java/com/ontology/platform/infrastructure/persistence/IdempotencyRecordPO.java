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
@TableName("idempotency_record")
public class IdempotencyRecordPO {

    @TableId(type = IdType.INPUT)
    private String idempotencyKey;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("agent_id")
    private String agentId;

    @TableField("http_method")
    private String httpMethod;

    @TableField("request_path")
    private String requestPath;

    @TableField("response_status")
    private Integer responseStatus;

    @TableField("response_body")
    private String responseBody;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("expires_at")
    private Instant expiresAt;
}

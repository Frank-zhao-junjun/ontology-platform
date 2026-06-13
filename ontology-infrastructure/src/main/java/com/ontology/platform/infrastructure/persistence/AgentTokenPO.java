package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ontology.platform.common.enums.TokenStatus;
import lombok.*;

import java.time.Instant;

/**
 * Agent 令牌持久化对象
 * Agent Token Persistence Object
 * 对应表：agent_token
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("agent_token")
public class AgentTokenPO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 代理ID
     */
    @TableField("agent_id")
    private String agentId;

    /**
     * 令牌哈希（BCrypt 哈希值）
     */
    @TableField("token_hash")
    private String tokenHash;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private String tenantId;

    /**
     * 显示名称
     */
    @TableField("display_name")
    private String displayName;

    /**
     * 状态：ACTIVE / SUSPENDED / REVOKED
     */
    @TableField("status")
    private String status;

    /**
     * 签发时间
     */
    @TableField("issued_at")
    private Instant issuedAt;

    /**
     * 过期时间
     */
    @TableField("expires_at")
    private Instant expiresAt;

    /**
     * 最后使用时间
     */
    @TableField("last_used_at")
    private Instant lastUsedAt;

    /**
     * 创建人
     */
    @TableField("created_by")
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private Instant createdAt;

    /**
     * 获取状态枚举
     */
    public TokenStatus getStatusEnum() {
        if (status == null) {
            return null;
        }
        return TokenStatus.valueOf(status);
    }

    /**
     * 设置状态枚举
     */
    public void setStatusEnum(TokenStatus statusEnum) {
        this.status = statusEnum != null ? statusEnum.name() : null;
    }
}

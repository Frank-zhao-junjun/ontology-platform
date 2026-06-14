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
@TableName("agent_role")
public class AgentRolePO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("token_id")
    private String tokenId;

    @TableField("domain")
    private String domain;

    @TableField("role")
    private String role;

    @TableField("granted_at")
    private Instant grantedAt;
}

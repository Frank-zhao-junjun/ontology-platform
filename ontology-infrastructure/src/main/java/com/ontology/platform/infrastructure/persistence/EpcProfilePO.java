package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@TableName("epc_profile")
public class EpcProfilePO {
    @TableId(type = IdType.INPUT)
    private String id;
    @TableField("chain_id")
    private String chainId;
    @TableField("profile_data")
    private String profileData;
    @TableField("profile_version")
    private String profileVersion;
    @TableField("is_active")
    private Boolean isActive;
    @TableField("created_at")
    private Instant createdAt;
    @TableField("updated_at")
    private Instant updatedAt;
}

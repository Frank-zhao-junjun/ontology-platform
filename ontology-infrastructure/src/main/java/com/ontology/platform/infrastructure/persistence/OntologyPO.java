package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import com.ontology.platform.common.enums.OntologyStatus;
import lombok.*;

import java.time.Instant;

/**
 * 本体持久化对象
 * Ontology Persistence Object
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ontology")
public class OntologyPO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private String tenantId;

    /**
     * 本体名称（唯一标识）
     */
    @TableField("name")
    private String name;

    /**
     * 显示名称
     */
    @TableField("display_name")
    private String displayName;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 版本号
     */
    @TableField("version")
    private String version;

    /**
     * 状态：draft/published/archived
     */
    @TableField("status")
    private String status;

    /**
     * 发布时间
     */
    @TableField("published_at")
    private Instant publishedAt;

    /**
     * 对象类型数量
     */
    @TableField("object_type_count")
    private Integer objectTypeCount;

    /**
     * 动作类型数量
     */
    @TableField("action_type_count")
    private Integer actionTypeCount;

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
     * 更新时间
     */
    @TableField("updated_at")
    private Instant updatedAt;

    /**
     * 获取枚举类型的状态
     */
    public OntologyStatus getStatusEnum() {
        return OntologyStatus.fromValue(this.status);
    }

    /**
     * 设置枚举类型的状态
     */
    public void setStatusEnum(OntologyStatus statusEnum) {
        this.status = statusEnum.getValue();
    }
}

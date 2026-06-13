package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ontology.platform.common.enums.RelationCardinality;
import lombok.*;

import java.time.Instant;

/**
 * 关系定义持久化对象
 * Relation Persistence Object
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("relation_definition")
public class RelationPO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 所属本体ID
     */
    @TableField("ontology_id")
    private String ontologyId;

    /**
     * 源对象类型ID
     */
    @TableField("source_type_id")
    private String sourceTypeId;

    /**
     * 目标对象类型ID
     */
    @TableField("target_type_id")
    private String targetTypeId;

    /**
     * 关系名（在同一本体下唯一）
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
     * 基数：1:1 / 1:N / N:1 / M:N
     */
    @TableField("cardinality")
    private String cardinality;

    /**
     * 反向关系名
     */
    @TableField("reverse_name")
    private String reverseName;

    /**
     * 反向关系显示名称
     */
    @TableField("reverse_display_name")
    private String reverseDisplayName;

    /**
     * 扩展数据（JSON）
     */
    @TableField("extended_data")
    private String extendedData;

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
     * 获取基数枚举
     */
    public RelationCardinality getCardinalityEnum() {
        if (cardinality == null) {
            return null;
        }
        return RelationCardinality.fromValue(cardinality);
    }

    /**
     * 设置基数枚举
     */
    public void setCardinalityEnum(RelationCardinality cardinalityEnum) {
        this.cardinality = cardinalityEnum != null ? cardinalityEnum.getValue() : null;
    }
}

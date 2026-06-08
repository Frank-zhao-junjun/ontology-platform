package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * 对象类型持久化对象
 * ObjectType Persistence Object
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("object_type")
public class ObjectTypePO {

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
     * 类型名称
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
     * 主键属性名
     */
    @TableField("primary_key")
    private String primaryKey;

    /**
     * 父类型ID
     */
    @TableField("parent_id")
    private String parentId;

    /**
     * 实例数量
     */
    @TableField("instance_count")
    private Integer instanceCount;

    /**
     * 接口名称列表（JSON存储）
     */
    @TableField("interface_names")
    private String interfaceNames;

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
     * 获取接口名称列表
     */
    public List<String> getInterfaceNamesList() {
        if (interfaceNames == null || interfaceNames.isEmpty()) {
            return List.of();
        }
        try {
            return ObjectMapperHolder.MAPPER.readValue(
                    interfaceNames,
                    ObjectMapperHolder.LIST_TYPE_REFERENCE
            );
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 设置接口名称列表
     */
    public void setInterfaceNamesList(List<String> names) {
        if (names == null || names.isEmpty()) {
            this.interfaceNames = "[]";
            return;
        }
        try {
            this.interfaceNames = ObjectMapperHolder.MAPPER.writeValueAsString(names);
        } catch (Exception e) {
            this.interfaceNames = "[]";
        }
    }
}

/**
 * Jackson ObjectMapper持有器
 */
class ObjectMapperHolder {
    static final com.fasterxml.jackson.databind.ObjectMapper MAPPER;
    static final com.fasterxml.jackson.core.type.TypeReference<List<String>> LIST_TYPE_REFERENCE;
    
    static {
        MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();
        MAPPER.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        LIST_TYPE_REFERENCE = new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {};
    }
}

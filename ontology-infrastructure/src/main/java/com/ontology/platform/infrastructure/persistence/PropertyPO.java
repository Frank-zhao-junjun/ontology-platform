package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import com.ontology.platform.common.enums.PropertyDataType;
import lombok.*;

import java.time.Instant;
import java.util.Map;

/**
 * 属性定义持久化对象
 * Property Persistence Object
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("property_definition")
public class PropertyPO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 所属对象类型ID
     */
    @TableField("object_type_id")
    private String objectTypeId;

    /**
     * 属性名
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
     * 数据类型
     */
    @TableField("data_type")
    private String dataType;

    /**
     * 是否计算属性
     */
    @TableField("is_computed")
    private Boolean isComputed;

    /**
     * 是否必填
     */
    @TableField("is_required")
    private Boolean isRequired;

    /**
     * 是否唯一
     */
    @TableField("is_unique")
    private Boolean isUnique;

    /**
     * 是否可搜索
     */
    @TableField("is_searchable")
    private Boolean isSearchable;

    /**
     * 是否可排序
     */
    @TableField("is_sortable")
    private Boolean isSortable;

    /**
     * 默认值（JSON存储）
     */
    @TableField("default_value")
    private String defaultValue;

    /**
     * 排序顺序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 扩展数据（JSON存储，包括约束和嵌套属性）
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
     * 获取数据类型枚举
     */
    public PropertyDataType getDataTypeEnum() {
        if (dataType == null) {
            return null;
        }
        return PropertyDataType.fromValue(dataType);
    }

    /**
     * 设置数据类型枚举
     */
    public void setDataTypeEnum(PropertyDataType dataTypeEnum) {
        this.dataType = dataTypeEnum != null ? dataTypeEnum.getValue() : null;
    }

    /**
     * 获取默认值的对象形式
     */
    public Object getDefaultValueAsObject() {
        if (defaultValue == null || defaultValue.isEmpty()) {
            return null;
        }
        try {
            return PropertyMapperHolder.MAPPER.readValue(defaultValue, Object.class);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 设置默认值对象
     */
    public void setDefaultValueObject(Object value) {
        if (value == null) {
            this.defaultValue = null;
            return;
        }
        try {
            this.defaultValue = PropertyMapperHolder.MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            this.defaultValue = value.toString();
        }
    }

    /**
     * 获取扩展数据
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getExtendedDataMap() {
        if (extendedData == null || extendedData.isEmpty()) {
            return Map.of();
        }
        try {
            return PropertyMapperHolder.MAPPER.readValue(extendedData, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    /**
     * 设置扩展数据
     */
    public void setExtendedDataMap(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            this.extendedData = "{}";
            return;
        }
        try {
            this.extendedData = PropertyMapperHolder.MAPPER.writeValueAsString(data);
        } catch (Exception e) {
            this.extendedData = "{}";
        }
    }
}

/**
 * JSON映射器持有器
 */
class PropertyMapperHolder {
    static final com.fasterxml.jackson.databind.ObjectMapper MAPPER;
    
    static {
        MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();
        MAPPER.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        MAPPER.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
}

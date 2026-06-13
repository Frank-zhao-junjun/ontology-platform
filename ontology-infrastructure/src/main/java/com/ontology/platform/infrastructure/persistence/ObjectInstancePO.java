package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.Instant;
import java.util.Map;

/**
 * 对象实例持久化对象
 * Object Instance Persistence Object
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("object_instance")
public class ObjectInstancePO {

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
     * 所属对象类型ID
     */
    @TableField("object_type_id")
    private String objectTypeId;

    /**
     * 业务主键值
     */
    @TableField("primary_key_value")
    private String primaryKeyValue;

    /**
     * 核心数据（JSON 字符串）
     */
    @TableField("core_data")
    private String coreData;

    /**
     * 扩展数据（JSON 字符串）
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
     * 获取核心数据 Map 形式
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCoreDataMap() {
        if (coreData == null || coreData.isEmpty()) {
            return new java.util.HashMap<>();
        }
        try {
            return ObjectInstanceMapperHolder.MAPPER.readValue(coreData, Map.class);
        } catch (Exception e) {
            return new java.util.HashMap<>();
        }
    }

    /**
     * 设置核心数据 Map
     */
    public void setCoreDataMap(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            this.coreData = "{}";
            return;
        }
        try {
            this.coreData = ObjectInstanceMapperHolder.MAPPER.writeValueAsString(data);
        } catch (Exception e) {
            this.coreData = "{}";
        }
    }

    /**
     * 获取扩展数据 Map 形式
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getExtendedDataMap() {
        if (extendedData == null || extendedData.isEmpty()) {
            return new java.util.HashMap<>();
        }
        try {
            return ObjectInstanceMapperHolder.MAPPER.readValue(extendedData, Map.class);
        } catch (Exception e) {
            return new java.util.HashMap<>();
        }
    }

    /**
     * 设置扩展数据 Map
     */
    public void setExtendedDataMap(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            this.extendedData = "{}";
            return;
        }
        try {
            this.extendedData = ObjectInstanceMapperHolder.MAPPER.writeValueAsString(data);
        } catch (Exception e) {
            this.extendedData = "{}";
        }
    }
}

/**
 * JSON 映射器持有器
 */
class ObjectInstanceMapperHolder {
    static final com.fasterxml.jackson.databind.ObjectMapper MAPPER;

    static {
        MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();
        MAPPER.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        MAPPER.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
}

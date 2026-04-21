package com.ontology.platform.domain.entity;

import com.ontology.platform.common.enums.PropertyDataType;
import lombok.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 对象实例聚合根
 * Object Instance Aggregate Root
 * 表示基于ObjectType创建的具体业务实体实例
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectInstance {

    private String id;
    private String ontologyId;
    private String objectTypeId;
    private String objectTypeName;
    private String primaryKeyValue;

    /**
     * 实例属性（存储在JSONB中）
     * Key: 属性名
     * Value: 属性值
     */
    @Builder.Default
    private Map<String, Object> properties = new HashMap<>();

    /**
     * 实例状态：active, inactive, deleted
     */
    @Builder.Default
    private String status = "active";

    /**
     * 版本号（乐观锁）
     */
    @Builder.Default
    private int version = 1;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;

    /**
     * 创建新的对象实例
     */
    public static ObjectInstance create(
            String ontologyId,
            String objectTypeId,
            String objectTypeName,
            String primaryKeyValue,
            Map<String, Object> properties,
            String createdBy) {
        return ObjectInstance.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .objectTypeId(objectTypeId)
                .objectTypeName(objectTypeName)
                .primaryKeyValue(primaryKeyValue)
                .properties(properties != null ? new HashMap<>(properties) : new HashMap<>())
                .status("active")
                .version(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy(createdBy)
                .build();
    }

    /**
     * 获取属性值
     */
    public Object getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * 设置属性值
     */
    public void setProperty(String propertyName, Object value) {
        this.properties.put(propertyName, value);
        this.updatedAt = Instant.now();
    }

    /**
     * 批量设置属性
     */
    public void setProperties(Map<String, Object> newProperties) {
        if (newProperties != null) {
            this.properties.putAll(newProperties);
            this.updatedAt = Instant.now();
        }
    }

    /**
     * 移除属性
     */
    public void removeProperty(String propertyName) {
        this.properties.remove(propertyName);
        this.updatedAt = Instant.now();
    }

    /**
     * 检查属性是否存在
     */
    public boolean hasProperty(String propertyName) {
        return properties.containsKey(propertyName);
    }

    /**
     * 更新实例
     */
    public void update(Map<String, Object> newProperties) {
        if (newProperties != null) {
            this.properties.putAll(newProperties);
        }
        this.updatedAt = Instant.now();
        this.version++;
    }

    /**
     * 标记为已删除
     */
    public void markAsDeleted() {
        this.status = "deleted";
        this.updatedAt = Instant.now();
    }

    /**
     * 激活实例
     */
    public void activate() {
        this.status = "active";
        this.updatedAt = Instant.now();
    }

    /**
     * 停用实例
     */
    public void deactivate() {
        this.status = "inactive";
        this.updatedAt = Instant.now();
    }

    /**
     * 是否已删除
     */
    public boolean isDeleted() {
        return "deleted".equals(status);
    }

    /**
     * 是否激活
     */
    public boolean isActive() {
        return "active".equals(status);
    }

    /**
     * 获取主键属性名
     */
    public String getPrimaryKeyProperty() {
        return primaryKeyValue;
    }
}

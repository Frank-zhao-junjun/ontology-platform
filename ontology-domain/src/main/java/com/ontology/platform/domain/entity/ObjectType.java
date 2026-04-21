package com.ontology.platform.domain.entity;

import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 对象类型实体
 * Object Type Entity
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectType {

    private String id;
    private String ontologyId;
    private String name;
    private String displayName;
    private String description;
    private String primaryKey;
    private String parentId;

    @Builder.Default
    private List<String> interfaceNames = new ArrayList<>();

    private int instanceCount;
    private Instant createdAt;
    private Instant updatedAt;

    // 关联的属性定义列表
    @Builder.Default
    private List<Property> properties = new ArrayList<>();

    // 关联的关系定义列表
    @Builder.Default
    private List<Relation> relations = new ArrayList<>();

    /**
     * 创建新的对象类型
     */
    public static ObjectType create(
            String ontologyId,
            String name,
            String displayName,
            String description,
            String primaryKey) {
        return ObjectType.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .name(name)
                .displayName(displayName)
                .description(description)
                .primaryKey(primaryKey)
                .instanceCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 添加属性
     */
    public void addProperty(Property property) {
        this.properties.add(property);
        this.updatedAt = Instant.now();
    }

    /**
     * 移除属性
     */
    public void removeProperty(String propertyId) {
        this.properties.removeIf(p -> p.getId().equals(propertyId));
        this.updatedAt = Instant.now();
    }

    /**
     * 添加关系
     */
    public void addRelation(Relation relation) {
        this.relations.add(relation);
        this.updatedAt = Instant.now();
    }

    /**
     * 移除关系
     */
    public void removeRelation(String relationId) {
        this.relations.removeIf(r -> r.getId().equals(relationId));
        this.updatedAt = Instant.now();
    }

    /**
     * 更新实例计数
     */
    public void updateInstanceCount(int delta) {
        this.instanceCount += delta;
        if (this.instanceCount < 0) {
            this.instanceCount = 0;
        }
        this.updatedAt = Instant.now();
    }

    /**
     * 设置父类型
     */
    public void setParent(String parentId) {
        this.parentId = parentId;
        this.updatedAt = Instant.now();
    }

    /**
     * 更新信息
     */
    public void update(String displayName, String description, String primaryKey) {
        this.displayName = displayName;
        this.description = description;
        if (primaryKey != null && !primaryKey.isBlank()) {
            this.primaryKey = primaryKey;
        }
        this.updatedAt = Instant.now();
    }
}

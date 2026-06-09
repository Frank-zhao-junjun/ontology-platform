package com.ontology.platform.domain.entity;

import com.ontology.platform.common.enums.RelationCardinality;
import com.ontology.platform.domain.vo.RelationProperty;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 关系实体
 * Relation Entity
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Relation {

    private String id;
    private String ontologyId;
    private String sourceTypeId;
    private String targetTypeId;
    private String name;
    private String displayName;
    private String description;
    private RelationCardinality cardinality;
    private String reverseName;
    private String reverseDisplayName;
    private Instant createdAt;
    private Instant updatedAt;

    // 关系属性
    @Builder.Default
    private List<RelationProperty> properties = new ArrayList<>();

    /**
     * 创建新的关系
     */
    public static Relation create(
            String ontologyId,
            String sourceTypeId,
            String targetTypeId,
            String name,
            String displayName,
            String description,
            RelationCardinality cardinality) {
        return Relation.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .sourceTypeId(sourceTypeId)
                .targetTypeId(targetTypeId)
                .name(name)
                .displayName(displayName)
                .description(description)
                .cardinality(cardinality)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 设置反向关系
     */
    public void setReverse(String reverseName, String reverseDisplayName) {
        this.reverseName = reverseName;
        this.reverseDisplayName = reverseDisplayName;
        this.updatedAt = Instant.now();
    }

    /**
     * 添加关系属性
     */
    public void addProperty(RelationProperty property) {
        this.properties.add(property);
        this.updatedAt = Instant.now();
    }

    /**
     * 移除关系属性
     */
    public void removeProperty(String propertyName) {
        this.properties.removeIf(p -> p.getName().equals(propertyName));
        this.updatedAt = Instant.now();
    }

    /**
     * 更新信息
     */
    public void update(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    /**
     * 检查是否为多对多关系
     */
    public boolean isManyToMany() {
        return cardinality == RelationCardinality.MANY_TO_MANY;
    }

    /**
     * 检查是否为一对多关系
     */
    public boolean isOneToMany() {
        return cardinality == RelationCardinality.ONE_TO_MANY;
    }

    /**
     * 检查是否为多对一关系
     */
    public boolean isManyToOne() {
        return cardinality == RelationCardinality.MANY_TO_ONE;
    }

    /**
     * 检查是否为一对一关系
     */
    public boolean isOneToOne() {
        return cardinality == RelationCardinality.ONE_TO_ONE;
    }
}

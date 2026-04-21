package com.ontology.platform.domain.entity;

import com.ontology.platform.common.enums.OntologyStatus;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 本体聚合根
 * Ontology Aggregate Root
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ontology {

    private String id;
    private String tenantId;
    private String name;
    private String displayName;
    private String description;
    private String version;
    private OntologyStatus status;
    private Instant publishedAt;
    private int objectTypeCount;
    private int actionTypeCount;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    // 关联的对象类型列表
    @Builder.Default
    private List<ObjectType> objectTypes = new ArrayList<>();

    /**
     * 创建新的本体
     */
    public static Ontology create(String name, String displayName, String description, String createdBy) {
        return Ontology.builder()
                .id(UUID.randomUUID().toString())
                .tenantId("default")
                .name(name)
                .displayName(displayName)
                .description(description)
                .version("0.1.0")
                .status(OntologyStatus.DRAFT)
                .objectTypeCount(0)
                .actionTypeCount(0)
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 发布本体
     */
    public void publish() {
        if (this.status != OntologyStatus.DRAFT) {
            throw new IllegalStateException("Only draft ontology can be published");
        }
        this.status = OntologyStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 归档本体
     */
    public void archive() {
        if (this.status == OntologyStatus.ARCHIVED) {
            throw new IllegalStateException("Ontology is already archived");
        }
        this.status = OntologyStatus.ARCHIVED;
        this.updatedAt = Instant.now();
    }

    /**
     * 添加对象类型
     */
    public void addObjectType(ObjectType objectType) {
        this.objectTypes.add(objectType);
        this.objectTypeCount = this.objectTypes.size();
        this.updatedAt = Instant.now();
    }

    /**
     * 移除对象类型
     */
    public void removeObjectType(String objectTypeId) {
        this.objectTypes.removeIf(ot -> ot.getId().equals(objectTypeId));
        this.objectTypeCount = this.objectTypes.size();
        this.updatedAt = Instant.now();
    }

    /**
     * 更新本体信息
     */
    public void update(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    /**
     * 升级版本
     */
    public void bumpVersion() {
        String[] parts = this.version.split("\\.");
        int minor = Integer.parseInt(parts[1]) + 1;
        this.version = parts[0] + "." + minor + ".0";
        this.updatedAt = Instant.now();
    }
}

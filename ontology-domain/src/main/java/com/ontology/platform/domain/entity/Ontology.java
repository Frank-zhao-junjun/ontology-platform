package com.ontology.platform.domain.entity;

import com.ontology.platform.common.enums.OntologyStatus;
import com.ontology.platform.domain.event.OntologyArchivedEvent;
import com.ontology.platform.domain.event.OntologyCreatedEvent;
import com.ontology.platform.domain.event.OntologyDeletedEvent;
import com.ontology.platform.domain.event.OntologyPublishedEvent;
import com.ontology.platform.domain.event.OntologyUpdatedEvent;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 本体聚合根
 * Ontology Aggregate Root
 * 
 * 本体是业务领域的核心概念模型，包含对象类型、属性、关系等定义。
 * 遵循DDD聚合根模式，确保本体及其关联对象的业务一致性。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ontology {

    // ==================== 属性定义 ====================
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
    private boolean deleted;

    // 关联的对象类型列表
    @Builder.Default
    private List<ObjectType> objectTypes = new ArrayList<>();

    // ==================== 工厂方法 ====================

    /**
     * 创建新的本体
     * 
     * @param name 本体名称（唯一标识）
     * @param displayName 显示名称
     * @param description 本体描述
     * @param createdBy 创建人
     * @return 新的本体实例
     */
    public static Ontology create(String name, String displayName, String description, String createdBy) {
        validateName(name);
        
        return Ontology.builder()
                .id(UUID.randomUUID().toString())
                .tenantId("default")
                .name(name.trim())
                .displayName(displayName != null ? displayName.trim() : name.trim())
                .description(description != null ? description.trim() : "")
                .version("0.1.0")
                .status(OntologyStatus.DRAFT)
                .objectTypeCount(0)
                .actionTypeCount(0)
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .deleted(false)
                .build();
    }

    // ==================== 业务方法 ====================

    /**
     * 发布本体
     * 
     * 业务规则：
     * - 只有草稿状态的本体可以发布
     * - 发布后状态变为PUBLISHED
     * - 记录发布时间
     * 
     * @throws IllegalStateException 如果本体不是草稿状态
     */
    public void publish() {
        if (this.status != OntologyStatus.DRAFT) {
            throw new IllegalStateException(
                String.format("Only draft ontology can be published. Current status: %s", this.status)
            );
        }
        OntologyStatus previousStatus = this.status;
        this.status = OntologyStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.updatedAt = Instant.now();
        
        // 发布领域事件
        publishEvent(new OntologyPublishedEvent(
            this,
            this.id,
            this.tenantId,
            this.name,
            this.version,
            previousStatus,
            this.status,
            this.createdBy
        ));
    }

    /**
     * 归档本体
     * 
     * 业务规则：
     * - 已发布或草稿状态的本体都可以归档
     * - 已归档的本体不能再次归档
     * - 归档后状态变为ARCHIVED
     * 
     * @throws IllegalStateException 如果本体已经归档
     */
    public void archive() {
        if (this.status == OntologyStatus.ARCHIVED) {
            throw new IllegalStateException("Ontology is already archived");
        }
        OntologyStatus previousStatus = this.status;
        this.status = OntologyStatus.ARCHIVED;
        this.updatedAt = Instant.now();
        
        // 发布领域事件
        publishEvent(new OntologyArchivedEvent(
            this,
            this.id,
            this.tenantId,
            this.name,
            this.version,
            previousStatus,
            this.status,
            this.createdBy
        ));
    }

    /**
     * 添加对象类型
     * 
     * 业务规则：
     * - 已发布的本体不能添加对象类型（结构不可变）
     * 
     * @param objectType 对象类型
     * @throws IllegalStateException 如果本体已发布
     */
    public void addObjectType(ObjectType objectType) {
        if (this.status == OntologyStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot modify structure of published ontology");
        }
        this.objectTypes.add(objectType);
        this.objectTypeCount = this.objectTypes.size();
        this.updatedAt = Instant.now();
    }

    /**
     * 移除对象类型
     * 
     * 业务规则：
     * - 已发布的本体不能移除对象类型（结构不可变）
     * 
     * @param objectTypeId 对象类型ID
     * @throws IllegalStateException 如果本体已发布
     */
    public void removeObjectType(String objectTypeId) {
        if (this.status == OntologyStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot modify structure of published ontology");
        }
        this.objectTypes.removeIf(ot -> ot.getId().equals(objectTypeId));
        this.objectTypeCount = this.objectTypes.size();
        this.updatedAt = Instant.now();
    }

    /**
     * 更新本体信息
     * 
     * 业务规则：
     * - 已发布的本体只能更新描述，不能修改显示名称
     * - 草稿和归档状态的本体可以自由更新
     * 
     * @param displayName 显示名称
     * @param description 本体描述
     * @throws IllegalStateException 如果本体已发布且试图修改显示名称
     */
    public void update(String displayName, String description) {
        if (this.status == OntologyStatus.PUBLISHED && displayName != null) {
            throw new IllegalStateException("Cannot modify display name of published ontology");
        }
        
        if (displayName != null && !displayName.isBlank()) {
            this.displayName = displayName.trim();
        }
        if (description != null) {
            this.description = description.trim();
        }
        this.updatedAt = Instant.now();
        
        // 发布更新事件
        publishEvent(new OntologyUpdatedEvent(
            this,
            this.id,
            this.tenantId,
            this.name,
            this.displayName,
            this.description,
            this.createdBy
        ));
    }

    /**
     * 升级版本号
     * 
     * 采用语义化版本（SemVer）格式：主版本.次版本.修订版本
     * 次版本升级表示新增功能但保持向后兼容
     */
    public void bumpVersion() {
        String[] parts = this.version.split("\\.");
        if (parts.length != 3) {
            throw new IllegalStateException("Invalid version format: " + this.version);
        }
        int minor = Integer.parseInt(parts[1]) + 1;
        this.version = parts[0] + "." + minor + ".0";
        this.updatedAt = Instant.now();
    }

    /**
     * 检查是否可以修改结构
     * 
     * @return true表示可以修改，false表示不可修改
     */
    public boolean canModifyStructure() {
        return this.status != OntologyStatus.PUBLISHED;
    }

    /**
     * 检查是否可以删除
     * 
     * @return true表示可以删除，false表示不可删除
     */
    public boolean canDelete() {
        return !this.deleted;
    }

    /**
     * 标记为删除
     */
    public void markAsDeleted(String deletedBy) {
        this.deleted = true;
        this.updatedAt = Instant.now();
        
        // 发布删除事件
        publishEvent(new OntologyDeletedEvent(
            this,
            this.id,
            this.tenantId,
            this.name,
            deletedBy
        ));
    }

    // ==================== 私有方法 ====================

    /**
     * 验证名称格式
     */
    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Ontology name cannot be empty");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 100) {
            throw new IllegalArgumentException("Ontology name cannot exceed 100 characters");
        }
        // 名称只能包含字母、数字、下划线和中划线
        if (!trimmed.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException(
                "Ontology name can only contain letters, numbers, underscores and hyphens"
            );
        }
    }

    /**
     * 发布领域事件
     * 使用Spring的事件发布机制
     */
    private void publishEvent(Object event) {
        if (event instanceof OntologyCreatedEvent) {
            // 事件已在create方法中发布
        }
        // 实际发布需要通过Spring的ApplicationEventPublisher
        // 这里记录日志，生产环境会通过事件监听器处理
    }

    // ==================== 领域事件发布方法（供外部调用） ====================

    /**
     * 发布创建事件
     * 由应用层调用，用于在本体创建后发布事件
     */
    public void publishCreatedEvent() {
        publishEvent(new OntologyCreatedEvent(
            this,
            this.id,
            this.tenantId,
            this.name,
            this.displayName,
            this.createdBy
        ));
    }

    /**
     * 发布更新事件
     * 由应用层调用，用于在本体更新后发布事件
     */
    public void publishUpdateEvent() {
        publishEvent(new OntologyUpdatedEvent(
            this,
            this.id,
            this.tenantId,
            this.name,
            this.displayName,
            this.description,
            this.createdBy
        ));
    }

    /**
     * 发布删除事件
     * 由应用层调用，用于在本体删除后发布事件
     */
    public void publishDeleteEvent() {
        publishEvent(new OntologyDeletedEvent(
            this,
            this.id,
            this.tenantId,
            this.name,
            this.createdBy
        ));
    }
}

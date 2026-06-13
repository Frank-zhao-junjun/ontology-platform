package com.ontology.platform.domain.entity;

import lombok.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 对象实例实体
 * Object Instance Entity
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectInstance {

    /**
     * 主键ID
     */
    private String id;

    /**
     * 所属本体ID
     */
    private String ontologyId;

    /**
     * 所属对象类型ID
     */
    private String objectTypeId;

    /**
     * 业务主键值
     */
    private String primaryKeyValue;

    /**
     * 核心数据（JSON 字段映射）
     */
    @Builder.Default
    private Map<String, Object> coreData = new HashMap<>();

    /**
     * 扩展数据（JSON 字段映射）
     */
    @Builder.Default
    private Map<String, Object> extendedData = new HashMap<>();

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 更新时间
     */
    private Instant updatedAt;

    /**
     * 创建新的对象实例
     */
    public static ObjectInstance create(
            String ontologyId,
            String objectTypeId,
            String primaryKeyValue,
            Map<String, Object> coreData) {
        Instant now = Instant.now();
        return ObjectInstance.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .objectTypeId(objectTypeId)
                .primaryKeyValue(primaryKeyValue)
                .coreData(coreData != null ? coreData : new HashMap<>())
                .extendedData(new HashMap<>())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}

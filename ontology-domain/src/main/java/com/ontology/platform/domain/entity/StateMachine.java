package com.ontology.platform.domain.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * 对象状态机定义 (US-S06)。
 * 定义对象类型的生命周期状态及其转换规则。
 * 状态转换必须由行为驱动 -- 不允许跳过行为直接修改状态。
 * 因果链：行为执行 -> 状态转换 -> 领域事件发布 -> 事件处理器执行。
 */
@Getter
public class StateMachine {
    private final String id, contextId, name, nameEn;
    private final String objectTypeId, statusField;
    private final String statesJson, transitionsJson;
    private final Instant createdAt, updatedAt;

    @Builder
    public StateMachine(String id, String contextId, String name, String nameEn,
                        String objectTypeId, String statusField,
                        String statesJson, String transitionsJson,
                        Instant createdAt, Instant updatedAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.contextId = contextId;
        this.name = name;
        this.nameEn = nameEn;
        this.objectTypeId = objectTypeId;
        this.statusField = statusField != null ? statusField : "status";
        this.statesJson = statesJson != null ? statesJson : "[]";
        this.transitionsJson = transitionsJson != null ? transitionsJson : "[]";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt;
    }

    public static StateMachine create(String contextId, String name, String nameEn,
                                      String objectTypeId, String statusField,
                                      String statesJson, String transitionsJson) {
        return StateMachine.builder().contextId(contextId).name(name).nameEn(nameEn)
                .objectTypeId(objectTypeId).statusField(statusField)
                .statesJson(statesJson).transitionsJson(transitionsJson).build();
    }
}

package com.ontology.platform.domain.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * 业务场景定义 (US-S02)。
 * 同一限界上下文可定义多个业务场景（如 MTS / MTO），
 * 影响行为规则和事件处理器的适用范围。
 */
@Getter
public class BusinessScenario {
    private final String id, contextId, name, code, nameEn, description;
    private final String applicableObjectTypeIdsJson;
    private final Instant createdAt;

    @Builder
    public BusinessScenario(String id, String contextId, String name, String code,
                            String nameEn, String description,
                            String applicableObjectTypeIdsJson, Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.contextId = contextId;
        this.name = name;
        this.code = code;
        this.nameEn = nameEn;
        this.description = description;
        this.applicableObjectTypeIdsJson = applicableObjectTypeIdsJson != null ? applicableObjectTypeIdsJson : "[]";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static BusinessScenario create(String contextId, String name, String code,
                                          String nameEn, String description) {
        return BusinessScenario.builder().contextId(contextId).name(name).code(code)
                .nameEn(nameEn).description(description).build();
    }
}

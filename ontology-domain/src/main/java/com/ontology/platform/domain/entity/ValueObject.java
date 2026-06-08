package com.ontology.platform.domain.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * 可复用值对象定义 (US-S05)。
 * 值对象为全局级别（跨上下文复用），无独立业务标识符。
 * 修改时会提示影响范围（哪些对象类型引用了该值对象）。
 */
@Getter
public class ValueObject {
    private final String id, name, code, nameEn, description;
    private final String propertiesJson;
    private final Instant createdAt, updatedAt;

    @Builder
    public ValueObject(String id, String name, String code, String nameEn,
                       String description, String propertiesJson,
                       Instant createdAt, Instant updatedAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name;
        this.code = code;
        this.nameEn = nameEn;
        this.description = description;
        this.propertiesJson = propertiesJson != null ? propertiesJson : "[]";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt;
    }

    public static ValueObject create(String name, String code, String nameEn,
                                     String description, String propertiesJson) {
        return ValueObject.builder().name(name).code(code).nameEn(nameEn)
                .description(description).propertiesJson(propertiesJson).build();
    }
}

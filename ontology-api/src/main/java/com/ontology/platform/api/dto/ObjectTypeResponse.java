package com.ontology.platform.api.dto;

import com.ontology.platform.domain.entity.ObjectTypeV2;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectTypeResponse {
    @Schema(description = "对象类型ID") private String id;
    @Schema(description = "所属限界上下文ID") private String contextId;
    @Schema(description = "所属聚合根ID") private String aggregateRootId;
    @Schema(description = "父对象ID") private String parentObjectId;
    @Schema(description = "对象类型名称") private String name;
    @Schema(description = "对象类型代码") private String code;
    @Schema(description = "对象种类") private String objectKind;
    @Schema(description = "描述") private String description;
    @Schema(description = "属性定义(JSON)") private String attributes;
    @Schema(description = "是否激活") private boolean active;
    @Schema(description = "创建时间") private Instant createdAt;

    public static ObjectTypeResponse from(ObjectTypeV2 ot) {
        return ObjectTypeResponse.builder()
                .id(ot.getId())
                .contextId(ot.getContextId())
                .aggregateRootId(ot.getAggregateRootId())
                .parentObjectId(ot.getParentObjectId())
                .name(ot.getName())
                .code(ot.getCode())
                .objectKind(ot.getObjectKind())
                .description(ot.getDescription())
                .attributes(ot.getAttributes())
                .active(true) // ObjectTypeV2 currently lacks explicit active field; default true
                .createdAt(ot.getCreatedAt())
                .build();
    }
}

package com.ontology.platform.api.dto;

import com.ontology.platform.domain.entity.Relationship;
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
public class RelationshipResponse {
    @Schema(description = "关系ID") private String id;
    @Schema(description = "所属限界上下文ID") private String contextId;
    @Schema(description = "源对象类型ID") private String sourceObjectId;
    @Schema(description = "目标对象类型ID") private String targetObjectId;
    @Schema(description = "关系名称") private String name;
    @Schema(description = "关系代码") private String code;
    @Schema(description = "基数") private String cardinality;
    @Schema(description = "关系类型") private String relationKind;
    @Schema(description = "是否跨上下文") private boolean crossContext;
    @Schema(description = "跨上下文时的目标上下文ID") private String targetContextId;
    @Schema(description = "创建时间") private Instant createdAt;

    public static RelationshipResponse from(Relationship r) {
        return RelationshipResponse.builder()
                .id(r.getId())
                .contextId(r.getContextId())
                .sourceObjectId(r.getSourceObjectId())
                .targetObjectId(r.getTargetObjectId())
                .name(r.getName())
                .code(r.getCode())
                .cardinality(r.getCardinality())
                .relationKind(r.getRelationKind())
                .crossContext(r.isCrossContext())
                .targetContextId(r.getTargetContextId())
                .createdAt(r.getCreatedAt())
                .build();
    }
}

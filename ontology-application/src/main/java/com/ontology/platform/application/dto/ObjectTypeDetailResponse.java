package com.ontology.platform.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "对象类型详情响应（含属性和关系列表）")
public class ObjectTypeDetailResponse extends ObjectTypeResponse {

    @Schema(description = "属性列表")
    private List<PropertyResponse> properties;
    @Schema(description = "关系摘要列表")
    private List<RelationSummary> relations;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "关系摘要")
    public static class RelationSummary {
        @Schema(description = "关系ID")
        private String id;
        @Schema(description = "关系名称")
        private String name;
        @Schema(description = "显示名称")
        private String displayName;
        @Schema(description = "目标对象类型ID")
        private String targetTypeId;
        @Schema(description = "目标对象类型名称")
        private String targetTypeName;
        @Schema(description = "基数")
        private String cardinality;
    }
}

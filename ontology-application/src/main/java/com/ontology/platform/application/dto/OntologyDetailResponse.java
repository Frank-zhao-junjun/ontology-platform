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
@Schema(description = "本体详情响应（含对象类型列表）")
public class OntologyDetailResponse extends OntologyResponse {

    @Schema(description = "对象类型列表")
    private List<ObjectTypeSummary> objectTypes;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "对象类型摘要")
    public static class ObjectTypeSummary {
        @Schema(description = "对象类型ID")
        private String id;
        @Schema(description = "名称")
        private String name;
        @Schema(description = "显示名称")
        private String displayName;
        @Schema(description = "属性数量")
        private int propertyCount;
        @Schema(description = "关系数量")
        private int relationCount;
        @Schema(description = "实例数量")
        private int instanceCount;
    }
}

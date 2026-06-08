package com.ontology.platform.application.dto;

import lombok.*;

import java.util.List;

/**
 * 对象类型详情响应DTO（包含属性和关系列表）
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectTypeDetailResponse extends ObjectTypeResponse {

    private List<PropertyResponse> properties;
    private List<RelationSummary> relations;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationSummary {
        private String id;
        private String name;
        private String displayName;
        private String targetTypeId;
        private String targetTypeName;
        private String cardinality;
    }
}

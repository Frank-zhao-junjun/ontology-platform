package com.ontology.platform.application.dto;

import lombok.*;

import java.util.List;

/**
 * 本体详情响应DTO（包含对象类型列表）
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OntologyDetailResponse extends OntologyResponse {

    private List<ObjectTypeSummary> objectTypes;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObjectTypeSummary {
        private String id;
        private String name;
        private String displayName;
        private int propertyCount;
        private int relationCount;
        private int instanceCount;
    }
}

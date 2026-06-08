package com.ontology.platform.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 图遍历查询请求DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphQueryRequest {

    @NotBlank(message = "本体ID不能为空")
    private String ontologyId;

    @NotBlank(message = "起点对象类型不能为空")
    private String startObjectType;

    @NotBlank(message = "起点对象ID不能为空")
    private String startObjectId;

    @Builder.Default
    private List<TraversalPath> path = new ArrayList<>();

    @Min(value = 1, message = "最大深度必须大于0")
    @Max(value = 5, message = "最大深度不能超过5")
    private Integer maxDepth;

    @Min(value = 1, message = "限制数必须大于0")
    @Max(value = 1000, message = "限制数不能超过1000")
    private Integer limit;

    private String direction;  // OUT, IN, BOTH

    private List<GraphFilter> filters;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TraversalPath {
        @NotBlank(message = "关系类型不能为空")
        private String relationType;
        private String targetObjectType;
        @Min(value = 1, message = "深度必须大于0")
        @Max(value = 3, message = "单段深度不能超过3")
        private Integer depth;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphFilter {
        private int depth;
        private String targetType;
        @Builder.Default
        private List<FilterCondition> conditions = new ArrayList<>();
        private String logic;  // AND, OR
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterCondition {
        @NotBlank(message = "字段名不能为空")
        private String field;
        @NotBlank(message = "操作符不能为空")
        private String operator;  // eq, ne, gt, gte, lt, lte, in, notIn, contains, isNull, isNotNull
        private Object value;
    }
}

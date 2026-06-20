package com.ontology.platform.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "图遍历查询请求")
public class GraphQueryRequest {

    @NotBlank(message = "本体ID不能为空")
    @Schema(description = "所属本体ID")
    private String ontologyId;

    @NotBlank(message = "起点对象类型不能为空")
    @Schema(description = "起点对象类型")
    private String startObjectType;

    @NotBlank(message = "起点对象ID不能为空")
    @Schema(description = "起点对象ID")
    private String startObjectId;

    @Builder.Default
    @Schema(description = "遍历路径（按顺序递归）")
    private List<TraversalPath> path = new ArrayList<>();

    @Min(value = 1, message = "最大深度必须大于0")
    @Max(value = 5, message = "最大深度不能超过5")
    @Schema(description = "最大遍历深度", example = "3")
    private Integer maxDepth;

    @Min(value = 1, message = "限制数必须大于0")
    @Max(value = 1000, message = "限制数不能超过1000")
    @Schema(description = "返回结果限制", example = "100")
    private Integer limit;

    @Schema(description = "遍历方向: OUT/IN/BOTH", example = "OUT")
    private String direction;

    @Schema(description = "过滤器列表")
    private List<GraphFilter> filters;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "遍历路径段")
    public static class TraversalPath {
        @NotBlank(message = "关系类型不能为空")
        @Schema(description = "关系类型")
        private String relationType;
        @Schema(description = "目标对象类型（可选过滤）")
        private String targetObjectType;
        @Min(value = 1, message = "深度必须大于0")
        @Max(value = 3, message = "单段深度不能超过3")
        @Schema(description = "本段遍历深度", example = "1")
        private Integer depth;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "图过滤器")
    public static class GraphFilter {
        @Schema(description = "过滤深度层级")
        private int depth;
        @Schema(description = "目标类型过滤")
        private String targetType;
        @Builder.Default
        @Schema(description = "过滤条件列表")
        private List<FilterCondition> conditions = new ArrayList<>();
        @Schema(description = "条件逻辑: AND/OR", example = "AND")
        private String logic;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "过滤条件")
    public static class FilterCondition {
        @NotBlank(message = "字段名不能为空")
        @Schema(description = "字段名")
        private String field;
        @NotBlank(message = "操作符不能为空")
        @Schema(description = "操作符: eq/ne/gt/gte/lt/lte/in/notIn/contains/isNull/isNotNull")
        private String operator;
        @Schema(description = "值")
        private Object value;
    }
}

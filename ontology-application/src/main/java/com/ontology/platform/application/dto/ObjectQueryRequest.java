package com.ontology.platform.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "对象列表查询请求")
public class ObjectQueryRequest {

    @NotBlank(message = "本体ID不能为空")
    @Schema(description = "所属本体ID")
    private String ontologyId;

    @NotBlank(message = "对象类型不能为空")
    @Schema(description = "对象类型名称")
    private String objectType;

    @Builder.Default
    @Schema(description = "过滤条件列表")
    private List<QueryFilter> filters = new ArrayList<>();

    @Schema(description = "条件逻辑: AND/OR", example = "AND")
    private String logic;

    @Builder.Default
    @Schema(description = "包含的属性字段列表")
    private List<String> includeProperties = new ArrayList<>();

    @Schema(description = "排序字段", example = "createdAt")
    private String orderBy;
    @Schema(description = "排序方向: ASC/DESC")
    private String order;

    @Min(value = 1, message = "偏移量必须大于0")
    @Schema(description = "偏移量（偏移分页）", example = "0")
    private Integer offset;

    @Min(value = 1, message = "限制数必须大于0")
    @Max(value = 100, message = "限制数不能超过100")
    @Schema(description = "每页限制", example = "20")
    private Integer limit;

    @Schema(description = "游标（游标分页）")
    private String cursor;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "查询过滤条件")
    public static class QueryFilter {
        @NotBlank(message = "字段名不能为空")
        @Schema(description = "字段名")
        private String field;
        @NotBlank(message = "操作符不能为空")
        @Schema(description = "操作符: eq/ne/gt/gte/lt/lte/contains/in/notIn")
        private String operator;
        @Schema(description = "值")
        private Object value;
    }
}

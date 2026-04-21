package com.ontology.platform.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 对象列表查询请求DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectQueryRequest {

    @NotBlank(message = "本体ID不能为空")
    private String ontologyId;

    @NotBlank(message = "对象类型不能为空")
    private String objectType;

    @Builder.Default
    private List<QueryFilter> filters = new ArrayList<>();

    private String logic;  // AND, OR

    @Builder.Default
    private List<String> includeProperties = new ArrayList<>();

    private String orderBy;
    private String order;  // ASC, DESC

    @Min(value = 1, message = "偏移量必须大于0")
    private Integer offset;

    @Min(value = 1, message = "限制数必须大于0")
    @Max(value = 100, message = "限制数不能超过100")
    private Integer limit;

    private String cursor;  // 游标分页

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryFilter {
        @NotBlank(message = "字段名不能为空")
        private String field;
        @NotBlank(message = "操作符不能为空")
        private String operator;
        private Object value;
    }
}

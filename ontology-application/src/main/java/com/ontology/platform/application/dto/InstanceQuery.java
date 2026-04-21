package com.ontology.platform.application.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * 对象实例查询请求DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstanceQuery {

    /**
     * 对象类型ID（可选）
     */
    private String objectTypeId;

    /**
     * 对象类型名称（可选）
     */
    private String objectTypeName;

    /**
     * 状态过滤（active, inactive, deleted）
     */
    private String status;

    /**
     * 属性过滤条件
     */
    private Map<String, Object> propertyFilters;

    /**
     * 页码
     */
    @Builder.Default
    private int page = 1;

    /**
     * 每页数量
     */
    @Builder.Default
    private int pageSize = 20;

    /**
     * 排序字段
     */
    private String sortBy;

    /**
     * 排序方向（asc, desc）
     */
    @Builder.Default
    private String sortOrder = "desc";

    /**
     * 游标（用于分页）
     */
    private String cursor;

    /**
     * 返回的字段列表（可选）
     */
    private List<String> fields;
}

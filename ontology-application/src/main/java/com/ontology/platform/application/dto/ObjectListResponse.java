package com.ontology.platform.application.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 对象列表查询响应DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectListResponse<T> {

    @Builder.Default
    private List<T> items = new ArrayList<>();

    /**
     * 总数
     */
    private long total;

    /**
     * 过滤后的总数
     */
    private long totalFiltered;

    /**
     * 当前页码
     */
    private int page;

    /**
     * 每页大小
     */
    private int pageSize;

    /**
     * 是否有更多数据
     */
    private boolean hasMore;

    /**
     * 游标（用于游标分页）
     */
    private String cursor;
}

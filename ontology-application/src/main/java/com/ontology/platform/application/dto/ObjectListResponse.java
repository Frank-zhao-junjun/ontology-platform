package com.ontology.platform.application.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 对象列表查询响应DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectListResponse {

    @Builder.Default
    private List<ObjectData> items = new ArrayList<>();

    private PaginationMeta meta;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObjectData {
        private String id;
        private String objectType;
        private Map<String, Object> properties;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationMeta {
        private int total;
        private int offset;
        private int limit;
        private boolean hasMore;
        private String nextCursor;
    }
}

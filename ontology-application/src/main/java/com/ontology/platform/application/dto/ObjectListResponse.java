package com.ontology.platform.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "对象列表查询响应")
public class ObjectListResponse {

    @Builder.Default
    @Schema(description = "数据项列表")
    private List<ObjectData> items = new ArrayList<>();

    @Schema(description = "分页元数据")
    private PaginationMeta meta;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "对象数据项")
    public static class ObjectData {
        @Schema(description = "对象ID")
        private String id;
        @Schema(description = "对象类型")
        private String objectType;
        @Schema(description = "属性字典")
        private Map<String, Object> properties;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "分页元数据")
    public static class PaginationMeta {
        @Schema(description = "总数")
        private int total;
        @Schema(description = "偏移量")
        private int offset;
        @Schema(description = "每页限制")
        private int limit;
        @Schema(description = "是否有更多")
        private boolean hasMore;
        @Schema(description = "下一页游标")
        private String nextCursor;
    }
}

package com.ontology.platform.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;

/**
 * 统一API响应格式
 * Unified API Response Format
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;
    private Meta meta;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        @Builder.Default
        private String requestId = RequestContext.getRequestId();
        @Builder.Default
        private String timestamp = Instant.now().toString();
        private Page page;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Page {
        private String cursor;
        private boolean hasMore;
        private long total;
        private int limit;
    }

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(0)
                .message("success")
                .data(data)
                .meta(Meta.builder().build())
                .build();
    }

    /**
     * 成功响应（带分页信息）
     */
    public static <T> ApiResponse<T> success(T data, long total, int limit, String cursor, boolean hasMore) {
        return ApiResponse.<T>builder()
                .code(0)
                .message("success")
                .data(data)
                .meta(Meta.builder()
                        .page(Page.builder()
                                .total(total)
                                .limit(limit)
                                .cursor(cursor)
                                .hasMore(hasMore)
                                .build())
                        .build())
                .build();
    }

    /**
     * 错误响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .meta(Meta.builder().build())
                .build();
    }

    /**
     * 错误响应（带详情）
     */
    public static <T> ApiResponse<T> error(int code, String message, String details) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .meta(Meta.builder().build())
                .build();
    }
}

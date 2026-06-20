package com.ontology.platform.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "统一API响应格式")
public class ApiResponse<T> {

    @Schema(description = "业务状态码: 0=成功", example = "0")
    private int code;
    @Schema(description = "响应消息", example = "success")
    private String message;
    @Schema(description = "响应数据（泛型）")
    private T data;
    @Schema(description = "元数据")
    private Meta meta;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "请求元数据")
    public static class Meta {
        @Builder.Default
        @Schema(description = "请求ID（链路追踪）")
        private String requestId = RequestContext.getRequestId();
        @Builder.Default
        @Schema(description = "时间戳")
        private String timestamp = Instant.now().toString();
        @Schema(description = "分页信息")
        private Page page;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "分页信息")
    public static class Page {
        @Schema(description = "下一页游标")
        private String cursor;
        @Schema(description = "是否还有更多数据")
        private boolean hasMore;
        @Schema(description = "总记录数")
        private long total;
        @Schema(description = "每页限制")
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

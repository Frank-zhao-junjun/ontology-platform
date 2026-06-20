package com.ontology.platform.application.dto.upload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "导入任务响应DTO，包含导入结果和统计")
public class ImportTaskResponse {

    /**
     * 导入任务ID
     */
    private String importId;

    /**
     * 上传任务ID
     */
    private String uploadId;

    /**
     * 本体ID
     */
    private String ontologyId;

    /**
     * 对象类型名称
     */
    private String objectTypeName;

    /**
     * 状态
     */
    private String status;

    /**
     * 进度信息
     */
    private Progress progress;

    /**
     * 错误列表
     */
    private List<ErrorDetail> errors;

    /**
     * 开始时间
     */
    private String startedAt;

    /**
     * 完成时间
     */
    private String completedAt;

    /**
     * 预计完成时间
     */
    private String estimatedCompletion;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Progress {
        private long totalRows;
        private long processedRows;
        private long successRows;
        private long failedRows;
        private int progressPercent;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetail {
        private int row;
        private String field;
        private String message;
        private String originalValue;
    }
}

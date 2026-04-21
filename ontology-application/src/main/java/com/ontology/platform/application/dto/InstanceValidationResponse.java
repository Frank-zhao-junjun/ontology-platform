package com.ontology.platform.application.dto;

import lombok.*;

import java.util.List;

/**
 * 实例验证响应DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstanceValidationResponse {

    /**
     * 是否有效
     */
    private boolean valid;

    /**
     * 验证的实例数量
     */
    private int instanceCount;

    /**
     * 有效实例数量
     */
    private int validCount;

    /**
     * 无效实例数量
     */
    private int invalidCount;

    /**
     * 错误数量
     */
    private int errorCount;

    /**
     * 警告数量
     */
    private int warningCount;

    /**
     * 每个实例的验证结果
     */
    private List<InstanceValidationResult> results;

    /**
     * 单个实例的验证结果
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstanceValidationResult {
        private String instanceId;
        private String primaryKeyValue;
        private boolean valid;
        private List<ValidationIssue> issues;
    }

    /**
     * 验证问题
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationIssue {
        private String severity;
        private String type;
        private String field;
        private String message;
    }
}

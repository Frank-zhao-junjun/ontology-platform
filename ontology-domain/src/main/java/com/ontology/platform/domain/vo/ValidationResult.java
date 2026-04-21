package com.ontology.platform.domain.vo;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 验证结果值对象
 * Validation Result Value Object
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

    /**
     * 是否有效
     */
    @Builder.Default
    private boolean valid = true;

    /**
     * 验证错误信息列表
     */
    @Builder.Default
    private List<ValidationError> errors = new ArrayList<>();

    /**
     * 验证警告信息列表
     */
    @Builder.Default
    private List<ValidationError> warnings = new ArrayList<>();

    /**
     * 添加错误
     */
    public void addError(String field, String message) {
        this.errors.add(new ValidationError("ERROR", field, message));
        this.valid = false;
    }

    /**
     * 添加错误（带类型）
     */
    public void addError(String severity, String field, String message) {
        if ("ERROR".equalsIgnoreCase(severity)) {
            this.errors.add(new ValidationError(severity, field, message));
            this.valid = false;
        } else {
            this.warnings.add(new ValidationError(severity, field, message));
        }
    }

    /**
     * 添加警告
     */
    public void addWarning(String field, String message) {
        this.warnings.add(new ValidationError("WARNING", field, message));
    }

    /**
     * 设置为无效
     */
    public void invalidate() {
        this.valid = false;
    }

    /**
     * 检查是否有错误
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * 检查是否有警告
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * 获取总问题数
     */
    public int getTotalIssueCount() {
        return errors.size() + warnings.size();
    }

    /**
     * 创建成功的验证结果
     */
    public static ValidationResult success() {
        return ValidationResult.builder()
                .valid(true)
                .errors(new ArrayList<>())
                .warnings(new ArrayList<>())
                .build();
    }

    /**
     * 创建失败的验证结果
     */
    public static ValidationResult failure(String field, String message) {
        ValidationResult result = new ValidationResult();
        result.addError(field, message);
        return result;
    }

    /**
     * 验证错误
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        /**
         * 严重程度：ERROR, WARNING, INFO
         */
        private String severity;

        /**
         * 字段名
         */
        private String field;

        /**
         * 错误信息
         */
        private String message;
    }
}

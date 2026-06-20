package com.ontology.platform.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "本体验证结果响应")
public class ValidationResultResponse {

    @Schema(description = "是否通过验证")
    private boolean valid;

    @Builder.Default
    @Schema(description = "验证摘要")
    private ValidationSummary summary = new ValidationSummary();

    @Builder.Default
    @Schema(description = "验证问题列表")
    private List<ValidationIssue> issues = new ArrayList<>();

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "验证摘要")
    public static class ValidationSummary {
        @Builder.Default
        @Schema(description = "错误数")
        private int errors = 0;
        @Builder.Default
        @Schema(description = "警告数")
        private int warnings = 0;
        @Builder.Default
        @Schema(description = "通过数")
        private int passed = 0;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "验证问题")
    public static class ValidationIssue {
        @Schema(description = "严重级别: ERROR/WARNING/INFO")
        private String severity;
        @Schema(description = "问题类型")
        private String type;
        @Schema(description = "实体类型")
        private String entityType;
        @Schema(description = "实体ID")
        private String entityId;
        @Schema(description = "实体名称")
        private String entityName;
        @Schema(description = "问题描述")
        private String message;
        @Schema(description = "修改建议")
        private String suggestion;
    }
}

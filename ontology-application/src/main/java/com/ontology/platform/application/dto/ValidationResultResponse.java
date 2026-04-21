package com.ontology.platform.application.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 本体验证结果响应DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResultResponse {

    private boolean valid;

    @Builder.Default
    private ValidationSummary summary = new ValidationSummary();

    @Builder.Default
    private List<ValidationIssue> issues = new ArrayList<>();

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationSummary {
        @Builder.Default
        private int errors = 0;
        @Builder.Default
        private int warnings = 0;
        @Builder.Default
        private int passed = 0;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationIssue {
        private String severity;  // ERROR, WARNING, INFO
        private String type;
        private String entityType;
        private String entityId;
        private String entityName;
        private String message;
        private String suggestion;
    }
}

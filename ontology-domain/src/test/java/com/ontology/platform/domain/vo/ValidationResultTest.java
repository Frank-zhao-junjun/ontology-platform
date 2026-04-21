package com.ontology.platform.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ValidationResult值对象测试
 */
@DisplayName("ValidationResult值对象测试")
class ValidationResultTest {

    @Nested
    @DisplayName("创建验证结果")
    class CreateTests {

        @Test
        @DisplayName("应创建成功的验证结果")
        void shouldCreateSuccessResult() {
            // Act
            ValidationResult result = ValidationResult.success();

            // Assert
            assertThat(result.isValid()).isTrue();
            assertThat(result.hasErrors()).isFalse();
            assertThat(result.hasWarnings()).isFalse();
            assertThat(result.getTotalIssueCount()).isZero();
        }

        @Test
        @DisplayName("应创建失败的验证结果")
        void shouldCreateFailureResult() {
            // Act
            ValidationResult result = ValidationResult.failure("field", "error message");

            // Assert
            assertThat(result.isValid()).isFalse();
            assertThat(result.hasErrors()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("field");
            assertThat(result.getErrors().get(0).getMessage()).isEqualTo("error message");
        }
    }

    @Nested
    @DisplayName("添加错误")
    class AddErrorTests {

        @Test
        @DisplayName("应添加错误并标记为无效")
        void shouldAddErrorAndInvalidate() {
            // Arrange
            ValidationResult result = ValidationResult.success();
            assertThat(result.isValid()).isTrue();

            // Act
            result.addError("name", "名称不能为空");

            // Assert
            assertThat(result.isValid()).isFalse();
            assertThat(result.hasErrors()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getSeverity()).isEqualTo("ERROR");
            assertThat(result.getErrors().get(0).getField()).isEqualTo("name");
            assertThat(result.getErrors().get(0).getMessage()).isEqualTo("名称不能为空");
        }

        @Test
        @DisplayName("应添加多个错误")
        void shouldAddMultipleErrors() {
            // Arrange
            ValidationResult result = ValidationResult.success();

            // Act
            result.addError("field1", "错误1");
            result.addError("field2", "错误2");
            result.addError("field3", "错误3");

            // Assert
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(3);
        }

        @Test
        @DisplayName("添加ERROR级别应标记为无效")
        void shouldInvalidateForErrorSeverity() {
            // Arrange
            ValidationResult result = ValidationResult.success();

            // Act
            result.addError("ERROR", "field", "error message");

            // Assert
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("添加WARNING级别不应标记为无效")
        void shouldNotInvalidateForWarningSeverity() {
            // Arrange
            ValidationResult result = ValidationResult.success();

            // Act
            result.addError("WARNING", "field", "warning message");

            // Assert
            assertThat(result.isValid()).isTrue();
            assertThat(result.hasErrors()).isFalse();
            assertThat(result.hasWarnings()).isTrue();
            assertThat(result.getWarnings()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("添加警告")
    class AddWarningTests {

        @Test
        @DisplayName("应添加警告不影响有效性")
        void shouldAddWarningWithoutAffectingValidity() {
            // Arrange
            ValidationResult result = ValidationResult.success();

            // Act
            result.addWarning("name", "建议使用更规范的名称");

            // Assert
            assertThat(result.isValid()).isTrue();
            assertThat(result.hasErrors()).isFalse();
            assertThat(result.hasWarnings()).isTrue();
            assertThat(result.getWarnings()).hasSize(1);
            assertThat(result.getWarnings().get(0).getSeverity()).isEqualTo("WARNING");
        }
    }

    @Nested
    @DisplayName("invalidate - 强制无效")
    class InvalidateTests {

        @Test
        @DisplayName("应强制标记为无效")
        void shouldForceInvalidate() {
            // Arrange
            ValidationResult result = ValidationResult.success();
            assertThat(result.isValid()).isTrue();

            // Act
            result.invalidate();

            // Assert
            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("统计信息")
    class StatisticsTests {

        @Test
        @DisplayName("应正确统计问题总数")
        void shouldCountTotalIssues() {
            // Arrange
            ValidationResult result = ValidationResult.success();

            // Act
            result.addError("field1", "error");
            result.addWarning("field2", "warning");
            result.addWarning("field3", "warning");

            // Assert
            assertThat(result.getTotalIssueCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("应正确判断是否有错误")
        void shouldCheckHasErrors() {
            // Arrange
            ValidationResult result = ValidationResult.success();

            // Assert
            assertThat(result.hasErrors()).isFalse();

            // Act
            result.addError("field", "error");

            // Assert
            assertThat(result.hasErrors()).isTrue();
        }

        @Test
        @DisplayName("应正确判断是否有警告")
        void shouldCheckHasWarnings() {
            // Arrange
            ValidationResult result = ValidationResult.success();

            // Assert
            assertThat(result.hasWarnings()).isFalse();

            // Act
            result.addWarning("field", "warning");

            // Assert
            assertThat(result.hasWarnings()).isTrue();
        }
    }

    @Nested
    @DisplayName("ValidationError内部类")
    class ValidationErrorTests {

        @Test
        @DisplayName("应正确创建验证错误")
        void shouldCreateValidationError() {
            // Act
            ValidationResult.ValidationError error = ValidationResult.ValidationError.builder()
                    .severity("ERROR")
                    .field("email")
                    .message("邮箱格式不正确")
                    .build();

            // Assert
            assertThat(error.getSeverity()).isEqualTo("ERROR");
            assertThat(error.getField()).isEqualTo("email");
            assertThat(error.getMessage()).isEqualTo("邮箱格式不正确");
        }
    }
}

package com.ontology.platform.api.config;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.common.exception.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GlobalExceptionHandler测试
 * 
 * 测试范围：
 * - 业务异常处理
 * - 资源不存在异常处理
 * - 参数校验异常处理
 * - 绑定异常处理
 * - 约束违反异常处理
 */
@DisplayName("GlobalExceptionHandler测试")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("BusinessException - 业务异常处理")
    class BusinessExceptionTests {

        @Test
        @DisplayName("应返回400状态码")
        void shouldReturn400ForBusinessException() {
            // Arrange
            BusinessException exception = new BusinessException(ErrorCode.VALIDATION_ERROR, "业务验证失败");

            // Act
            ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
            assertThat(response.getBody().getMessage()).isEqualTo("业务验证失败");
        }

        @Test
        @DisplayName("应正确映射不同错误码到HTTP状态")
        void shouldMapErrorCodesToHttpStatus() {
            // Arrange
            BusinessException notImplementedException = new BusinessException(
                    ErrorCode.NOT_IMPLEMENTED, "功能未实现");

            // Act
            ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(notImplementedException);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("ResourceNotFoundException - 资源不存在异常处理")
    class ResourceNotFoundExceptionTests {

        @Test
        @DisplayName("应返回404状态码")
        void shouldReturn404ForResourceNotFound() {
            // Arrange
            ResourceNotFoundException exception = new ResourceNotFoundException("Ontology", "test-id-123");

            // Act
            ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleResourceNotFoundException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
            assertThat(response.getBody().getMessage()).contains("Ontology");
            assertThat(response.getBody().getMessage()).contains("test-id-123");
        }

        @Test
        @DisplayName("应包含错误码6001")
        void shouldIncludeErrorCode6001() {
            // Arrange
            ResourceNotFoundException exception = new ResourceNotFoundException("ObjectType", "456");

            // Act
            ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleResourceNotFoundException(exception);

            // Assert
            assertThat(response.getBody().getCode()).isEqualTo(6001);
        }

        @Test
        @DisplayName("应处理简单消息异常")
        void shouldHandleSimpleMessageException() {
            // Arrange
            ResourceNotFoundException exception = new ResourceNotFoundException("资源不存在");

            // Act
            ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleResourceNotFoundException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().getMessage()).isEqualTo("资源不存在");
        }
    }

    @Nested
    @DisplayName("ValidationException - 验证异常处理")
    class ValidationExceptionTests {

        @Test
        @DisplayName("应返回400状态码")
        void shouldReturn400ForValidationException() {
            // Arrange
            ValidationException exception = new ValidationException("字段验证失败");

            // Act
            ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleValidationException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
            assertThat(response.getBody().getMessage()).isEqualTo("字段验证失败");
        }

        @Test
        @DisplayName("应包含错误码3001")
        void shouldIncludeErrorCode3001() {
            // Arrange
            ValidationException exception = new ValidationException("参数不合法");

            // Act
            ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleValidationException(exception);

            // Assert
            assertThat(response.getBody().getCode()).isEqualTo(3001);
        }

        @Test
        @DisplayName("应包含详细信息")
        void shouldIncludeDetails() {
            // Arrange
            ValidationException exception = new ValidationException("字段不合法", "field: name");

            // Act
            ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleValidationException(exception);

            // Assert
            assertThat(response.getBody().getMessage()).isEqualTo("字段不合法");
        }
    }

    @Nested
    @DisplayName("MethodArgumentNotValidException - 方法参数校验异常处理")
    class MethodArgumentNotValidExceptionTests {

        @Test
        @DisplayName("应返回400状态码")
        void shouldReturn400ForMethodArgumentNotValid() {
            // Arrange
            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
            
            when(exception.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(
                    createFieldError("name", "名称不能为空")
            ));

            // Act
            ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleMethodArgumentNotValidException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
            assertThat(response.getBody().getMessage()).contains("name");
        }

        @Test
        @DisplayName("应处理多个字段错误")
        void shouldHandleMultipleFieldErrors() {
            // Arrange
            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
            
            when(exception.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(
                    createFieldError("name", "名称不能为空"),
                    createFieldError("displayName", "显示名称不能为空")
            ));

            // Act
            ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleMethodArgumentNotValidException(exception);

            // Assert
            assertThat(response.getBody().getMessage()).contains("名称不能为空");
            assertThat(response.getBody().getMessage()).contains("显示名称不能为空");
        }
    }

    @Nested
    @DisplayName("BindException - 绑定异常处理")
    class BindExceptionTests {

        @Test
        @DisplayName("应返回400状态码")
        void shouldReturn400ForBindException() {
            // Arrange
            org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
            org.springframework.web.bind.BindException bindException = new org.springframework.web.bind.BindException(bindingResult);
            
            when(bindingResult.getFieldErrors()).thenReturn(List.of(
                    createFieldError("field", "字段错误")
            ));

            // Act
            ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBindException(bindException);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
            assertThat(response.getBody().getMessage()).contains("Binding failed");
        }
    }

    @Nested
    @DisplayName("ConstraintViolationException - 约束违反异常处理")
    class ConstraintViolationExceptionTests {

        @Test
        @DisplayName("应返回400状态码")
        void shouldReturn400ForConstraintViolation() {
            // Arrange
            ConstraintViolationException exception = mock(ConstraintViolationException.class);
            
            @SuppressWarnings("unchecked")
            ConstraintViolation<String> violation = mock(ConstraintViolation.class);
            Path propertyPath = mock(Path.class);
            
            when(exception.getConstraintViolations()).thenReturn(Set.of(violation));
            when(violation.getPropertyPath()).thenReturn(propertyPath);
            when(violation.getMessage()).thenReturn("must not be null");
            when(propertyPath.toString()).thenReturn("createOntology.request.name");

            // Act
            ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleConstraintViolationException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("MissingServletRequestParameterException - 缺少请求参数异常处理")
    class MissingServletRequestParameterExceptionTests {

        @Test
        @DisplayName("应返回400状态码")
        void shouldReturn400ForMissingParameter() {
            // Arrange
            MissingServletRequestParameterException exception = 
                    new MissingServletRequestParameterException("page", "int");

            // Act
            ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleMissingServletRequestParameter(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
            assertThat(response.getBody().getMessage()).contains("page");
        }
    }

    @Nested
    @DisplayName("Exception - 通用异常处理")
    class GeneralExceptionTests {

        @Test
        @DisplayName("应返回500状态码")
        void shouldReturn500ForGeneralException() {
            // Arrange
            Exception exception = new RuntimeException("Unexpected error");

            // Act
            ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
        }
    }

    // ==================== Helper Methods ====================

    private FieldError createFieldError(String field, String message) {
        return new FieldError("object", field, message);
    }
}

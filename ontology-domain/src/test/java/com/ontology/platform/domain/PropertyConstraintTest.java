package com.ontology.platform.domain;

import com.ontology.platform.domain.vo.PropertyConstraint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * PropertyConstraint值对象测试
 */
@DisplayName("PropertyConstraint值对象测试")
class PropertyConstraintTest {

    @Nested
    @DisplayName("create - 创建约束")
    class CreateTests {

        @Test
        @DisplayName("应创建最小值约束")
        void shouldCreateMinValueConstraint() {
            PropertyConstraint constraint = PropertyConstraint.minValue(new BigDecimal("10"), "值不能小于10");
            assertThat(constraint.getType()).isEqualTo(PropertyConstraint.ConstraintType.MIN_VALUE);
            assertThat(constraint.getValue()).isEqualTo(new BigDecimal("10"));
        }

        @Test
        @DisplayName("应创建最大值约束")
        void shouldCreateMaxValueConstraint() {
            PropertyConstraint constraint = PropertyConstraint.maxValue(new BigDecimal("100"), null);
            assertThat(constraint.getType()).isEqualTo(PropertyConstraint.ConstraintType.MAX_VALUE);
            assertThat(constraint.getValue()).isEqualTo(new BigDecimal("100"));
        }

        @Test
        @DisplayName("应创建正则表达式约束")
        void shouldCreatePatternConstraint() {
            PropertyConstraint constraint = PropertyConstraint.pattern("^[a-zA-Z]+$", "只能包含字母");
            assertThat(constraint.getType()).isEqualTo(PropertyConstraint.ConstraintType.PATTERN);
            assertThat(constraint.getValue()).isEqualTo("^[a-zA-Z]+$");
        }

        @Test
        @DisplayName("应创建枚举约束")
        void shouldCreateEnumConstraint() {
            List<String> enumValues = List.of("RED", "GREEN", "BLUE");
            PropertyConstraint constraint = PropertyConstraint.enumValues(enumValues, "必须是有效的颜色值");
            assertThat(constraint.getType()).isEqualTo(PropertyConstraint.ConstraintType.ENUM_VALUES);
            assertThat(constraint.getValue()).isEqualTo(enumValues);
        }
    }

    @Nested
    @DisplayName("validate - 约束验证")
    class ValidateTests {

        @Test
        @DisplayName("最小值约束应接受大于等于最小值的数值")
        void minValueShouldAcceptValidValue() {
            PropertyConstraint constraint = PropertyConstraint.minValue(new BigDecimal("10"), null);
            assertThat(constraint.validate(15)).isTrue();
            assertThat(constraint.validate(10)).isTrue();
            assertThat(constraint.validate(new BigDecimal("9.99"))).isFalse();
        }

        @Test
        @DisplayName("最大值约束应接受小于等于最大值的数值")
        void maxValueShouldAcceptValidValue() {
            PropertyConstraint constraint = PropertyConstraint.maxValue(new BigDecimal("100"), null);
            assertThat(constraint.validate(50)).isTrue();
            assertThat(constraint.validate(100)).isTrue();
            assertThat(constraint.validate(150)).isFalse();
        }

        @Test
        @DisplayName("正则约束应正确验证")
        void patternShouldValidateCorrectly() {
            PropertyConstraint constraint = PropertyConstraint.pattern("^[a-z]+$", null);
            assertThat(constraint.validate("hello")).isTrue();
            assertThat(constraint.validate("Hello")).isFalse();
        }

        @Test
        @DisplayName("枚举约束应正确验证")
        void enumValuesShouldValidateCorrectly() {
            PropertyConstraint constraint = PropertyConstraint.enumValues(List.of("RED", "GREEN", "BLUE"), null);
            assertThat(constraint.validate("RED")).isTrue();
            assertThat(constraint.validate("YELLOW")).isFalse();
        }

        @Test
        @DisplayName("约束应接受null值")
        void constraintShouldAcceptNullValue() {
            PropertyConstraint constraint = PropertyConstraint.minValue(new BigDecimal("10"), null);
            assertThat(constraint.validate(null)).isTrue();
        }

        @Test
        @DisplayName("Builder 直接构造 MIN_VALUE 传 Integer 不会抛 ClassCastException")
        void builderMinValueWithIntegerShouldNotThrow() {
            // 绕过工厂方法，模拟不安全构造
            PropertyConstraint constraint = PropertyConstraint.builder()
                    .type(PropertyConstraint.ConstraintType.MIN_VALUE)
                    .value(5) // Integer, 不是 BigDecimal
                    .build();

            // 修复后应安全处理，不会抛 ClassCastException
            assertThatCode(() -> constraint.validate(10))
                    .doesNotThrowAnyException();
            assertThat(constraint.validate(10)).isTrue();
            assertThat(constraint.validate(3)).isFalse();
        }

        @Test
        @DisplayName("Builder 直接构造 MAX_VALUE 传 Double 不会抛 ClassCastException")
        void builderMaxValueWithDoubleShouldNotThrow() {
            PropertyConstraint constraint = PropertyConstraint.builder()
                    .type(PropertyConstraint.ConstraintType.MAX_VALUE)
                    .value(100.5) // Double, 不是 BigDecimal
                    .build();

            assertThatCode(() -> constraint.validate(50))
                    .doesNotThrowAnyException();
            assertThat(constraint.validate(50)).isTrue();
            assertThat(constraint.validate(101)).isFalse();
        }

        @Test
        @DisplayName("Builder 直接构造 MIN_VALUE 传非数字值返回 false")
        void builderMinValueWithNonNumericShouldReturnFalse() {
            PropertyConstraint constraint = PropertyConstraint.builder()
                    .type(PropertyConstraint.ConstraintType.MIN_VALUE)
                    .value("not-a-number")
                    .build();

            // toBigDecimal 返回 null → validateMinValue 返回 false
            assertThat(constraint.validate(10)).isFalse();
        }

        @Test
        @DisplayName("Builder 直接构造 MIN_LENGTH 传 Double 安全转换为 int")
        void builderMinLengthWithDoubleShouldNotThrow() {
            PropertyConstraint constraint = PropertyConstraint.builder()
                    .type(PropertyConstraint.ConstraintType.MIN_LENGTH)
                    .value(5.9) // Double, 不是 Integer
                    .build();

            assertThatCode(() -> constraint.validate("hello world"))
                    .doesNotThrowAnyException();
            assertThat(constraint.validate("hello world")).isTrue(); // 11 chars >= 5
            assertThat(constraint.validate("hi")).isFalse(); // 2 chars < 5
        }

        @Test
        @DisplayName("PATTERN 约束存储非字符串值应返回 false 而非抛异常")
        void patternWithNonStringValueShouldNotThrow() {
            PropertyConstraint constraint = PropertyConstraint.builder()
                    .type(PropertyConstraint.ConstraintType.PATTERN)
                    .value(12345) // Integer, 不是 String
                    .build();

            assertThatCode(() -> constraint.validate("hello"))
                    .doesNotThrowAnyException();
            assertThat(constraint.validate("hello")).isFalse();
        }

        @Test
        @DisplayName("ENUM_VALUES 约束存储非列表值应返回 false 而非抛异常")
        void enumValuesWithNonListValueShouldNotThrow() {
            PropertyConstraint constraint = PropertyConstraint.builder()
                    .type(PropertyConstraint.ConstraintType.ENUM_VALUES)
                    .value("not-a-list") // String, 不是 List
                    .build();

            assertThatCode(() -> constraint.validate("RED"))
                    .doesNotThrowAnyException();
            assertThat(constraint.validate("RED")).isFalse();
        }

        @Test
        @DisplayName("Builder 直接构造 MIN_VALUE 传 Integer 边界值应正确比较")
        void builderMinValueWithIntegerBoundary() {
            PropertyConstraint constraint = PropertyConstraint.builder()
                    .type(PropertyConstraint.ConstraintType.MIN_VALUE)
                    .value(10)
                    .build();

            assertThat(constraint.validate(10)).isTrue();
            assertThat(constraint.validate(11)).isTrue();
            assertThat(constraint.validate(9)).isFalse();
        }

        @Test
        @DisplayName("Builder 直接构造 MIN_VALUE 传 Long 边界值应正确比较")
        void builderMinValueWithLongBoundary() {
            PropertyConstraint constraint = PropertyConstraint.builder()
                    .type(PropertyConstraint.ConstraintType.MIN_VALUE)
                    .value(10L)
                    .build();

            assertThat(constraint.validate(10L)).isTrue();
            assertThat(constraint.validate(11L)).isTrue();
            assertThat(constraint.validate(9L)).isFalse();
        }

        @Test
        @DisplayName("Builder 直接构造 MIN_VALUE 传 Double 边界值应正确比较")
        void builderMinValueWithDoubleBoundary() {
            PropertyConstraint constraint = PropertyConstraint.builder()
                    .type(PropertyConstraint.ConstraintType.MIN_VALUE)
                    .value(10.0)
                    .build();

            assertThat(constraint.validate(10.0)).isTrue();
            assertThat(constraint.validate(10.1)).isTrue();
            assertThat(constraint.validate(9.9)).isFalse();
        }

        @Test
        @DisplayName("Builder 直接构造 MIN_VALUE 传数值字符串应正确解析并比较")
        void builderMinValueWithNumericStringBoundary() {
            PropertyConstraint constraint = PropertyConstraint.builder()
                    .type(PropertyConstraint.ConstraintType.MIN_VALUE)
                    .value("10")
                    .build();

            assertThat(constraint.validate(10)).isTrue();
            assertThat(constraint.validate(11)).isTrue();
            assertThat(constraint.validate(9)).isFalse();
        }

        @Test
        @DisplayName("Builder 直接构造 MIN_VALUE 传非法字符串应返回 false")
        void builderMinValueWithInvalidStringReturnsFalse() {
            PropertyConstraint constraint = PropertyConstraint.builder()
                    .type(PropertyConstraint.ConstraintType.MIN_VALUE)
                    .value("not-a-number")
                    .build();

            assertThatCode(() -> constraint.validate(10))
                    .doesNotThrowAnyException();
            assertThat(constraint.validate(10)).isFalse();
        }
    }
}

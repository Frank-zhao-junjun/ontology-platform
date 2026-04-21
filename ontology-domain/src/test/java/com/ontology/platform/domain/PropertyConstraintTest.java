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
    }
}

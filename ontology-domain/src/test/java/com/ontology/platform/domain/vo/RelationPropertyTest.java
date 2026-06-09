package com.ontology.platform.domain.vo;

import com.ontology.platform.common.enums.PropertyDataType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RelationProperty值对象单元测试
 */
@DisplayName("RelationProperty值对象测试")
class RelationPropertyTest {

    @Nested
    @DisplayName("create方法测试")
    class CreateTest {

        @Test
        @DisplayName("应该创建基本关系属性")
        void shouldCreateBasicProperty() {
            RelationProperty property = RelationProperty.create(
                    "WEIGHT",
                    "权重",
                    PropertyDataType.DECIMAL,
                    false
            );

            assertNotNull(property);
            assertEquals("WEIGHT", property.getName());
            assertEquals("权重", property.getDisplayName());
            assertEquals(PropertyDataType.DECIMAL, property.getDataType());
            assertFalse(property.isRequired());
            assertNull(property.getDefaultValue());
        }

        @Test
        @DisplayName("应该创建必填属性")
        void shouldCreateRequiredProperty() {
            RelationProperty property = RelationProperty.create(
                    "START_DATE",
                    "开始日期",
                    PropertyDataType.DATE,
                    true
            );

            assertTrue(property.isRequired());
        }
    }

    @Nested
    @DisplayName("validateValue方法测试")
    class ValidateValueTest {

        @Test
        @DisplayName("必填属性不应该接受null值")
        void shouldNotAcceptNullForRequiredProperty() {
            RelationProperty property = RelationProperty.create(
                    "START_DATE", "开始日期",
                    PropertyDataType.DATE, true
            );

            assertFalse(property.validateValue(null));
        }

        @Test
        @DisplayName("非必填属性应该接受null值")
        void shouldAcceptNullForOptionalProperty() {
            RelationProperty property = RelationProperty.create(
                    "END_DATE", "结束日期",
                    PropertyDataType.DATE, false
            );

            assertTrue(property.validateValue(null));
        }

        @ParameterizedTest
        @EnumSource(PropertyDataType.class)
        @DisplayName("应该验证各类型数据的有效性")
        void shouldValidateDataTypes(PropertyDataType dataType) {
            RelationProperty property = RelationProperty.create(
                    "TEST", "测试",
                    dataType, false
            );

            // 不同类型应该有不同的验证行为
            assertDoesNotThrow(() -> property.validateValue(getValidValueForType(dataType)));
        }

        private Object getValidValueForType(PropertyDataType type) {
            return switch (type) {
                case STRING, TEXT -> "test string";
                case INTEGER -> 42L;
                case DECIMAL -> 3.14;
                case BOOLEAN -> true;
                case DATE, DATETIME, UUID, ENUM -> "2024-01-01";
                case ARRAY -> new Object[]{};
                case OBJECT, JSON -> new Object();
            };
        }
    }

    @Nested
    @DisplayName("类型匹配测试")
    class TypeMatchingTest {

        @Test
        @DisplayName("STRING类型应该接受字符串")
        void stringShouldAcceptString() {
            RelationProperty property = RelationProperty.create(
                    "NAME", "名称",
                    PropertyDataType.STRING, false
            );

            assertTrue(property.validateValue("test"));
        }

        @Test
        @DisplayName("INTEGER类型应该接受整数")
        void integerShouldAcceptInteger() {
            RelationProperty property = RelationProperty.create(
                    "COUNT", "数量",
                    PropertyDataType.INTEGER, false
            );

            assertTrue(property.validateValue(100));
        }

        @Test
        @DisplayName("DECIMAL类型应该接受浮点数")
        void decimalShouldAcceptDecimal() {
            RelationProperty property = RelationProperty.create(
                    "WEIGHT", "权重",
                    PropertyDataType.DECIMAL, false
            );

            assertTrue(property.validateValue(3.14));
            assertTrue(property.validateValue(100.0));
        }

        @Test
        @DisplayName("BOOLEAN类型应该接受布尔值")
        void booleanShouldAcceptBoolean() {
            RelationProperty property = RelationProperty.create(
                    "IS_ACTIVE", "是否激活",
                    PropertyDataType.BOOLEAN, false
            );

            assertTrue(property.validateValue(true));
            assertTrue(property.validateValue(false));
        }
    }
}

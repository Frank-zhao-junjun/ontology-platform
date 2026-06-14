package com.ontology.platform.domain;

import com.ontology.platform.common.enums.PropertyDataType;
import com.ontology.platform.domain.factory.TestFactory;
import com.ontology.platform.domain.vo.Property;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Property值对象测试
 * 
 * 测试范围：
 * - 属性创建
 * - 值校验（各种数据类型）
 * - 信息更新
 * - 排序顺序更新
 */
@DisplayName("Property值对象测试")
class PropertyTest {

    private static final String TEST_OBJECT_TYPE_ID = UUID.randomUUID().toString();
    private static final String TEST_NAME = "test_property";
    private static final String TEST_DISPLAY_NAME = "测试属性";
    private static final String TEST_DESCRIPTION = "这是一个测试属性";

    @Nested
    @DisplayName("create - 创建属性")
    class CreateTests {

        @Test
        @DisplayName("应创建带有默认值的属性")
        void shouldCreatePropertyWithDefaults() {
            // Act
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.STRING, false);

            // Assert
            assertThat(property.getId()).isNotNull().isNotEmpty();
            assertThat(UUID.fromString(property.getId())).isNotNull();
            assertThat(property.getObjectTypeId()).isEqualTo(TEST_OBJECT_TYPE_ID);
            assertThat(property.getName()).isEqualTo(TEST_NAME);
            assertThat(property.getDisplayName()).isEqualTo(TEST_DISPLAY_NAME);
            assertThat(property.getDescription()).isEqualTo(TEST_DESCRIPTION);
            assertThat(property.getDataType()).isEqualTo(PropertyDataType.STRING);
            assertThat(property.isRequired()).isFalse();
            assertThat(property.isUnique()).isFalse();
            assertThat(property.isSearchable()).isTrue();
            assertThat(property.isSortable()).isTrue();
            assertThat(property.isComputed()).isFalse();
            assertThat(property.getSortOrder()).isZero();
            assertThat(property.getDefaultValue()).isNull();
            assertThat(property.getCreatedAt()).isNotNull().isBeforeOrEqualTo(Instant.now());
            assertThat(property.getUpdatedAt()).isNotNull().isBeforeOrEqualTo(Instant.now());
        }

        @Test
        @DisplayName("应创建必填属性")
        void shouldCreateRequiredProperty() {
            // Act
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.INTEGER, true);

            // Assert
            assertThat(property.isRequired()).isTrue();
            assertThat(property.getDataType()).isEqualTo(PropertyDataType.INTEGER);
        }

        @ParameterizedTest
        @DisplayName("应创建所有数据类型属性")
        @EnumSource(PropertyDataType.class)
        void shouldCreatePropertyWithAllDataTypes(PropertyDataType dataType) {
            // Act
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    dataType, false);

            // Assert
            assertThat(property.getDataType()).isEqualTo(dataType);
            assertThat(property.getId()).isNotNull();
        }
    }

    @Nested
    @DisplayName("validateValue - 值校验")
    class ValidateValueTests {

        // STRING类型校验
        @Test
        @DisplayName("STRING类型应接受字符串值")
        void stringShouldAcceptStringValue() {
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.STRING, false);

            assertThat(property.validateValue("test string")).isTrue();
            assertThat(property.validateValue("")).isTrue();
        }

        @Test
        @DisplayName("STRING类型应拒绝非字符串值")
        void stringShouldRejectNonStringValue() {
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.STRING, false);

            assertThat(property.validateValue(123)).isFalse();
            assertThat(property.validateValue(true)).isFalse();
            assertThat(property.validateValue(45.67)).isFalse();
        }

        // TEXT类型校验
        @Test
        @DisplayName("TEXT类型应接受长文本")
        void textShouldAcceptLongText() {
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.TEXT, false);

            String longText = "A".repeat(10000);
            assertThat(property.validateValue(longText)).isTrue();
        }

        // INTEGER类型校验
        @Test
        @DisplayName("INTEGER类型应接受整数值")
        void integerShouldAcceptIntegerValues() {
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.INTEGER, false);

            assertThat(property.validateValue(42)).isTrue();
            assertThat(property.validateValue(-100L)).isTrue();
            assertThat(property.validateValue(0)).isTrue();
            assertThat(property.validateValue(Integer.MAX_VALUE)).isTrue();
        }

        @Test
        @DisplayName("INTEGER类型应拒绝非整数值")
        void integerShouldRejectNonIntegerValues() {
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.INTEGER, false);

            assertThat(property.validateValue(3.14)).isFalse();
            assertThat(property.validateValue("42")).isFalse();
            assertThat(property.validateValue(true)).isFalse();
        }

        // DECIMAL类型校验
        @Test
        @DisplayName("DECIMAL类型应接受浮点数值")
        void decimalShouldAcceptDecimalValues() {
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.DECIMAL, false);

            assertThat(property.validateValue(3.14)).isTrue();
            assertThat(property.validateValue(100.5f)).isTrue();
            assertThat(property.validateValue(-0.001)).isTrue();
        }

        @Test
        @DisplayName("DECIMAL类型应拒绝非数值")
        void decimalShouldRejectNonDecimalValues() {
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.DECIMAL, false);

            assertThat(property.validateValue("3.14")).isFalse();
            assertThat(property.validateValue(true)).isFalse();
        }

        // BOOLEAN类型校验
        @Test
        @DisplayName("BOOLEAN类型应接受布尔值")
        void booleanShouldAcceptBooleanValues() {
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.BOOLEAN, false);

            assertThat(property.validateValue(true)).isTrue();
            assertThat(property.validateValue(false)).isTrue();
        }

        @Test
        @DisplayName("BOOLEAN类型应拒绝非布尔值")
        void booleanShouldRejectNonBooleanValues() {
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.BOOLEAN, false);

            assertThat(property.validateValue(1)).isFalse();
            assertThat(property.validateValue("true")).isFalse();
        }

        // DATE和DATETIME类型校验
        @Test
        @DisplayName("DATE类型应接受日期字符串和Instant")
        void dateShouldAcceptDateValues() {
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.DATE, false);

            assertThat(property.validateValue("2024-01-15")).isTrue();
            assertThat(property.validateValue(Instant.now())).isTrue();
        }

        @Test
        @DisplayName("DATETIME类型应接受日期时间字符串和Instant")
        void datetimeShouldAcceptDateTimeValues() {
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.DATETIME, false);

            assertThat(property.validateValue("2024-01-15T10:30:00")).isTrue();
            assertThat(property.validateValue(Instant.now())).isTrue();
        }

        // UUID类型校验
        @Test
        @DisplayName("UUID类型应接受UUID字符串")
        void uuidShouldAcceptUuidString() {
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.UUID, false);

            assertThat(property.validateValue("550e8400-e29b-41d4-a716-446655440000")).isTrue();
        }

        // 复杂类型校验
        @Test
        @DisplayName("ENUM类型应接受任何值")
        void enumShouldAcceptAnyValue() {
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.ENUM, false);

            assertThat(property.validateValue("value1")).isTrue();
            assertThat(property.validateValue(123)).isTrue();
        }

        @Test
        @DisplayName("ARRAY类型应接受任何值")
        void arrayShouldAcceptAnyValue() {
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.ARRAY, false);

            assertThat(property.validateValue(new Object[]{})).isTrue();
            assertThat(property.validateValue("[]")).isTrue();
        }

        @Test
        @DisplayName("OBJECT类型应接受任何值")
        void objectShouldAcceptAnyValue() {
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.OBJECT, false);

            assertThat(property.validateValue(new Object())).isTrue();
        }

        @Test
        @DisplayName("JSON类型应接受任何值")
        void jsonShouldAcceptAnyValue() {
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.JSON, false);

            assertThat(property.validateValue("{\"key\": \"value\"}")).isTrue();
            assertThat(property.validateValue(new Object())).isTrue();
        }

        // null值校验
        @Test
        @DisplayName("非必填属性应接受null值")
        void nonRequiredShouldAcceptNullValue() {
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.STRING, false);

            assertThat(property.validateValue(null)).isTrue();
        }

        @Test
        @DisplayName("必填属性应拒绝null值")
        void requiredShouldRejectNullValue() {
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.STRING, true);

            assertThat(property.validateValue(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("update - 更新属性信息")
    class UpdateTests {

        @Test
        @DisplayName("应成功更新显示名称、描述和必填状态")
        void shouldUpdateAllFields() {
            // Arrange
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.STRING, false);
            String newDisplayName = "新显示名称";
            String newDescription = "新描述";
            boolean newIsRequired = true;

            // Act
            property.update(newDisplayName, newDescription, newIsRequired);

            // Assert
            assertThat(property.getDisplayName()).isEqualTo(newDisplayName);
            assertThat(property.getDescription()).isEqualTo(newDescription);
            assertThat(property.isRequired()).isEqualTo(newIsRequired);
            assertThat(property.getUpdatedAt()).isAfterOrEqualTo(property.getCreatedAt());
        }

        @Test
        @DisplayName("应成功切换必填状态")
        void shouldToggleRequiredStatus() {
            // Arrange
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.STRING, true);
            assertThat(property.isRequired()).isTrue();

            // Act
            property.update(property.getDisplayName(), property.getDescription(), false);

            // Assert
            assertThat(property.isRequired()).isFalse();
        }
    }

    @Nested
    @DisplayName("updateSortOrder - 更新排序顺序")
    class UpdateSortOrderTests {

        @Test
        @DisplayName("应成功更新排序顺序")
        void shouldUpdateSortOrder() {
            // Arrange
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.STRING, false);
            Instant originalUpdatedAt = property.getUpdatedAt();

            // Act
            property.updateSortOrder(10);

            // Assert
            assertThat(property.getSortOrder()).isEqualTo(10);
            assertThat(property.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("应成功设置排序顺序为负数")
        void shouldAllowNegativeSortOrder() {
            // Arrange
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.STRING, false);

            // Act
            property.updateSortOrder(-1);

            // Assert
            assertThat(property.getSortOrder()).isEqualTo(-1);
        }

        @Test
        @DisplayName("应成功设置排序顺序为零")
        void shouldAllowZeroSortOrder() {
            // Arrange
            Property property = Property.create(
                    TEST_OBJECT_TYPE_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    PropertyDataType.STRING, false);
            property.updateSortOrder(5);

            // Act
            property.updateSortOrder(0);

            // Assert
            assertThat(property.getSortOrder()).isZero();
        }
    }
}

package com.ontology.platform.domain.entity;

import com.ontology.platform.domain.vo.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * ObjectInstance聚合根测试
 * 
 * 测试范围：
 * - 实例创建
 * - 属性操作（设置/获取/移除）
 * - 状态管理
 * - 业务规则验证
 */
@DisplayName("ObjectInstance聚合根测试")
class ObjectInstanceTest {

    private static final String TEST_ONTOLOGY_ID = "test-ontology-id";
    private static final String TEST_OBJECT_TYPE_ID = "test-object-type-id";
    private static final String TEST_OBJECT_TYPE_NAME = "customer";
    private static final String TEST_PRIMARY_KEY_VALUE = "C001";
    private static final String TEST_USER = "test-user";

    @Nested
    @DisplayName("create - 创建实例")
    class CreateTests {

        @Test
        @DisplayName("应创建带有默认值的实例")
        void shouldCreateInstanceWithDefaults() {
            // Arrange
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", "张三");
            properties.put("email", "zhangsan@example.com");

            // Act
            ObjectInstance instance = ObjectInstance.create(
                    TEST_ONTOLOGY_ID,
                    TEST_OBJECT_TYPE_ID,
                    TEST_OBJECT_TYPE_NAME,
                    TEST_PRIMARY_KEY_VALUE,
                    properties,
                    TEST_USER
            );

            // Assert
            assertThat(instance.getId()).isNotNull().isNotEmpty();
            assertThat(UUID.fromString(instance.getId())).isNotNull();
            assertThat(instance.getOntologyId()).isEqualTo(TEST_ONTOLOGY_ID);
            assertThat(instance.getObjectTypeId()).isEqualTo(TEST_OBJECT_TYPE_ID);
            assertThat(instance.getObjectTypeName()).isEqualTo(TEST_OBJECT_TYPE_NAME);
            assertThat(instance.getPrimaryKeyValue()).isEqualTo(TEST_PRIMARY_KEY_VALUE);
            assertThat(instance.getStatus()).isEqualTo("active");
            assertThat(instance.getVersion()).isEqualTo(1);
            assertThat(instance.getCreatedBy()).isEqualTo(TEST_USER);
            assertThat(instance.getCreatedAt()).isNotNull().isBeforeOrEqualTo(Instant.now());
            assertThat(instance.getUpdatedAt()).isNotNull().isBeforeOrEqualTo(Instant.now());
            assertThat(instance.getProperties()).hasSize(2);
            assertThat(instance.getProperty("name")).isEqualTo("张三");
            assertThat(instance.getProperty("email")).isEqualTo("zhangsan@example.com");
        }

        @Test
        @DisplayName("应创建带有空属性的实例")
        void shouldCreateInstanceWithEmptyProperties() {
            // Act
            ObjectInstance instance = ObjectInstance.create(
                    TEST_ONTOLOGY_ID,
                    TEST_OBJECT_TYPE_ID,
                    TEST_OBJECT_TYPE_NAME,
                    TEST_PRIMARY_KEY_VALUE,
                    null,
                    TEST_USER
            );

            // Assert
            assertThat(instance.getProperties()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("应创建带有唯一ID的实例")
        void shouldCreateInstanceWithUniqueId() {
            // Act
            ObjectInstance instance1 = ObjectInstance.create(
                    TEST_ONTOLOGY_ID, TEST_OBJECT_TYPE_ID, TEST_OBJECT_TYPE_NAME,
                    TEST_PRIMARY_KEY_VALUE, null, TEST_USER
            );
            ObjectInstance instance2 = ObjectInstance.create(
                    TEST_ONTOLOGY_ID, TEST_OBJECT_TYPE_ID, TEST_OBJECT_TYPE_NAME,
                    TEST_PRIMARY_KEY_VALUE, null, TEST_USER
            );

            // Assert
            assertThat(instance1.getId()).isNotEqualTo(instance2.getId());
        }
    }

    @Nested
    @DisplayName("getProperty - 获取属性")
    class GetPropertyTests {

        @Test
        @DisplayName("应返回存在的属性值")
        void shouldReturnExistingPropertyValue() {
            // Arrange
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", "张三");
            ObjectInstance instance = ObjectInstance.create(
                    TEST_ONTOLOGY_ID, TEST_OBJECT_TYPE_ID, TEST_OBJECT_TYPE_NAME,
                    TEST_PRIMARY_KEY_VALUE, properties, TEST_USER
            );

            // Act & Assert
            assertThat(instance.getProperty("name")).isEqualTo("张三");
            assertThat(instance.getProperty("age")).isNull();
        }

        @Test
        @DisplayName("应正确判断属性是否存在")
        void shouldCheckPropertyExists() {
            // Arrange
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", "张三");
            ObjectInstance instance = ObjectInstance.create(
                    TEST_ONTOLOGY_ID, TEST_OBJECT_TYPE_ID, TEST_OBJECT_TYPE_NAME,
                    TEST_PRIMARY_KEY_VALUE, properties, TEST_USER
            );

            // Act & Assert
            assertThat(instance.hasProperty("name")).isTrue();
            assertThat(instance.hasProperty("age")).isFalse();
        }
    }

    @Nested
    @DisplayName("setProperty - 设置属性")
    class SetPropertyTests {

        @Test
        @DisplayName("应成功设置属性")
        void shouldSetProperty() {
            // Arrange
            ObjectInstance instance = ObjectInstance.create(
                    TEST_ONTOLOGY_ID, TEST_OBJECT_TYPE_ID, TEST_OBJECT_TYPE_NAME,
                    TEST_PRIMARY_KEY_VALUE, null, TEST_USER
            );
            Instant beforeUpdate = instance.getUpdatedAt();

            // Act
            instance.setProperty("name", "李四");
            instance.setProperty("age", 30);

            // Assert
            assertThat(instance.getProperty("name")).isEqualTo("李四");
            assertThat(instance.getProperty("age")).isEqualTo(30);
            assertThat(instance.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
        }

        @Test
        @DisplayName("应覆盖已存在的属性")
        void shouldOverrideExistingProperty() {
            // Arrange
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", "张三");
            ObjectInstance instance = ObjectInstance.create(
                    TEST_ONTOLOGY_ID, TEST_OBJECT_TYPE_ID, TEST_OBJECT_TYPE_NAME,
                    TEST_PRIMARY_KEY_VALUE, properties, TEST_USER
            );

            // Act
            instance.setProperty("name", "王五");

            // Assert
            assertThat(instance.getProperty("name")).isEqualTo("王五");
            assertThat(instance.getProperties()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("removeProperty - 移除属性")
    class RemovePropertyTests {

        @Test
        @DisplayName("应成功移除属性")
        void shouldRemoveProperty() {
            // Arrange
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", "张三");
            properties.put("email", "zhangsan@example.com");
            ObjectInstance instance = ObjectInstance.create(
                    TEST_ONTOLOGY_ID, TEST_OBJECT_TYPE_ID, TEST_OBJECT_TYPE_NAME,
                    TEST_PRIMARY_KEY_VALUE, properties, TEST_USER
            );

            // Act
            instance.removeProperty("email");

            // Assert
            assertThat(instance.hasProperty("email")).isFalse();
            assertThat(instance.hasProperty("name")).isTrue();
            assertThat(instance.getProperties()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("update - 更新实例")
    class UpdateTests {

        @Test
        @DisplayName("应成功更新属性")
        void shouldUpdateProperties() {
            // Arrange
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", "张三");
            ObjectInstance instance = ObjectInstance.create(
                    TEST_ONTOLOGY_ID, TEST_OBJECT_TYPE_ID, TEST_OBJECT_TYPE_NAME,
                    TEST_PRIMARY_KEY_VALUE, properties, TEST_USER
            );
            int originalVersion = instance.getVersion();

            // Act
            Map<String, Object> newProperties = new HashMap<>();
            newProperties.put("name", "李四");
            newProperties.put("age", 30);
            instance.update(newProperties);

            // Assert
            assertThat(instance.getProperty("name")).isEqualTo("李四");
            assertThat(instance.getProperty("age")).isEqualTo(30);
            assertThat(instance.getVersion()).isEqualTo(originalVersion + 1);
        }

        @Test
        @DisplayName("应保留原有属性并添加新属性")
        void shouldPreserveExistingAndAddNew() {
            // Arrange
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", "张三");
            ObjectInstance instance = ObjectInstance.create(
                    TEST_ONTOLOGY_ID, TEST_OBJECT_TYPE_ID, TEST_OBJECT_TYPE_NAME,
                    TEST_PRIMARY_KEY_VALUE, properties, TEST_USER
            );

            // Act
            Map<String, Object> newProperties = new HashMap<>();
            newProperties.put("email", "zhangsan@example.com");
            instance.update(newProperties);

            // Assert
            assertThat(instance.getProperties()).hasSize(2);
            assertThat(instance.getProperty("name")).isEqualTo("张三");
            assertThat(instance.getProperty("email")).isEqualTo("zhangsan@example.com");
        }
    }

    @Nested
    @DisplayName("状态管理")
    class StatusManagementTests {

        @Test
        @DisplayName("新实例应处于激活状态")
        void shouldBeActiveInitially() {
            // Arrange & Act
            ObjectInstance instance = ObjectInstance.create(
                    TEST_ONTOLOGY_ID, TEST_OBJECT_TYPE_ID, TEST_OBJECT_TYPE_NAME,
                    TEST_PRIMARY_KEY_VALUE, null, TEST_USER
            );

            // Assert
            assertThat(instance.isActive()).isTrue();
            assertThat(instance.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("应成功标记为删除")
        void shouldMarkAsDeleted() {
            // Arrange
            ObjectInstance instance = ObjectInstance.create(
                    TEST_ONTOLOGY_ID, TEST_OBJECT_TYPE_ID, TEST_OBJECT_TYPE_NAME,
                    TEST_PRIMARY_KEY_VALUE, null, TEST_USER
            );

            // Act
            instance.markAsDeleted();

            // Assert
            assertThat(instance.isDeleted()).isTrue();
            assertThat(instance.isActive()).isFalse();
            assertThat(instance.getStatus()).isEqualTo("deleted");
        }

        @Test
        @DisplayName("应成功激活实例")
        void shouldActivateInstance() {
            // Arrange
            ObjectInstance instance = ObjectInstance.create(
                    TEST_ONTOLOGY_ID, TEST_OBJECT_TYPE_ID, TEST_OBJECT_TYPE_NAME,
                    TEST_PRIMARY_KEY_VALUE, null, TEST_USER
            );
            instance.deactivate();

            // Act
            instance.activate();

            // Assert
            assertThat(instance.isActive()).isTrue();
            assertThat(instance.getStatus()).isEqualTo("active");
        }

        @Test
        @DisplayName("应成功停用实例")
        void shouldDeactivateInstance() {
            // Arrange
            ObjectInstance instance = ObjectInstance.create(
                    TEST_ONTOLOGY_ID, TEST_OBJECT_TYPE_ID, TEST_OBJECT_TYPE_NAME,
                    TEST_PRIMARY_KEY_VALUE, null, TEST_USER
            );

            // Act
            instance.deactivate();

            // Assert
            assertThat(instance.isActive()).isFalse();
            assertThat(instance.getStatus()).isEqualTo("inactive");
        }
    }
}

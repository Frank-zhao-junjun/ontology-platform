package com.ontology.platform.domain;

import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.entity.Relation;
import com.ontology.platform.domain.entity.Ontology;
import com.ontology.platform.domain.factory.TestFactory;
import com.ontology.platform.domain.vo.Property;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * ObjectType实体测试
 * 
 * 测试范围：
 * - 对象类型创建
 * - 属性管理（添加/移除）
 * - 关系管理（添加/移除）
 * - 实例计数更新
 * - 继承关系设置
 * - 信息更新
 */
@DisplayName("ObjectType实体测试")
class ObjectTypeTest {

    private static final String TEST_ONTOLOGY_ID = UUID.randomUUID().toString();
    private static final String TEST_NAME = "person";
    private static final String TEST_DISPLAY_NAME = "人员";
    private static final String TEST_DESCRIPTION = "人员对象类型";
    private static final String TEST_PRIMARY_KEY = "id";

    @Nested
    @DisplayName("create - 创建对象类型")
    class CreateTests {

        @Test
        @DisplayName("应创建带有默认值对象类型")
        void shouldCreateObjectTypeWithDefaults() {
            // Act
            ObjectType objectType = ObjectType.create(
                    TEST_ONTOLOGY_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_PRIMARY_KEY);

            // Assert
            assertThat(objectType.getId()).isNotNull().isNotEmpty();
            assertThat(UUID.fromString(objectType.getId())).isNotNull();
            assertThat(objectType.getOntologyId()).isEqualTo(TEST_ONTOLOGY_ID);
            assertThat(objectType.getName()).isEqualTo(TEST_NAME);
            assertThat(objectType.getDisplayName()).isEqualTo(TEST_DISPLAY_NAME);
            assertThat(objectType.getDescription()).isEqualTo(TEST_DESCRIPTION);
            assertThat(objectType.getPrimaryKey()).isEqualTo(TEST_PRIMARY_KEY);
            assertThat(objectType.getInstanceCount()).isZero();
            assertThat(objectType.getCreatedAt()).isNotNull().isBeforeOrEqualTo(Instant.now());
            assertThat(objectType.getUpdatedAt()).isNotNull().isBeforeOrEqualTo(Instant.now());
            assertThat(objectType.getProperties()).isEmpty();
            assertThat(objectType.getRelations()).isEmpty();
            assertThat(objectType.getInterfaceNames()).isEmpty();
            assertThat(objectType.getParentId()).isNull();
        }

        @Test
        @DisplayName("应创建带有唯一ID的对象类型")
        void shouldCreateObjectTypeWithUniqueId() {
            // Act
            ObjectType objectType1 = ObjectType.create(
                    TEST_ONTOLOGY_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_PRIMARY_KEY);
            ObjectType objectType2 = ObjectType.create(
                    TEST_ONTOLOGY_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_PRIMARY_KEY);

            // Assert
            assertThat(objectType1.getId()).isNotEqualTo(objectType2.getId());
        }
    }

    @Nested
    @DisplayName("addProperty - 添加属性")
    class AddPropertyTests {

        @Test
        @DisplayName("应成功添加属性")
        void shouldAddProperty() {
            // Arrange
            ObjectType objectType = ObjectType.create(
                    TEST_ONTOLOGY_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_PRIMARY_KEY);
            Property property = TestFactory.createProperty(
                    UUID.randomUUID().toString(), objectType.getId(), "name", 
                    com.ontology.platform.common.enums.PropertyDataType.STRING);

            // Act
            objectType.addProperty(property);

            // Assert
            assertThat(objectType.getProperties()).hasSize(1);
            assertThat(objectType.getProperties()).contains(property);
            assertThat(objectType.getUpdatedAt()).isAfterOrEqualTo(objectType.getCreatedAt());
        }

        @Test
        @DisplayName("应成功添加多个属性")
        void shouldAddMultipleProperties() {
            // Arrange
            ObjectType objectType = ObjectType.create(
                    TEST_ONTOLOGY_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_PRIMARY_KEY);
            Property property1 = TestFactory.createProperty(
                    UUID.randomUUID().toString(), objectType.getId(), "name",
                    com.ontology.platform.common.enums.PropertyDataType.STRING);
            Property property2 = TestFactory.createProperty(
                    UUID.randomUUID().toString(), objectType.getId(), "age",
                    com.ontology.platform.common.enums.PropertyDataType.INTEGER);

            // Act
            objectType.addProperty(property1);
            objectType.addProperty(property2);

            // Assert
            assertThat(objectType.getProperties()).hasSize(2);
        }

        @Test
        @DisplayName("添加属性应更新updatedAt时间戳")
        void shouldUpdateTimestampWhenAddingProperty() {
            // Arrange
            ObjectType objectType = ObjectType.create(
                    TEST_ONTOLOGY_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_PRIMARY_KEY);
            Instant originalUpdatedAt = objectType.getUpdatedAt();
            Property property = TestFactory.createProperty(
                    UUID.randomUUID().toString(), objectType.getId(), "name",
                    com.ontology.platform.common.enums.PropertyDataType.STRING);

            // Act
            objectType.addProperty(property);

            // Assert
            assertThat(objectType.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }
    }

    @Nested
    @DisplayName("removeProperty - 移除属性")
    class RemovePropertyTests {

        @Test
        @DisplayName("应成功移除已存在的属性")
        void shouldRemoveExistingProperty() {
            // Arrange
            ObjectType objectType = ObjectType.create(
                    TEST_ONTOLOGY_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_PRIMARY_KEY);
            Property property = TestFactory.createProperty(
                    UUID.randomUUID().toString(), objectType.getId(), "name",
                    com.ontology.platform.common.enums.PropertyDataType.STRING);
            objectType.addProperty(property);
            assertThat(objectType.getProperties()).hasSize(1);

            // Act
            objectType.removeProperty(property.getId());

            // Assert
            assertThat(objectType.getProperties()).isEmpty();
        }

        @Test
        @DisplayName("移除不存在的属性应不改变列表")
        void shouldNotChangeWhenRemovingNonExistingProperty() {
            // Arrange
            ObjectType objectType = ObjectType.create(
                    TEST_ONTOLOGY_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_PRIMARY_KEY);
            Property property = TestFactory.createProperty(
                    UUID.randomUUID().toString(), objectType.getId(), "name",
                    com.ontology.platform.common.enums.PropertyDataType.STRING);
            objectType.addProperty(property);

            // Act
            objectType.removeProperty("non-existing-id");

            // Assert
            assertThat(objectType.getProperties()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("addRelation - 添加关系")
    class AddRelationTests {

        @Test
        @DisplayName("应成功添加关系")
        void shouldAddRelation() {
            // Arrange
            ObjectType objectType = ObjectType.create(
                    TEST_ONTOLOGY_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_PRIMARY_KEY);
            Relation relation = TestFactory.createOneToManyRelation(
                    UUID.randomUUID().toString(), TEST_ONTOLOGY_ID, 
                    objectType.getId(), "target-id", "has_manager");

            // Act
            objectType.addRelation(relation);

            // Assert
            assertThat(objectType.getRelations()).hasSize(1);
            assertThat(objectType.getRelations()).contains(relation);
        }

        @Test
        @DisplayName("应成功添加多个关系")
        void shouldAddMultipleRelations() {
            // Arrange
            ObjectType objectType = ObjectType.create(
                    TEST_ONTOLOGY_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_PRIMARY_KEY);
            Relation relation1 = TestFactory.createOneToManyRelation(
                    UUID.randomUUID().toString(), TEST_ONTOLOGY_ID, 
                    objectType.getId(), "target-id-1", "has_friend");
            Relation relation2 = TestFactory.createOneToManyRelation(
                    UUID.randomUUID().toString(), TEST_ONTOLOGY_ID, 
                    objectType.getId(), "target-id-2", "has_colleague");

            // Act
            objectType.addRelation(relation1);
            objectType.addRelation(relation2);

            // Assert
            assertThat(objectType.getRelations()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("removeRelation - 移除关系")
    class RemoveRelationTests {

        @Test
        @DisplayName("应成功移除已存在的关系")
        void shouldRemoveExistingRelation() {
            // Arrange
            ObjectType objectType = ObjectType.create(
                    TEST_ONTOLOGY_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_PRIMARY_KEY);
            Relation relation = TestFactory.createOneToManyRelation(
                    UUID.randomUUID().toString(), TEST_ONTOLOGY_ID, 
                    objectType.getId(), "target-id", "has_manager");
            objectType.addRelation(relation);
            assertThat(objectType.getRelations()).hasSize(1);

            // Act
            objectType.removeRelation(relation.getId());

            // Assert
            assertThat(objectType.getRelations()).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateInstanceCount - 更新实例计数")
    class UpdateInstanceCountTests {

        @Test
        @DisplayName("应成功增加实例计数")
        void shouldIncrementInstanceCount() {
            // Arrange
            ObjectType objectType = ObjectType.create(
                    TEST_ONTOLOGY_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_PRIMARY_KEY);
            assertThat(objectType.getInstanceCount()).isZero();

            // Act
            objectType.updateInstanceCount(5);

            // Assert
            assertThat(objectType.getInstanceCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("应成功减少实例计数")
        void shouldDecrementInstanceCount() {
            // Arrange
            ObjectType objectType = TestFactory.createObjectType(
                    UUID.randomUUID().toString(), TEST_ONTOLOGY_ID, TEST_NAME);
            objectType.updateInstanceCount(10);

            // Act
            objectType.updateInstanceCount(-3);

            // Assert
            assertThat(objectType.getInstanceCount()).isEqualTo(7);
        }

        @Test
        @DisplayName("减少实例计数不应小于零")
        void shouldNotGoBelowZero() {
            // Arrange
            ObjectType objectType = TestFactory.createObjectType(
                    UUID.randomUUID().toString(), TEST_ONTOLOGY_ID, TEST_NAME);
            objectType.updateInstanceCount(5);

            // Act
            objectType.updateInstanceCount(-10);

            // Assert
            assertThat(objectType.getInstanceCount()).isZero();
        }
    }

    @Nested
    @DisplayName("setParent - 设置父类型")
    class SetParentTests {

        @Test
        @DisplayName("应成功设置父类型ID")
        void shouldSetParentId() {
            // Arrange
            ObjectType objectType = ObjectType.create(
                    TEST_ONTOLOGY_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_PRIMARY_KEY);
            String parentId = UUID.randomUUID().toString();

            // Act
            objectType.setParent(parentId);

            // Assert
            assertThat(objectType.getParentId()).isEqualTo(parentId);
        }

        @Test
        @DisplayName("应成功清除父类型ID")
        void shouldClearParentId() {
            // Arrange
            ObjectType objectType = ObjectType.create(
                    TEST_ONTOLOGY_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_PRIMARY_KEY);
            String parentId = UUID.randomUUID().toString();
            objectType.setParent(parentId);

            // Act
            objectType.setParent(null);

            // Assert
            assertThat(objectType.getParentId()).isNull();
        }
    }

    @Nested
    @DisplayName("update - 更新对象类型信息")
    class UpdateTests {

        @Test
        @DisplayName("应成功更新显示名称、描述和主键")
        void shouldUpdateAllFields() {
            // Arrange
            ObjectType objectType = ObjectType.create(
                    TEST_ONTOLOGY_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_PRIMARY_KEY);
            String newDisplayName = "新显示名称";
            String newDescription = "新描述";
            String newPrimaryKey = "new_id";

            // Act
            objectType.update(newDisplayName, newDescription, newPrimaryKey);

            // Assert
            assertThat(objectType.getDisplayName()).isEqualTo(newDisplayName);
            assertThat(objectType.getDescription()).isEqualTo(newDescription);
            assertThat(objectType.getPrimaryKey()).isEqualTo(newPrimaryKey);
        }

        @Test
        @DisplayName("应成功更新显示名称和描述，保留原主键")
        void shouldUpdateWithoutChangingPrimaryKey() {
            // Arrange
            ObjectType objectType = ObjectType.create(
                    TEST_ONTOLOGY_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_PRIMARY_KEY);
            String originalPrimaryKey = objectType.getPrimaryKey();

            // Act
            objectType.update("New Display Name", "New Description", null);

            // Assert
            assertThat(objectType.getDisplayName()).isEqualTo("New Display Name");
            assertThat(objectType.getDescription()).isEqualTo("New Description");
            assertThat(objectType.getPrimaryKey()).isEqualTo(originalPrimaryKey);
        }

        @Test
        @DisplayName("应保留原主键当传入空字符串")
        void shouldKeepOriginalPrimaryKeyWhenEmpty() {
            // Arrange
            ObjectType objectType = ObjectType.create(
                    TEST_ONTOLOGY_ID, TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_PRIMARY_KEY);
            String originalPrimaryKey = objectType.getPrimaryKey();

            // Act
            objectType.update("New Display Name", "New Description", "");

            // Assert
            assertThat(objectType.getPrimaryKey()).isEqualTo(originalPrimaryKey);
        }
    }
}

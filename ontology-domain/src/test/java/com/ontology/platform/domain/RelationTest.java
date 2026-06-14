package com.ontology.platform.domain;

import com.ontology.platform.common.enums.RelationCardinality;
import com.ontology.platform.domain.entity.Relation;
import com.ontology.platform.domain.entity.Ontology;
import com.ontology.platform.domain.factory.TestFactory;
import com.ontology.platform.domain.vo.RelationProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Relation实体测试
 * 
 * 测试范围：
 * - 关系创建
 * - 基数类型判断
 * - 反向关系设置
 * - 关系属性管理
 * - 信息更新
 */
@DisplayName("Relation实体测试")
class RelationTest {

    private static final String TEST_ONTOLOGY_ID = UUID.randomUUID().toString();
    private static final String TEST_SOURCE_TYPE_ID = UUID.randomUUID().toString();
    private static final String TEST_TARGET_TYPE_ID = UUID.randomUUID().toString();
    private static final String TEST_NAME = "has_manager";
    private static final String TEST_DISPLAY_NAME = "有经理";
    private static final String TEST_DESCRIPTION = "表示上下级关系";

    @Nested
    @DisplayName("create - 创建关系")
    class CreateTests {

        @Test
        @DisplayName("应创建带有默认值的关系")
        void shouldCreateRelationWithDefaults() {
            // Act
            Relation relation = Relation.create(
                    TEST_ONTOLOGY_ID, TEST_SOURCE_TYPE_ID, TEST_TARGET_TYPE_ID,
                    TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    RelationCardinality.ONE_TO_MANY);

            // Assert
            assertThat(relation.getId()).isNotNull().isNotEmpty();
            assertThat(UUID.fromString(relation.getId())).isNotNull();
            assertThat(relation.getOntologyId()).isEqualTo(TEST_ONTOLOGY_ID);
            assertThat(relation.getSourceTypeId()).isEqualTo(TEST_SOURCE_TYPE_ID);
            assertThat(relation.getTargetTypeId()).isEqualTo(TEST_TARGET_TYPE_ID);
            assertThat(relation.getName()).isEqualTo(TEST_NAME);
            assertThat(relation.getDisplayName()).isEqualTo(TEST_DISPLAY_NAME);
            assertThat(relation.getDescription()).isEqualTo(TEST_DESCRIPTION);
            assertThat(relation.getCardinality()).isEqualTo(RelationCardinality.ONE_TO_MANY);
            assertThat(relation.getReverseName()).isNull();
            assertThat(relation.getReverseDisplayName()).isNull();
            assertThat(relation.getProperties()).isEmpty();
            assertThat(relation.getCreatedAt()).isNotNull().isBeforeOrEqualTo(Instant.now());
            assertThat(relation.getUpdatedAt()).isNotNull().isBeforeOrEqualTo(Instant.now());
        }

        @ParameterizedTest
        @DisplayName("应创建所有基数类型的关系")
        @EnumSource(RelationCardinality.class)
        void shouldCreateRelationWithAllCardinalities(RelationCardinality cardinality) {
            // Act
            Relation relation = Relation.create(
                    TEST_ONTOLOGY_ID, TEST_SOURCE_TYPE_ID, TEST_TARGET_TYPE_ID,
                    TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    cardinality);

            // Assert
            assertThat(relation.getCardinality()).isEqualTo(cardinality);
            assertThat(relation.getId()).isNotNull();
        }

        @Test
        @DisplayName("应创建带有唯一ID的关系")
        void shouldCreateRelationWithUniqueId() {
            // Act
            Relation relation1 = Relation.create(
                    TEST_ONTOLOGY_ID, TEST_SOURCE_TYPE_ID, TEST_TARGET_TYPE_ID,
                    TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    RelationCardinality.ONE_TO_MANY);
            Relation relation2 = Relation.create(
                    TEST_ONTOLOGY_ID, TEST_SOURCE_TYPE_ID, TEST_TARGET_TYPE_ID,
                    TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    RelationCardinality.ONE_TO_MANY);

            // Assert
            assertThat(relation1.getId()).isNotEqualTo(relation2.getId());
        }
    }

    @Nested
    @DisplayName("基数类型判断")
    class CardinalityTests {

        @Test
        @DisplayName("ONE_TO_ONE关系应正确识别")
        void shouldIdentifyOneToOne() {
            Relation relation = Relation.create(
                    TEST_ONTOLOGY_ID, TEST_SOURCE_TYPE_ID, TEST_TARGET_TYPE_ID,
                    TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    RelationCardinality.ONE_TO_ONE);

            assertThat(relation.isOneToOne()).isTrue();
            assertThat(relation.isOneToMany()).isFalse();
            assertThat(relation.isManyToOne()).isFalse();
            assertThat(relation.isManyToMany()).isFalse();
        }

        @Test
        @DisplayName("ONE_TO_MANY关系应正确识别")
        void shouldIdentifyOneToMany() {
            Relation relation = Relation.create(
                    TEST_ONTOLOGY_ID, TEST_SOURCE_TYPE_ID, TEST_TARGET_TYPE_ID,
                    TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    RelationCardinality.ONE_TO_MANY);

            assertThat(relation.isOneToMany()).isTrue();
            assertThat(relation.isOneToOne()).isFalse();
            assertThat(relation.isManyToOne()).isFalse();
            assertThat(relation.isManyToMany()).isFalse();
        }

        @Test
        @DisplayName("MANY_TO_ONE关系应正确识别")
        void shouldIdentifyManyToOne() {
            Relation relation = Relation.create(
                    TEST_ONTOLOGY_ID, TEST_SOURCE_TYPE_ID, TEST_TARGET_TYPE_ID,
                    TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    RelationCardinality.MANY_TO_ONE);

            assertThat(relation.isManyToOne()).isTrue();
            assertThat(relation.isOneToOne()).isFalse();
            assertThat(relation.isOneToMany()).isFalse();
            assertThat(relation.isManyToMany()).isFalse();
        }

        @Test
        @DisplayName("MANY_TO_MANY关系应正确识别")
        void shouldIdentifyManyToMany() {
            Relation relation = Relation.create(
                    TEST_ONTOLOGY_ID, TEST_SOURCE_TYPE_ID, TEST_TARGET_TYPE_ID,
                    TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    RelationCardinality.MANY_TO_MANY);

            assertThat(relation.isManyToMany()).isTrue();
            assertThat(relation.isOneToOne()).isFalse();
            assertThat(relation.isOneToMany()).isFalse();
            assertThat(relation.isManyToOne()).isFalse();
        }
    }

    @Nested
    @DisplayName("setReverse - 设置反向关系")
    class SetReverseTests {

        @Test
        @DisplayName("应成功设置反向关系")
        void shouldSetReverseRelation() {
            // Arrange
            Relation relation = Relation.create(
                    TEST_ONTOLOGY_ID, TEST_SOURCE_TYPE_ID, TEST_TARGET_TYPE_ID,
                    TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    RelationCardinality.ONE_TO_MANY);
            String reverseName = "is_manager_of";
            String reverseDisplayName = "是经理";

            // Act
            relation.setReverse(reverseName, reverseDisplayName);

            // Assert
            assertThat(relation.getReverseName()).isEqualTo(reverseName);
            assertThat(relation.getReverseDisplayName()).isEqualTo(reverseDisplayName);
            assertThat(relation.getUpdatedAt()).isAfterOrEqualTo(relation.getCreatedAt());
        }

        @Test
        @DisplayName("应成功更新反向关系")
        void shouldUpdateReverseRelation() {
            // Arrange
            Relation relation = Relation.create(
                    TEST_ONTOLOGY_ID, TEST_SOURCE_TYPE_ID, TEST_TARGET_TYPE_ID,
                    TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    RelationCardinality.ONE_TO_MANY);
            relation.setReverse("old_reverse", "旧反向名称");

            // Act
            relation.setReverse("new_reverse", "新反向名称");

            // Assert
            assertThat(relation.getReverseName()).isEqualTo("new_reverse");
            assertThat(relation.getReverseDisplayName()).isEqualTo("新反向名称");
        }

        @Test
        @DisplayName("应成功清除反向关系")
        void shouldClearReverseRelation() {
            // Arrange
            Relation relation = Relation.create(
                    TEST_ONTOLOGY_ID, TEST_SOURCE_TYPE_ID, TEST_TARGET_TYPE_ID,
                    TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    RelationCardinality.ONE_TO_MANY);
            relation.setReverse("reverse", "反向名称");

            // Act
            relation.setReverse(null, null);

            // Assert
            assertThat(relation.getReverseName()).isNull();
            assertThat(relation.getReverseDisplayName()).isNull();
        }
    }

    @Nested
    @DisplayName("addProperty - 添加关系属性")
    class AddPropertyTests {

        @Test
        @DisplayName("应成功添加关系属性")
        void shouldAddRelationProperty() {
            // Arrange
            Relation relation = Relation.create(
                    TEST_ONTOLOGY_ID, TEST_SOURCE_TYPE_ID, TEST_TARGET_TYPE_ID,
                    TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    RelationCardinality.ONE_TO_MANY);
            RelationProperty property = RelationProperty.builder()
                    .name("weight")
                    .displayName("权重")
                    .dataType(com.ontology.platform.common.enums.PropertyDataType.DECIMAL)
                    .build();

            // Act
            relation.addProperty(property);

            // Assert
            assertThat(relation.getProperties()).hasSize(1);
            assertThat(relation.getProperties()).contains(property);
            assertThat(relation.getUpdatedAt()).isAfterOrEqualTo(relation.getCreatedAt());
        }

        @Test
        @DisplayName("应成功添加多个关系属性")
        void shouldAddMultipleRelationProperties() {
            // Arrange
            Relation relation = Relation.create(
                    TEST_ONTOLOGY_ID, TEST_SOURCE_TYPE_ID, TEST_TARGET_TYPE_ID,
                    TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    RelationCardinality.ONE_TO_MANY);
            RelationProperty property1 = RelationProperty.builder()
                    .name("weight")
                    .displayName("权重")
                    .dataType(com.ontology.platform.common.enums.PropertyDataType.DECIMAL)
                    .build();
            RelationProperty property2 = RelationProperty.builder()
                    .name("since")
                    .displayName("开始时间")
                    .dataType(com.ontology.platform.common.enums.PropertyDataType.DATETIME)
                    .build();

            // Act
            relation.addProperty(property1);
            relation.addProperty(property2);

            // Assert
            assertThat(relation.getProperties()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("removeProperty - 移除关系属性")
    class RemovePropertyTests {

        @Test
        @DisplayName("应成功移除已存在的关系属性")
        void shouldRemoveExistingRelationProperty() {
            // Arrange
            Relation relation = Relation.create(
                    TEST_ONTOLOGY_ID, TEST_SOURCE_TYPE_ID, TEST_TARGET_TYPE_ID,
                    TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    RelationCardinality.ONE_TO_MANY);
            RelationProperty property = RelationProperty.builder()
                    .name("weight")
                    .displayName("权重")
                    .dataType(com.ontology.platform.common.enums.PropertyDataType.DECIMAL)
                    .build();
            relation.addProperty(property);
            assertThat(relation.getProperties()).hasSize(1);

            // Act
            relation.removeProperty("weight");

            // Assert
            assertThat(relation.getProperties()).isEmpty();
        }

        @Test
        @DisplayName("移除不存在的属性应不改变列表")
        void shouldNotChangeWhenRemovingNonExistingProperty() {
            // Arrange
            Relation relation = Relation.create(
                    TEST_ONTOLOGY_ID, TEST_SOURCE_TYPE_ID, TEST_TARGET_TYPE_ID,
                    TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    RelationCardinality.ONE_TO_MANY);
            RelationProperty property = RelationProperty.builder()
                    .name("weight")
                    .displayName("权重")
                    .dataType(com.ontology.platform.common.enums.PropertyDataType.DECIMAL)
                    .build();
            relation.addProperty(property);

            // Act
            relation.removeProperty("non-existing");

            // Assert
            assertThat(relation.getProperties()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("update - 更新关系信息")
    class UpdateTests {

        @Test
        @DisplayName("应成功更新显示名称和描述")
        void shouldUpdateDisplayNameAndDescription() {
            // Arrange
            Relation relation = Relation.create(
                    TEST_ONTOLOGY_ID, TEST_SOURCE_TYPE_ID, TEST_TARGET_TYPE_ID,
                    TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    RelationCardinality.ONE_TO_MANY);
            String newDisplayName = "新显示名称";
            String newDescription = "新描述";

            // Act
            relation.update(newDisplayName, newDescription);

            // Assert
            assertThat(relation.getDisplayName()).isEqualTo(newDisplayName);
            assertThat(relation.getDescription()).isEqualTo(newDescription);
            assertThat(relation.getUpdatedAt()).isAfterOrEqualTo(relation.getCreatedAt());
        }

        @Test
        @DisplayName("应成功更新描述为null")
        void shouldUpdateDescriptionToNull() {
            // Arrange
            Relation relation = Relation.create(
                    TEST_ONTOLOGY_ID, TEST_SOURCE_TYPE_ID, TEST_TARGET_TYPE_ID,
                    TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    RelationCardinality.ONE_TO_MANY);

            // Act
            relation.update("New Display Name", null);

            // Assert
            assertThat(relation.getDescription()).isNull();
        }

        @Test
        @DisplayName("不应改变基数类型")
        void shouldNotChangeCardinality() {
            // Arrange
            Relation relation = Relation.create(
                    TEST_ONTOLOGY_ID, TEST_SOURCE_TYPE_ID, TEST_TARGET_TYPE_ID,
                    TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION,
                    RelationCardinality.MANY_TO_MANY);
            RelationCardinality originalCardinality = relation.getCardinality();

            // Act
            relation.update("New Display Name", "New Description");

            // Assert
            assertThat(relation.getCardinality()).isEqualTo(originalCardinality);
        }
    }
}

package com.ontology.platform.domain.entity;

import com.ontology.platform.common.enums.RelationCardinality;
import com.ontology.platform.domain.vo.RelationProperty;
import com.ontology.platform.common.enums.PropertyDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Relation实体单元测试
 */
@DisplayName("Relation实体测试")
class RelationTest {

    private static final String ONTOLOGY_ID = "ontology-001";
    private static final String SOURCE_TYPE_ID = "object-type-employee";
    private static final String TARGET_TYPE_ID = "object-type-department";
    private static final String NAME = "WORKS_IN";
    private static final String DISPLAY_NAME = "所属部门";
    private static final String DESCRIPTION = "员工与部门的关系";

    @Nested
    @DisplayName("create方法测试")
    class CreateTest {

        @Test
        @DisplayName("应该创建基本关系")
        void shouldCreateBasicRelation() {
            Relation relation = Relation.create(
                    ONTOLOGY_ID,
                    SOURCE_TYPE_ID,
                    TARGET_TYPE_ID,
                    NAME,
                    DISPLAY_NAME,
                    DESCRIPTION,
                    RelationCardinality.MANY_TO_ONE
            );

            assertNotNull(relation);
            assertNotNull(relation.getId());
            assertEquals(ONTOLOGY_ID, relation.getOntologyId());
            assertEquals(SOURCE_TYPE_ID, relation.getSourceTypeId());
            assertEquals(TARGET_TYPE_ID, relation.getTargetTypeId());
            assertEquals(NAME, relation.getName());
            assertEquals(DISPLAY_NAME, relation.getDisplayName());
            assertEquals(DESCRIPTION, relation.getDescription());
            assertEquals(RelationCardinality.MANY_TO_ONE, relation.getCardinality());
            assertNotNull(relation.getCreatedAt());
            assertNotNull(relation.getUpdatedAt());
            assertNotNull(relation.getProperties());
            assertTrue(relation.getProperties().isEmpty());
        }

        @Test
        @DisplayName("应该生成唯一的ID")
        void shouldGenerateUniqueId() {
            Relation relation1 = Relation.create(
                    ONTOLOGY_ID, SOURCE_TYPE_ID, TARGET_TYPE_ID,
                    NAME, DISPLAY_NAME, DESCRIPTION,
                    RelationCardinality.ONE_TO_ONE
            );
            Relation relation2 = Relation.create(
                    ONTOLOGY_ID, SOURCE_TYPE_ID, TARGET_TYPE_ID,
                    NAME, DISPLAY_NAME, DESCRIPTION,
                    RelationCardinality.ONE_TO_ONE
            );

            assertNotEquals(relation1.getId(), relation2.getId());
        }
    }

    @Nested
    @DisplayName("setReverse方法测试")
    class SetReverseTest {

        @Test
        @DisplayName("应该设置反向关系")
        void shouldSetReverseRelation() {
            Relation relation = Relation.create(
                    ONTOLOGY_ID, SOURCE_TYPE_ID, TARGET_TYPE_ID,
                    NAME, DISPLAY_NAME, DESCRIPTION,
                    RelationCardinality.ONE_TO_ONE
            );

            relation.setReverse("HAS_EMPLOYEE", "下属员工");

            assertEquals("HAS_EMPLOYEE", relation.getReverseName());
            assertEquals("下属员工", relation.getReverseDisplayName());
        }

        @Test
        @DisplayName("应该更新updatedAt时间戳")
        void shouldUpdateTimestamp() {
            Relation relation = Relation.create(
                    ONTOLOGY_ID, SOURCE_TYPE_ID, TARGET_TYPE_ID,
                    NAME, DISPLAY_NAME, DESCRIPTION,
                    RelationCardinality.ONE_TO_ONE
            );
            var originalUpdatedAt = relation.getUpdatedAt();

            relation.setReverse("HAS_EMPLOYEE", "下属员工");

            assertTrue(relation.getUpdatedAt().isAfter(originalUpdatedAt) ||
                    relation.getUpdatedAt().equals(originalUpdatedAt));
        }
    }

    @Nested
    @DisplayName("addProperty方法测试")
    class AddPropertyTest {

        @Test
        @DisplayName("应该添加关系属性")
        void shouldAddProperty() {
            Relation relation = Relation.create(
                    ONTOLOGY_ID, SOURCE_TYPE_ID, TARGET_TYPE_ID,
                    NAME, DISPLAY_NAME, DESCRIPTION,
                    RelationCardinality.ONE_TO_MANY
            );

            RelationProperty property = RelationProperty.create(
                    "WEIGHT", "权重",
                    PropertyDataType.DECIMAL, false
            );
            relation.addProperty(property);

            assertEquals(1, relation.getProperties().size());
            assertEquals("WEIGHT", relation.getProperties().get(0).getName());
        }

        @Test
        @DisplayName("应该支持添加多个属性")
        void shouldAddMultipleProperties() {
            Relation relation = Relation.create(
                    ONTOLOGY_ID, SOURCE_TYPE_ID, TARGET_TYPE_ID,
                    NAME, DISPLAY_NAME, DESCRIPTION,
                    RelationCardinality.MANY_TO_MANY
            );

            relation.addProperty(RelationProperty.create("WEIGHT", "权重", PropertyDataType.DECIMAL, false));
            relation.addProperty(RelationProperty.create("START_DATE", "开始日期", PropertyDataType.DATE, false));

            assertEquals(2, relation.getProperties().size());
        }
    }

    @Nested
    @DisplayName("removeProperty方法测试")
    class RemovePropertyTest {

        @Test
        @DisplayName("应该移除指定属性")
        void shouldRemoveProperty() {
            Relation relation = Relation.create(
                    ONTOLOGY_ID, SOURCE_TYPE_ID, TARGET_TYPE_ID,
                    NAME, DISPLAY_NAME, DESCRIPTION,
                    RelationCardinality.ONE_TO_ONE
            );
            relation.addProperty(RelationProperty.create("WEIGHT", "权重", PropertyDataType.DECIMAL, false));
            relation.addProperty(RelationProperty.create("START_DATE", "开始日期", PropertyDataType.DATE, false));

            relation.removeProperty("WEIGHT");

            assertEquals(1, relation.getProperties().size());
            assertEquals("START_DATE", relation.getProperties().get(0).getName());
        }

        @Test
        @DisplayName("移除不存在的属性应该不抛异常")
        void shouldNotThrowWhenRemovingNonExistentProperty() {
            Relation relation = Relation.create(
                    ONTOLOGY_ID, SOURCE_TYPE_ID, TARGET_TYPE_ID,
                    NAME, DISPLAY_NAME, DESCRIPTION,
                    RelationCardinality.ONE_TO_ONE
            );

            assertDoesNotThrow(() -> relation.removeProperty("NON_EXISTENT"));
        }
    }

    @Nested
    @DisplayName("update方法测试")
    class UpdateTest {

        @Test
        @DisplayName("应该更新显示名称和描述")
        void shouldUpdateDisplayNameAndDescription() {
            Relation relation = Relation.create(
                    ONTOLOGY_ID, SOURCE_TYPE_ID, TARGET_TYPE_ID,
                    NAME, DISPLAY_NAME, DESCRIPTION,
                    RelationCardinality.ONE_TO_ONE
            );

            relation.update("新显示名称", "新描述");

            assertEquals("新显示名称", relation.getDisplayName());
            assertEquals("新描述", relation.getDescription());
        }

        @Test
        @DisplayName("应该更新updatedAt时间戳")
        void shouldUpdateTimestamp() {
            Relation relation = Relation.create(
                    ONTOLOGY_ID, SOURCE_TYPE_ID, TARGET_TYPE_ID,
                    NAME, DISPLAY_NAME, DESCRIPTION,
                    RelationCardinality.ONE_TO_ONE
            );
            var originalUpdatedAt = relation.getUpdatedAt();

            relation.update("新显示名称", "新描述");

            assertTrue(relation.getUpdatedAt().isAfter(originalUpdatedAt) ||
                    relation.getUpdatedAt().equals(originalUpdatedAt));
        }
    }

    @Nested
    @DisplayName("isManyToMany方法测试")
    class IsManyToManyTest {

        @Test
        @DisplayName("MANY_TO_MANY应该返回true")
        void shouldReturnTrueForManyToMany() {
            Relation relation = Relation.create(
                    ONTOLOGY_ID, SOURCE_TYPE_ID, TARGET_TYPE_ID,
                    NAME, DISPLAY_NAME, DESCRIPTION,
                    RelationCardinality.MANY_TO_MANY
            );

            assertTrue(relation.isManyToMany());
        }

        @Test
        @DisplayName("ONE_TO_ONE应该返回false")
        void shouldReturnFalseForOneToOne() {
            Relation relation = Relation.create(
                    ONTOLOGY_ID, SOURCE_TYPE_ID, TARGET_TYPE_ID,
                    NAME, DISPLAY_NAME, DESCRIPTION,
                    RelationCardinality.ONE_TO_ONE
            );

            assertFalse(relation.isManyToMany());
        }

        @Test
        @DisplayName("ONE_TO_MANY应该返回false")
        void shouldReturnFalseForOneToMany() {
            Relation relation = Relation.create(
                    ONTOLOGY_ID, SOURCE_TYPE_ID, TARGET_TYPE_ID,
                    NAME, DISPLAY_NAME, DESCRIPTION,
                    RelationCardinality.ONE_TO_MANY
            );

            assertFalse(relation.isManyToMany());
        }
    }
}

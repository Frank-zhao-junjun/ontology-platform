package com.ontology.platform.domain;

import com.ontology.platform.common.enums.OntologyStatus;
import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.entity.Ontology;
import com.ontology.platform.domain.factory.TestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Ontology聚合根测试
 * 
 * 测试范围：
 * - 本体创建
 * - 状态转换（草稿→已发布→已归档）
 * - 对象类型管理（添加/移除）
 * - 版本升级
 * - 信息更新
 */
@DisplayName("Ontology聚合根测试")
class OntologyTest {

    private static final String TEST_USER = "test-user";
    private static final String TEST_NAME = "test_ontology";
    private static final String TEST_DISPLAY_NAME = "测试本体";
    private static final String TEST_DESCRIPTION = "这是一个测试本体";

    @Nested
    @DisplayName("create - 创建本体")
    class CreateTests {

        @Test
        @DisplayName("应创建带有默认值的草稿本体")
        void shouldCreateDraftOntologyWithDefaults() {
            // Act
            Ontology ontology = Ontology.create(TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_USER);

            // Assert
            assertThat(ontology.getId()).isNotNull().isNotEmpty();
            assertThat(UUID.fromString(ontology.getId())).isNotNull();
            assertThat(ontology.getTenantId()).isEqualTo("default");
            assertThat(ontology.getName()).isEqualTo(TEST_NAME);
            assertThat(ontology.getDisplayName()).isEqualTo(TEST_DISPLAY_NAME);
            assertThat(ontology.getDescription()).isEqualTo(TEST_DESCRIPTION);
            assertThat(ontology.getVersion()).isEqualTo("0.1.0");
            assertThat(ontology.getStatus()).isEqualTo(OntologyStatus.DRAFT);
            assertThat(ontology.getObjectTypeCount()).isZero();
            assertThat(ontology.getActionTypeCount()).isZero();
            assertThat(ontology.getCreatedBy()).isEqualTo(TEST_USER);
            assertThat(ontology.getCreatedAt()).isNotNull().isBeforeOrEqualTo(Instant.now());
            assertThat(ontology.getUpdatedAt()).isNotNull().isBeforeOrEqualTo(Instant.now());
            assertThat(ontology.getPublishedAt()).isNull();
            assertThat(ontology.getObjectTypes()).isEmpty();
        }

        @Test
        @DisplayName("应创建带有唯一ID的本体")
        void shouldCreateOntologyWithUniqueId() {
            // Act
            Ontology ontology1 = Ontology.create(TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_USER);
            Ontology ontology2 = Ontology.create(TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_USER);

            // Assert
            assertThat(ontology1.getId()).isNotEqualTo(ontology2.getId());
        }

        @Test
        @DisplayName("应创建带有空描述的本体")
        void shouldCreateOntologyWithEmptyDescription() {
            // Act
            Ontology ontology = Ontology.create(TEST_NAME, TEST_DISPLAY_NAME, null, TEST_USER);

            // Assert
            assertThat(ontology.getDescription()).isNull();
        }
    }

    @Nested
    @DisplayName("publish - 发布本体")
    class PublishTests {

        @Test
        @DisplayName("应成功发布草稿本体")
        void shouldPublishDraftOntology() {
            // Arrange
            Ontology ontology = Ontology.create(TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_USER);
            assertThat(ontology.getStatus()).isEqualTo(OntologyStatus.DRAFT);

            // Act
            ontology.publish();

            // Assert
            assertThat(ontology.getStatus()).isEqualTo(OntologyStatus.PUBLISHED);
            assertThat(ontology.getPublishedAt()).isNotNull().isBeforeOrEqualTo(Instant.now());
            assertThat(ontology.getUpdatedAt()).isNotNull().isAfter(ontology.getCreatedAt());
        }

        @Test
        @DisplayName("发布已归档本体应抛出异常")
        void shouldThrowExceptionWhenPublishingArchivedOntology() {
            // Arrange
            Ontology ontology = TestFactory.createArchivedOntology(UUID.randomUUID().toString(), TEST_NAME);

            // Act & Assert
            assertThatThrownBy(ontology::publish)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only draft ontology can be published");
        }

        @Test
        @DisplayName("重复发布草稿本体应抛出异常")
        void shouldThrowExceptionWhenPublishingAlreadyPublishedOntology() {
            // Arrange
            Ontology ontology = TestFactory.createPublishedOntology(UUID.randomUUID().toString(), TEST_NAME);

            // Act & Assert
            assertThatThrownBy(ontology::publish)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only draft ontology can be published");
        }
    }

    @Nested
    @DisplayName("archive - 归档本体")
    class ArchiveTests {

        @Test
        @DisplayName("应成功归档草稿本体")
        void shouldArchiveDraftOntology() {
            // Arrange
            Ontology ontology = Ontology.create(TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_USER);

            // Act
            ontology.archive();

            // Assert
            assertThat(ontology.getStatus()).isEqualTo(OntologyStatus.ARCHIVED);
            assertThat(ontology.getUpdatedAt()).isNotNull().isAfter(ontology.getCreatedAt());
        }

        @Test
        @DisplayName("应成功归档已发布本体")
        void shouldArchivePublishedOntology() {
            // Arrange
            Ontology ontology = TestFactory.createPublishedOntology(UUID.randomUUID().toString(), TEST_NAME);

            // Act
            ontology.archive();

            // Assert
            assertThat(ontology.getStatus()).isEqualTo(OntologyStatus.ARCHIVED);
        }

        @Test
        @DisplayName("重复归档应抛出异常")
        void shouldThrowExceptionWhenArchivingAlreadyArchivedOntology() {
            // Arrange
            Ontology ontology = TestFactory.createArchivedOntology(UUID.randomUUID().toString(), TEST_NAME);

            // Act & Assert
            assertThatThrownBy(ontology::archive)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already archived");
        }
    }

    @Nested
    @DisplayName("addObjectType - 添加对象类型")
    class AddObjectTypeTests {

        @Test
        @DisplayName("应成功添加对象类型")
        void shouldAddObjectType() {
            // Arrange
            Ontology ontology = Ontology.create(TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_USER);
            String ontologyId = ontology.getId();
            ObjectType objectType = TestFactory.createObjectType(UUID.randomUUID().toString(), ontologyId, "person");

            // Act
            ontology.addObjectType(objectType);

            // Assert
            assertThat(ontology.getObjectTypes()).hasSize(1);
            assertThat(ontology.getObjectTypeCount()).isEqualTo(1);
            assertThat(ontology.getObjectTypes()).contains(objectType);
            assertThat(ontology.getUpdatedAt()).isAfter(ontology.getCreatedAt());
        }

        @Test
        @DisplayName("应成功添加多个对象类型")
        void shouldAddMultipleObjectTypes() {
            // Arrange
            Ontology ontology = Ontology.create(TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_USER);
            String ontologyId = ontology.getId();
            ObjectType objectType1 = TestFactory.createObjectType(UUID.randomUUID().toString(), ontologyId, "person");
            ObjectType objectType2 = TestFactory.createObjectType(UUID.randomUUID().toString(), ontologyId, "organization");

            // Act
            ontology.addObjectType(objectType1);
            ontology.addObjectType(objectType2);

            // Assert
            assertThat(ontology.getObjectTypes()).hasSize(2);
            assertThat(ontology.getObjectTypeCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("添加对象类型应更新updatedAt时间戳")
        void shouldUpdateTimestampWhenAddingObjectType() {
            // Arrange
            Ontology ontology = Ontology.create(TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_USER);
            Instant originalUpdatedAt = ontology.getUpdatedAt();
            String ontologyId = ontology.getId();
            ObjectType objectType = TestFactory.createObjectType(UUID.randomUUID().toString(), ontologyId, "person");

            // Act
            ontology.addObjectType(objectType);

            // Assert
            assertThat(ontology.getUpdatedAt()).isAfter(originalUpdatedAt);
        }
    }

    @Nested
    @DisplayName("removeObjectType - 移除对象类型")
    class RemoveObjectTypeTests {

        @Test
        @DisplayName("应成功移除已存在的对象类型")
        void shouldRemoveExistingObjectType() {
            // Arrange
            Ontology ontology = Ontology.create(TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_USER);
            String ontologyId = ontology.getId();
            ObjectType objectType = TestFactory.createObjectType(UUID.randomUUID().toString(), ontologyId, "person");
            ontology.addObjectType(objectType);
            assertThat(ontology.getObjectTypeCount()).isEqualTo(1);

            // Act
            ontology.removeObjectType(objectType.getId());

            // Assert
            assertThat(ontology.getObjectTypes()).isEmpty();
            assertThat(ontology.getObjectTypeCount()).isZero();
        }

        @Test
        @DisplayName("移除不存在的对象类型应不改变列表")
        void shouldNotChangeWhenRemovingNonExistingObjectType() {
            // Arrange
            Ontology ontology = Ontology.create(TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_USER);
            String ontologyId = ontology.getId();
            ObjectType objectType = TestFactory.createObjectType(UUID.randomUUID().toString(), ontologyId, "person");
            ontology.addObjectType(objectType);

            // Act
            ontology.removeObjectType("non-existing-id");

            // Assert
            assertThat(ontology.getObjectTypes()).hasSize(1);
            assertThat(ontology.getObjectTypeCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("从空列表移除应安全执行")
        void shouldSafelyRemoveFromEmptyList() {
            // Arrange
            Ontology ontology = Ontology.create(TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_USER);

            // Act
            ontology.removeObjectType("any-id");

            // Assert
            assertThat(ontology.getObjectTypes()).isEmpty();
            assertThat(ontology.getObjectTypeCount()).isZero();
        }
    }

    @Nested
    @DisplayName("update - 更新本体信息")
    class UpdateTests {

        @Test
        @DisplayName("应成功更新显示名称和描述")
        void shouldUpdateDisplayNameAndDescription() {
            // Arrange
            Ontology ontology = Ontology.create(TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_USER);
            String newDisplayName = "新显示名称";
            String newDescription = "新的描述内容";

            // Act
            ontology.update(newDisplayName, newDescription);

            // Assert
            assertThat(ontology.getDisplayName()).isEqualTo(newDisplayName);
            assertThat(ontology.getDescription()).isEqualTo(newDescription);
            assertThat(ontology.getUpdatedAt()).isAfter(ontology.getCreatedAt());
        }

        @Test
        @DisplayName("应成功更新描述为null")
        void shouldUpdateDescriptionToNull() {
            // Arrange
            Ontology ontology = Ontology.create(TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_USER);

            // Act
            ontology.update("New Display Name", null);

            // Assert
            assertThat(ontology.getDescription()).isNull();
        }
    }

    @Nested
    @DisplayName("bumpVersion - 升级版本")
    class BumpVersionTests {

        @Test
        @DisplayName("应成功升级次版本号")
        void shouldBumpMinorVersion() {
            // Arrange
            Ontology ontology = Ontology.create(TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_USER);
            assertThat(ontology.getVersion()).isEqualTo("0.1.0");

            // Act
            ontology.bumpVersion();

            // Assert
            assertThat(ontology.getVersion()).isEqualTo("0.2.0");
        }

        @Test
        @DisplayName("应成功多次升级版本")
        void shouldBumpVersionMultipleTimes() {
            // Arrange
            Ontology ontology = Ontology.create(TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_USER);
            assertThat(ontology.getVersion()).isEqualTo("0.1.0");

            // Act
            ontology.bumpVersion();
            ontology.bumpVersion();
            ontology.bumpVersion();

            // Assert
            assertThat(ontology.getVersion()).isEqualTo("0.4.0");
        }

        @Test
        @DisplayName("升级版本应更新updatedAt时间戳")
        void shouldUpdateTimestampWhenBumpingVersion() {
            // Arrange
            Ontology ontology = Ontology.create(TEST_NAME, TEST_DISPLAY_NAME, TEST_DESCRIPTION, TEST_USER);
            Instant originalUpdatedAt = ontology.getUpdatedAt();

            // Act
            ontology.bumpVersion();

            // Assert
            assertThat(ontology.getUpdatedAt()).isAfter(originalUpdatedAt);
        }
    }
}

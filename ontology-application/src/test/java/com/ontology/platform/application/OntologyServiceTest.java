package com.ontology.platform.application;

import com.ontology.platform.application.dto.*;
import com.ontology.platform.application.service.OntologyService;
import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.enums.OntologyStatus;
import com.ontology.platform.common.enums.PropertyDataType;
import com.ontology.platform.common.enums.RelationCardinality;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.common.exception.ValidationException;
import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.entity.Ontology;
import com.ontology.platform.domain.entity.Relation;
import com.ontology.platform.domain.repository.ObjectTypeRepository;
import com.ontology.platform.domain.repository.OntologyRepository;
import com.ontology.platform.domain.repository.RelationRepository;
import com.ontology.platform.domain.vo.Property;
import com.ontology.platform.domain.vo.RelationProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OntologyService测试
 * 
 * 测试范围：
 * - 本体CRUD操作
 * - 状态转换
 * - 业务规则校验（名称唯一性等）
 * - DTO转换
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OntologyService测试")
class OntologyServiceTest {

    @Mock
    private OntologyRepository ontologyRepository;

    @Mock
    private ObjectTypeRepository objectTypeRepository;

    @Mock
    private RelationRepository relationRepository;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private com.ontology.platform.application.service.impl.OntologyServiceImpl ontologyService;

    private static final String TEST_USER_ID = "test-user-123";
    private static final String TEST_TENANT_ID = "default";
    private static final String TEST_ONTOLOGY_ID = UUID.randomUUID().toString();
    private static final String TEST_OBJECT_TYPE_ID = UUID.randomUUID().toString();

    @Nested
    @DisplayName("createOntology - 创建本体")
    class CreateOntologyTests {

        @Test
        @DisplayName("应成功创建本体")
        void shouldCreateOntologySuccessfully() {
            // Arrange
            CreateOntologyRequest request = CreateOntologyRequest.builder()
                    .name("test_ontology")
                    .displayName("测试本体")
                    .description("这是一个测试本体")
                    .build();

            when(ontologyRepository.existsByTenantIdAndName(TEST_TENANT_ID, "test_ontology"))
                    .thenReturn(false);
            when(ontologyRepository.save(any(Ontology.class)))
                    .thenAnswer(invocation -> {
                        Ontology ontology = invocation.getArgument(0);
                        return ontology;
                    });

            // Act
            OntologyResponse response = ontologyService.createOntology(request, TEST_USER_ID);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("test_ontology");
            assertThat(response.getDisplayName()).isEqualTo("测试本体");
            assertThat(response.getStatus()).isEqualTo(OntologyStatus.DRAFT);
            assertThat(response.getVersion()).isEqualTo("0.1.0");

            ArgumentCaptor<Ontology> ontologyCaptor = ArgumentCaptor.forClass(Ontology.class);
            verify(ontologyRepository).save(ontologyCaptor.capture());
            Ontology savedOntology = ontologyCaptor.getValue();
            assertThat(savedOntology.getTenantId()).isEqualTo(TEST_TENANT_ID);
            assertThat(savedOntology.getCreatedBy()).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("本体名称已存在应抛出异常")
        void shouldThrowExceptionWhenNameExists() {
            // Arrange
            CreateOntologyRequest request = CreateOntologyRequest.builder()
                    .name("existing_ontology")
                    .displayName("已存在的本体")
                    .build();

            when(ontologyRepository.existsByTenantIdAndName(TEST_TENANT_ID, "existing_ontology"))
                    .thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> ontologyService.createOntology(request, TEST_USER_ID))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("本体名称已存在");

            verify(ontologyRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getOntologyById - 获取本体")
    class GetOntologyByIdTests {

        @Test
        @DisplayName("应成功获取存在的本体")
        void shouldGetExistingOntology() {
            // Arrange
            Ontology ontology = createTestOntology();
            when(ontologyRepository.findById(TEST_ONTOLOGY_ID))
                    .thenReturn(Optional.of(ontology));

            // Act
            OntologyDetailResponse response = ontologyService.getOntologyById(TEST_ONTOLOGY_ID);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(TEST_ONTOLOGY_ID);
            assertThat(response.getName()).isEqualTo("test_ontology");
        }

        @Test
        @DisplayName("获取不存在的本体应抛出异常")
        void shouldThrowExceptionWhenOntologyNotFound() {
            // Arrange
            when(ontologyRepository.findById("non-existing-id"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> ontologyService.getOntologyById("non-existing-id"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Ontology");
        }
    }

    @Nested
    @DisplayName("listOntologies - 列表查询")
    class ListOntologiesTests {

        @Test
        @DisplayName("应返回本体列表")
        void shouldReturnOntologyList() {
            // Arrange
            List<Ontology> ontologies = List.of(
                    createTestOntology(),
                    createTestOntology("test_ontology_2", "测试本体2")
            );
            when(ontologyRepository.findByTenantIdWithPage(TEST_TENANT_ID, 1, 20))
                    .thenReturn(ontologies);

            // Act
            List<OntologyResponse> responses = ontologyService.listOntologies(TEST_TENANT_ID, 1, 20);

            // Assert
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getName()).isEqualTo("test_ontology");
            assertThat(responses.get(1).getName()).isEqualTo("test_ontology_2");
        }

        @Test
        @DisplayName("空列表应返回空结果")
        void shouldReturnEmptyListWhenNoOntologies() {
            // Arrange
            when(ontologyRepository.findByTenantIdWithPage(TEST_TENANT_ID, 1, 20))
                    .thenReturn(List.of());

            // Act
            List<OntologyResponse> responses = ontologyService.listOntologies(TEST_TENANT_ID, 1, 20);

            // Assert
            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateOntology - 更新本体")
    class UpdateOntologyTests {

        @Test
        @DisplayName("应成功更新本体")
        void shouldUpdateOntologySuccessfully() {
            // Arrange
            Ontology ontology = createTestOntology();
            UpdateOntologyRequest request = UpdateOntologyRequest.builder()
                    .displayName("更新后的显示名称")
                    .description("更新后的描述")
                    .build();

            when(ontologyRepository.findById(TEST_ONTOLOGY_ID))
                    .thenReturn(Optional.of(ontology));
            when(ontologyRepository.existsByTenantIdAndNameAndIdNot(TEST_TENANT_ID, "test_ontology", TEST_ONTOLOGY_ID))
                    .thenReturn(false);
            when(ontologyRepository.update(any(Ontology.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            OntologyResponse response = ontologyService.updateOntology(TEST_ONTOLOGY_ID, request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getDisplayName()).isEqualTo("更新后的显示名称");
            verify(ontologyRepository).update(any(Ontology.class));
        }

        @Test
        @DisplayName("更新不存在的本体应抛出异常")
        void shouldThrowExceptionWhenUpdatingNonExisting() {
            // Arrange
            UpdateOntologyRequest request = UpdateOntologyRequest.builder()
                    .displayName("新名称")
                    .build();

            when(ontologyRepository.findById("non-existing-id"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> ontologyService.updateOntology("non-existing-id", request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteOntology - 删除本体")
    class DeleteOntologyTests {

        @Test
        @DisplayName("应成功删除本体")
        void shouldDeleteOntologySuccessfully() {
            // Arrange
            Ontology ontology = createTestOntology();
            when(ontologyRepository.findById(TEST_ONTOLOGY_ID))
                    .thenReturn(Optional.of(ontology));

            // Act
            ontologyService.deleteOntology(TEST_ONTOLOGY_ID);

            // Assert
            verify(ontologyRepository).deleteById(TEST_ONTOLOGY_ID);
        }

        @Test
        @DisplayName("删除不存在的本体应抛出异常")
        void shouldThrowExceptionWhenDeletingNonExisting() {
            // Arrange
            when(ontologyRepository.findById("non-existing-id"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> ontologyService.deleteOntology("non-existing-id"))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(ontologyRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("publishOntology - 发布本体")
    class PublishOntologyTests {

        @Test
        @DisplayName("应成功发布草稿本体")
        void shouldPublishDraftOntology() {
            // Arrange
            Ontology ontology = createTestOntology();
            when(ontologyRepository.findById(TEST_ONTOLOGY_ID))
                    .thenReturn(Optional.of(ontology));
            when(ontologyRepository.update(any(Ontology.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            OntologyResponse response = ontologyService.publishOntology(TEST_ONTOLOGY_ID);

            // Assert
            assertThat(response.getStatus()).isEqualTo(OntologyStatus.PUBLISHED);
            assertThat(response.getPublishedAt()).isNotNull();
            verify(ontologyRepository).update(any(Ontology.class));
        }

        @Test
        @DisplayName("发布不存在的本体应抛出异常")
        void shouldThrowExceptionWhenPublishingNonExisting() {
            // Arrange
            when(ontologyRepository.findById("non-existing-id"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> ontologyService.publishOntology("non-existing-id"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("archiveOntology - 归档本体")
    class ArchiveOntologyTests {

        @Test
        @DisplayName("应成功归档本体")
        void shouldArchiveOntologySuccessfully() {
            // Arrange
            Ontology ontology = createTestOntology();
            when(ontologyRepository.findById(TEST_ONTOLOGY_ID))
                    .thenReturn(Optional.of(ontology));
            when(ontologyRepository.update(any(Ontology.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            OntologyResponse response = ontologyService.archiveOntology(TEST_ONTOLOGY_ID);

            // Assert
            assertThat(response.getStatus()).isEqualTo(OntologyStatus.ARCHIVED);
            verify(ontologyRepository).update(any(Ontology.class));
        }
    }

    @Nested
    @DisplayName("createObjectType - 创建对象类型")
    class CreateObjectTypeTests {

        @Test
        @DisplayName("应成功创建对象类型")
        void shouldCreateObjectTypeSuccessfully() {
            // Arrange
            CreateObjectTypeRequest request = CreateObjectTypeRequest.builder()
                    .ontologyId(TEST_ONTOLOGY_ID)
                    .name("person")
                    .displayName("人员")
                    .description("人员对象类型")
                    .primaryKey("id")
                    .build();

            Ontology ontology = createTestOntology();

            when(ontologyRepository.findById(TEST_ONTOLOGY_ID))
                    .thenReturn(Optional.of(ontology));
            when(objectTypeRepository.existsByOntologyIdAndName(TEST_ONTOLOGY_ID, "person"))
                    .thenReturn(false);
            when(objectTypeRepository.save(any(ObjectType.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ObjectTypeResponse response = ontologyService.createObjectType(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("person");
            assertThat(response.getOntologyId()).isEqualTo(TEST_ONTOLOGY_ID);
            verify(objectTypeRepository).save(any(ObjectType.class));
        }

        @Test
        @DisplayName("对象类型名称已存在应抛出异常")
        void shouldThrowExceptionWhenObjectTypeNameExists() {
            // Arrange
            CreateObjectTypeRequest request = CreateObjectTypeRequest.builder()
                    .ontologyId(TEST_ONTOLOGY_ID)
                    .name("existing_type")
                    .displayName("已存在的类型")
                    .primaryKey("id")
                    .build();

            Ontology ontology = createTestOntology();

            when(ontologyRepository.findById(TEST_ONTOLOGY_ID))
                    .thenReturn(Optional.of(ontology));
            when(objectTypeRepository.existsByOntologyIdAndName(TEST_ONTOLOGY_ID, "existing_type"))
                    .thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> ontologyService.createObjectType(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("对象类型名称已存在");
        }

        @Test
        @DisplayName("本体不存在时应抛出异常")
        void shouldThrowExceptionWhenOntologyNotExists() {
            // Arrange
            CreateObjectTypeRequest request = CreateObjectTypeRequest.builder()
                    .ontologyId("non-existing-ontology")
                    .name("person")
                    .displayName("人员")
                    .primaryKey("id")
                    .build();

            when(ontologyRepository.findById("non-existing-ontology"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> ontologyService.createObjectType(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Ontology");
        }
    }

    @Nested
    @DisplayName("createProperty - 创建属性")
    class CreatePropertyTests {

        @Test
        @DisplayName("应成功创建属性")
        void shouldCreatePropertySuccessfully() {
            // Arrange
            ObjectType objectType = ObjectType.builder()
                    .id(TEST_OBJECT_TYPE_ID)
                    .ontologyId(TEST_ONTOLOGY_ID)
                    .name("person")
                    .displayName("人员")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            CreatePropertyRequest request = CreatePropertyRequest.builder()
                    .objectTypeId(TEST_OBJECT_TYPE_ID)
                    .name("email")
                    .displayName("邮箱")
                    .dataType(PropertyDataType.STRING)
                    .isRequired(false)
                    .isUnique(false)
                    .isSearchable(true)
                    .isSortable(true)
                    .build();

            when(objectTypeRepository.findById(TEST_OBJECT_TYPE_ID))
                    .thenReturn(Optional.of(objectType));
            when(objectTypeRepository.update(any(ObjectType.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PropertyResponse response = ontologyService.createProperty(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("email");
            assertThat(response.getDataType()).isEqualTo(PropertyDataType.STRING);
        }

        @Test
        @DisplayName("对象类型不存在时应抛出异常")
        void shouldThrowExceptionWhenObjectTypeNotExists() {
            // Arrange
            CreatePropertyRequest request = CreatePropertyRequest.builder()
                    .objectTypeId("non-existing-type")
                    .name("email")
                    .displayName("邮箱")
                    .dataType(PropertyDataType.STRING)
                    .build();

            when(objectTypeRepository.findById("non-existing-type"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> ontologyService.createProperty(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ObjectType");
        }
    }

    @Nested
    @DisplayName("validateOntology - 验证本体")
    class ValidateOntologyTests {

        @Test
        @DisplayName("应返回验证结果")
        void shouldReturnValidationResult() {
            // Arrange
            Ontology ontology = createTestOntology();
            when(ontologyRepository.findById(TEST_ONTOLOGY_ID))
                    .thenReturn(Optional.of(ontology));

            // Act
            ValidationResultResponse response = ontologyService.validateOntology(TEST_ONTOLOGY_ID);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.isValid()).isTrue();
            assertThat(response.getSummary()).isNotNull();
            assertThat(response.getSummary().getPassed()).isEqualTo(1);
        }

        @Test
        @DisplayName("本体不存在时应抛出异常")
        void shouldThrowExceptionWhenOntologyNotExists() {
            // Arrange
            when(ontologyRepository.findById("non-existing-id"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> ontologyService.validateOntology("non-existing-id"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ==================== Helper Methods ====================

    private Ontology createTestOntology() {
        return createTestOntology("test_ontology", "测试本体");
    }

    private Ontology createTestOntology(String name, String displayName) {
        return Ontology.builder()
                .id(TEST_ONTOLOGY_ID)
                .tenantId(TEST_TENANT_ID)
                .name(name)
                .displayName(displayName)
                .description("测试描述")
                .version("0.1.0")
                .status(OntologyStatus.DRAFT)
                .objectTypeCount(0)
                .actionTypeCount(0)
                .createdBy(TEST_USER_ID)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.*;
import com.ontology.platform.application.service.impl.RelationServiceImpl;
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
import com.ontology.platform.infrastructure.graph.GraphService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * RelationService单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RelationService测试")
class RelationServiceTest {

    @Mock
    private RelationRepository relationRepository;

    @Mock
    private ObjectTypeRepository objectTypeRepository;

    @Mock
    private OntologyRepository ontologyRepository;

    @Mock
    private GraphService graphService;

    @InjectMocks
    private RelationServiceImpl relationService;

    private Ontology testOntology;
    private ObjectType sourceObjectType;
    private ObjectType targetObjectType;

    @BeforeEach
    void setUp() {
        testOntology = Ontology.create("test-ontology", "测试本体", "描述", "user-001");
        sourceObjectType = ObjectType.create(
                testOntology.getId(), "EMPLOYEE", "员工", "员工对象类型", "emp_id"
        );
        targetObjectType = ObjectType.create(
                testOntology.getId(), "DEPARTMENT", "部门", "部门对象类型", "dept_id"
        );
    }

    @Nested
    @DisplayName("createRelation测试")
    class CreateRelationTest {

        @Test
        @DisplayName("应该成功创建关系")
        void shouldCreateRelationSuccessfully() {
            // given
            CreateRelationRequest request = CreateRelationRequest.builder()
                    .ontologyId(testOntology.getId())
                    .sourceTypeId(sourceObjectType.getId())
                    .targetTypeId(targetObjectType.getId())
                    .name("WORKS_IN")
                    .displayName("所属部门")
                    .description("员工与部门的关系")
                    .cardinality(RelationCardinality.MANY_TO_ONE)
                    .build();

            when(ontologyRepository.findById(testOntology.getId())).thenReturn(Optional.of(testOntology));
            when(objectTypeRepository.findById(sourceObjectType.getId())).thenReturn(Optional.of(sourceObjectType));
            when(objectTypeRepository.findById(targetObjectType.getId())).thenReturn(Optional.of(targetObjectType));
            when(relationRepository.existsByOntologyIdAndName(any(), any())).thenReturn(false);
            when(relationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            RelationResponse response = relationService.createRelation(request);

            // then
            assertNotNull(response);
            assertEquals("WORKS_IN", response.getName());
            assertEquals("所属部门", response.getDisplayName());
            assertEquals(RelationCardinality.MANY_TO_ONE, response.getCardinality());

            verify(relationRepository).save(any(Relation.class));
            verify(graphService).createEdge(eq(sourceObjectType.getId()), eq(targetObjectType.getId()), eq("WORKS_IN"));
        }

        @Test
        @DisplayName("本体不存在应该抛出异常")
        void shouldThrowExceptionWhenOntologyNotFound() {
            CreateRelationRequest request = CreateRelationRequest.builder()
                    .ontologyId("non-existent-ontology")
                    .sourceTypeId(sourceObjectType.getId())
                    .targetTypeId(targetObjectType.getId())
                    .name("WORKS_IN")
                    .displayName("所属部门")
                    .cardinality(RelationCardinality.ONE_TO_ONE)
                    .build();

            when(ontologyRepository.findById("non-existent-ontology")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> relationService.createRelation(request));
        }

        @Test
        @DisplayName("源对象类型不存在应该抛出异常")
        void shouldThrowExceptionWhenSourceTypeNotFound() {
            CreateRelationRequest request = CreateRelationRequest.builder()
                    .ontologyId(testOntology.getId())
                    .sourceTypeId("non-existent-source")
                    .targetTypeId(targetObjectType.getId())
                    .name("WORKS_IN")
                    .displayName("所属部门")
                    .cardinality(RelationCardinality.ONE_TO_ONE)
                    .build();

            when(ontologyRepository.findById(testOntology.getId())).thenReturn(Optional.of(testOntology));
            when(objectTypeRepository.findById("non-existent-source")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> relationService.createRelation(request));
        }

        @Test
        @DisplayName("关系名称重复应该抛出异常")
        void shouldThrowExceptionWhenNameDuplicated() {
            CreateRelationRequest request = CreateRelationRequest.builder()
                    .ontologyId(testOntology.getId())
                    .sourceTypeId(sourceObjectType.getId())
                    .targetTypeId(targetObjectType.getId())
                    .name("WORKS_IN")
                    .displayName("所属部门")
                    .cardinality(RelationCardinality.ONE_TO_ONE)
                    .build();

            when(ontologyRepository.findById(testOntology.getId())).thenReturn(Optional.of(testOntology));
            when(objectTypeRepository.findById(sourceObjectType.getId())).thenReturn(Optional.of(sourceObjectType));
            when(objectTypeRepository.findById(targetObjectType.getId())).thenReturn(Optional.of(targetObjectType));
            when(relationRepository.existsByOntologyIdAndName(testOntology.getId(), "WORKS_IN")).thenReturn(true);

            assertThrows(ValidationException.class, () -> relationService.createRelation(request));
        }

        @Test
        @DisplayName("源类型不属于本体应该抛出异常")
        void shouldThrowExceptionWhenSourceTypeNotBelongToOntology() {
            CreateRelationRequest request = CreateRelationRequest.builder()
                    .ontologyId(testOntology.getId())
                    .sourceTypeId(sourceObjectType.getId())
                    .targetTypeId(targetObjectType.getId())
                    .name("WORKS_IN")
                    .displayName("所属部门")
                    .cardinality(RelationCardinality.ONE_TO_ONE)
                    .build();

            // 修改sourceObjectType的ontologyId使其不匹配
            ObjectType wrongSourceType = ObjectType.create(
                    "different-ontology", "EMPLOYEE", "员工", "员工对象类型", "emp_id"
            );

            when(ontologyRepository.findById(testOntology.getId())).thenReturn(Optional.of(testOntology));
            when(objectTypeRepository.findById(sourceObjectType.getId())).thenReturn(Optional.of(wrongSourceType));
            when(objectTypeRepository.findById(targetObjectType.getId())).thenReturn(Optional.of(targetObjectType));

            assertThrows(ValidationException.class, () -> relationService.createRelation(request));
        }

        @Test
        @DisplayName("应该支持创建带属性的关系")
        void shouldCreateRelationWithProperties() {
            CreateRelationRequest request = CreateRelationRequest.builder()
                    .ontologyId(testOntology.getId())
                    .sourceTypeId(sourceObjectType.getId())
                    .targetTypeId(targetObjectType.getId())
                    .name("WORKS_IN")
                    .displayName("所属部门")
                    .cardinality(RelationCardinality.MANY_TO_ONE)
                    .properties(List.of(
                            RelationPropertyDTO.builder()
                                    .name("START_DATE")
                                    .displayName("入职日期")
                                    .dataType(PropertyDataType.DATE)
                                    .isRequired(true)
                                    .build()
                    ))
                    .build();

            when(ontologyRepository.findById(testOntology.getId())).thenReturn(Optional.of(testOntology));
            when(objectTypeRepository.findById(sourceObjectType.getId())).thenReturn(Optional.of(sourceObjectType));
            when(objectTypeRepository.findById(targetObjectType.getId())).thenReturn(Optional.of(targetObjectType));
            when(relationRepository.existsByOntologyIdAndName(any(), any())).thenReturn(false);
            when(relationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            RelationResponse response = relationService.createRelation(request);

            assertNotNull(response.getProperties());
            assertEquals(1, response.getProperties().size());
            assertEquals("START_DATE", response.getProperties().get(0).getName());
        }

        @Test
        @DisplayName("应该支持创建带反向关系")
        void shouldCreateRelationWithReverse() {
            CreateRelationRequest request = CreateRelationRequest.builder()
                    .ontologyId(testOntology.getId())
                    .sourceTypeId(sourceObjectType.getId())
                    .targetTypeId(targetObjectType.getId())
                    .name("WORKS_IN")
                    .displayName("所属部门")
                    .cardinality(RelationCardinality.MANY_TO_ONE)
                    .reverseName("HAS_EMPLOYEE")
                    .reverseDisplayName("拥有员工")
                    .build();

            when(ontologyRepository.findById(testOntology.getId())).thenReturn(Optional.of(testOntology));
            when(objectTypeRepository.findById(sourceObjectType.getId())).thenReturn(Optional.of(sourceObjectType));
            when(objectTypeRepository.findById(targetObjectType.getId())).thenReturn(Optional.of(targetObjectType));
            when(relationRepository.existsByOntologyIdAndName(any(), any())).thenReturn(false);
            when(relationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            RelationResponse response = relationService.createRelation(request);

            assertEquals("HAS_EMPLOYEE", response.getReverseName());
            assertEquals("拥有员工", response.getReverseDisplayName());
        }
    }

    @Nested
    @DisplayName("updateRelation测试")
    class UpdateRelationTest {

        @Test
        @DisplayName("应该成功更新关系")
        void shouldUpdateRelationSuccessfully() {
            Relation existingRelation = Relation.create(
                    testOntology.getId(),
                    sourceObjectType.getId(),
                    targetObjectType.getId(),
                    "WORKS_IN",
                    "所属部门",
                    "原描述",
                    RelationCardinality.MANY_TO_ONE
            );

            UpdateRelationRequest request = UpdateRelationRequest.builder()
                    .displayName("新名称")
                    .description("新描述")
                    .build();

            when(relationRepository.findById(existingRelation.getId())).thenReturn(Optional.of(existingRelation));
            when(relationRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

            RelationResponse response = relationService.updateRelation(existingRelation.getId(), request);

            assertEquals("新名称", response.getDisplayName());
            assertEquals("新描述", response.getDescription());
        }

        @Test
        @DisplayName("关系不存在应该抛出异常")
        void shouldThrowExceptionWhenRelationNotFound() {
            UpdateRelationRequest request = UpdateRelationRequest.builder()
                    .displayName("新名称")
                    .build();

            when(relationRepository.findById("non-existent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> relationService.updateRelation("non-existent", request));
        }

        @Test
        @DisplayName("应该支持更新关系属性")
        void shouldUpdateRelationProperties() {
            Relation existingRelation = Relation.create(
                    testOntology.getId(),
                    sourceObjectType.getId(),
                    targetObjectType.getId(),
                    "WORKS_IN",
                    "所属部门",
                    "描述",
                    RelationCardinality.MANY_TO_ONE
            );

            UpdateRelationRequest request = UpdateRelationRequest.builder()
                    .properties(List.of(
                            RelationPropertyDTO.builder()
                                    .name("WEIGHT")
                                    .displayName("权重")
                                    .dataType(PropertyDataType.DECIMAL)
                                    .isRequired(false)
                                    .build()
                    ))
                    .build();

            when(relationRepository.findById(existingRelation.getId())).thenReturn(Optional.of(existingRelation));
            when(relationRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

            RelationResponse response = relationService.updateRelation(existingRelation.getId(), request);

            assertNotNull(response.getProperties());
            assertEquals(1, response.getProperties().size());
            assertEquals("WEIGHT", response.getProperties().get(0).getName());
        }
    }

    @Nested
    @DisplayName("deleteRelation测试")
    class DeleteRelationTest {

        @Test
        @DisplayName("应该成功删除关系")
        void shouldDeleteRelationSuccessfully() {
            Relation existingRelation = Relation.create(
                    testOntology.getId(),
                    sourceObjectType.getId(),
                    targetObjectType.getId(),
                    "WORKS_IN",
                    "所属部门",
                    "描述",
                    RelationCardinality.ONE_TO_ONE
            );

            when(relationRepository.findById(existingRelation.getId())).thenReturn(Optional.of(existingRelation));
            when(objectTypeRepository.findById(sourceObjectType.getId())).thenReturn(Optional.of(sourceObjectType));
            when(objectTypeRepository.findById(targetObjectType.getId())).thenReturn(Optional.of(targetObjectType));

            relationService.deleteRelation(existingRelation.getId());

            verify(graphService).deleteEdge(eq(sourceObjectType.getId()), eq(targetObjectType.getId()), eq("WORKS_IN"));
            verify(relationRepository).deleteById(existingRelation.getId());
        }

        @Test
        @DisplayName("关系不存在应该抛出异常")
        void shouldThrowExceptionWhenRelationNotFound() {
            when(relationRepository.findById("non-existent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> relationService.deleteRelation("non-existent"));
        }
    }

    @Nested
    @DisplayName("查询方法测试")
    class QueryMethodsTest {

        @Test
        @DisplayName("应该按源类型查询关系")
        void shouldFindBySourceTypeId() {
            Relation relation = Relation.create(
                    testOntology.getId(),
                    sourceObjectType.getId(),
                    targetObjectType.getId(),
                    "WORKS_IN",
                    "所属部门",
                    "描述",
                    RelationCardinality.MANY_TO_ONE
            );

            when(relationRepository.findBySourceTypeId(sourceObjectType.getId()))
                    .thenReturn(List.of(relation));

            List<RelationResponse> result = relationService.findBySourceTypeId(sourceObjectType.getId());

            assertEquals(1, result.size());
            assertEquals("WORKS_IN", result.get(0).getName());
        }

        @Test
        @DisplayName("应该按目标类型查询关系")
        void shouldFindByTargetTypeId() {
            Relation relation = Relation.create(
                    testOntology.getId(),
                    sourceObjectType.getId(),
                    targetObjectType.getId(),
                    "WORKS_IN",
                    "所属部门",
                    "描述",
                    RelationCardinality.MANY_TO_ONE
            );

            when(relationRepository.findByTargetTypeId(targetObjectType.getId()))
                    .thenReturn(List.of(relation));

            List<RelationResponse> result = relationService.findByTargetTypeId(targetObjectType.getId());

            assertEquals(1, result.size());
            assertEquals("WORKS_IN", result.get(0).getName());
        }

        @Test
        @DisplayName("应该查询关联的对象类型")
        void shouldFindRelatedObjectTypes() {
            Relation relation = Relation.create(
                    testOntology.getId(),
                    sourceObjectType.getId(),
                    targetObjectType.getId(),
                    "WORKS_IN",
                    "所属部门",
                    "描述",
                    RelationCardinality.MANY_TO_ONE
            );

            when(relationRepository.findById(relation.getId())).thenReturn(Optional.of(relation));
            when(objectTypeRepository.findById(sourceObjectType.getId())).thenReturn(Optional.of(sourceObjectType));
            when(objectTypeRepository.findById(targetObjectType.getId())).thenReturn(Optional.of(targetObjectType));

            List<ObjectTypeResponse> result = relationService.findRelatedObjectTypes(relation.getId());

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("应该获取关系列表")
        void shouldListRelations() {
            Relation relation1 = Relation.create(
                    testOntology.getId(),
                    sourceObjectType.getId(),
                    targetObjectType.getId(),
                    "WORKS_IN",
                    "所属部门",
                    "描述",
                    RelationCardinality.MANY_TO_ONE
            );
            Relation relation2 = Relation.create(
                    testOntology.getId(),
                    targetObjectType.getId(),
                    sourceObjectType.getId(),
                    "MANAGES",
                    "管理部门",
                    "描述",
                    RelationCardinality.ONE_TO_ONE
            );

            when(relationRepository.findByOntologyId(testOntology.getId()))
                    .thenReturn(List.of(relation1, relation2));

            List<RelationResponse> result = relationService.listRelations(testOntology.getId());

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("应该获取关系详情")
        void shouldGetRelationById() {
            Relation relation = Relation.create(
                    testOntology.getId(),
                    sourceObjectType.getId(),
                    targetObjectType.getId(),
                    "WORKS_IN",
                    "所属部门",
                    "描述",
                    RelationCardinality.MANY_TO_ONE
            );

            when(relationRepository.findById(relation.getId())).thenReturn(Optional.of(relation));

            RelationResponse response = relationService.getRelationById(relation.getId());

            assertNotNull(response);
            assertEquals("WORKS_IN", response.getName());
            assertEquals("所属部门", response.getDisplayName());
        }
    }
}

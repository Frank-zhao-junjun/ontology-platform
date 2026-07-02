package com.ontology.platform.application.service.impl;

import com.ontology.platform.application.dto.UpdateObjectTypeRequest;
import com.ontology.platform.common.exception.ValidationException;
import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.entity.Ontology;
import com.ontology.platform.domain.repository.ObjectTypeRepository;
import com.ontology.platform.domain.repository.OntologyRepository;
import com.ontology.platform.domain.repository.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ObjectTypeServiceImpl 循环继承检测测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ObjectTypeServiceImpl 继承链校验测试")
class ObjectTypeServiceTest {

    @Mock
    private ObjectTypeRepository objectTypeRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private OntologyRepository ontologyRepository;

    @InjectMocks
    private ObjectTypeServiceImpl objectTypeService;

    private static final String ONTOLOGY_ID = "test-ontology-id";
    private static final String TYPE_A_ID = "type-a-id";
    private static final String TYPE_B_ID = "type-b-id";
    private static final String TYPE_C_ID = "type-c-id";

    private Ontology testOntology;

    @BeforeEach
    void setUp() {
        testOntology = Ontology.create("test_ontology", "测试本体", "测试用本体", "system");
        testOntology.setId(ONTOLOGY_ID);
    }

    private ObjectType objectTypeWithParent(String id, String parentId) {
        ObjectType objectType = ObjectType.create(ONTOLOGY_ID, "type_" + id, "类型" + id, "描述", "id");
        objectType.setId(id);
        objectType.setParentId(parentId);
        return objectType;
    }

    @Nested
    @DisplayName("updateObjectType - 循环继承检测")
    class UpdateObjectTypeCycleTests {

        @Test
        @DisplayName("更新时设置自身为父类型应被拒绝")
        void shouldRejectSelfReferenceOnUpdate() {
            ObjectType typeA = objectTypeWithParent(TYPE_A_ID, null);

            when(objectTypeRepository.findById(TYPE_A_ID)).thenReturn(Optional.of(typeA));

            UpdateObjectTypeRequest request = UpdateObjectTypeRequest.builder()
                    .parentId(TYPE_A_ID)
                    .build();

            assertThatThrownBy(() -> objectTypeService.updateObjectType(TYPE_A_ID, request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("不能将自身设为父类型");

            verify(objectTypeRepository, never()).update(any(ObjectType.class));
        }

        @Test
        @DisplayName("A→B→A 直接循环应被拒绝")
        void shouldRejectDirectCycleAtoBtoA() {
            ObjectType typeA = objectTypeWithParent(TYPE_A_ID, null);
            ObjectType typeB = objectTypeWithParent(TYPE_B_ID, TYPE_A_ID);

            when(objectTypeRepository.findById(TYPE_A_ID)).thenReturn(Optional.of(typeA));
            when(objectTypeRepository.findById(TYPE_B_ID)).thenReturn(Optional.of(typeB));

            UpdateObjectTypeRequest request = UpdateObjectTypeRequest.builder()
                    .parentId(TYPE_B_ID)
                    .build();

            assertThatThrownBy(() -> objectTypeService.updateObjectType(TYPE_A_ID, request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("检测到循环继承链");

            verify(objectTypeRepository, never()).update(any(ObjectType.class));
        }

        @Test
        @DisplayName("A→B→C→A 深层循环应被拒绝")
        void shouldRejectDeepCycleAtoBtoCtoA() {
            ObjectType typeA = objectTypeWithParent(TYPE_A_ID, null);
            ObjectType typeB = objectTypeWithParent(TYPE_B_ID, TYPE_C_ID);
            ObjectType typeC = objectTypeWithParent(TYPE_C_ID, TYPE_A_ID);

            when(objectTypeRepository.findById(TYPE_A_ID)).thenReturn(Optional.of(typeA));
            when(objectTypeRepository.findById(TYPE_B_ID)).thenReturn(Optional.of(typeB));
            when(objectTypeRepository.findById(TYPE_C_ID)).thenReturn(Optional.of(typeC));

            UpdateObjectTypeRequest request = UpdateObjectTypeRequest.builder()
                    .parentId(TYPE_B_ID)
                    .build();

            assertThatThrownBy(() -> objectTypeService.updateObjectType(TYPE_A_ID, request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("检测到循环继承链");

            verify(objectTypeRepository, never()).update(any(ObjectType.class));
        }

        @Test
        @DisplayName("A→B→C 有效继承链应被接受")
        void shouldAllowValidChainAtoBtoC() {
            ObjectType typeA = objectTypeWithParent(TYPE_A_ID, null);
            ObjectType typeB = objectTypeWithParent(TYPE_B_ID, TYPE_C_ID);
            ObjectType typeC = objectTypeWithParent(TYPE_C_ID, null);

            when(objectTypeRepository.findById(TYPE_A_ID)).thenReturn(Optional.of(typeA));
            when(objectTypeRepository.findById(TYPE_B_ID)).thenReturn(Optional.of(typeB));
            when(objectTypeRepository.findById(TYPE_C_ID)).thenReturn(Optional.of(typeC));
            when(objectTypeRepository.update(any(ObjectType.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateObjectTypeRequest request = UpdateObjectTypeRequest.builder()
                    .parentId(TYPE_B_ID)
                    .build();

            var response = objectTypeService.updateObjectType(TYPE_A_ID, request);

            assertThat(response).isNotNull();
            assertThat(response.getParentId()).isEqualTo(TYPE_B_ID);
            verify(objectTypeRepository).update(any(ObjectType.class));
        }
    }
}

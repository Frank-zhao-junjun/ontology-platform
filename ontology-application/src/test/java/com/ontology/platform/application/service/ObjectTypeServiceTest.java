package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.*;
import com.ontology.platform.application.service.impl.ObjectTypeServiceImpl;
import com.ontology.platform.common.enums.PropertyDataType;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.common.exception.ValidationException;
import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.entity.Ontology;
import com.ontology.platform.domain.repository.ObjectTypeRepository;
import com.ontology.platform.domain.repository.OntologyRepository;
import com.ontology.platform.domain.repository.PropertyRepository;
import com.ontology.platform.domain.vo.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ObjectTypeService单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ObjectTypeService单元测试")
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
    private static final String OBJECT_TYPE_ID = "test-object-type-id";
    private static final String PROPERTY_ID = "test-property-id";

    private Ontology testOntology;
    private ObjectType testObjectType;
    private Property testProperty;

    @BeforeEach
    void setUp() {
        testOntology = Ontology.create("test_ontology", "测试本体", "测试用本体", "system");
        testOntology.setId(ONTOLOGY_ID);

        testObjectType = ObjectType.create(ONTOLOGY_ID, "person", "人员", "人员对象类型", "id");
        testObjectType.setId(OBJECT_TYPE_ID);

        testProperty = Property.create(OBJECT_TYPE_ID, "name", "姓名", "人员姓名", PropertyDataType.STRING, false);
        testProperty.setId(PROPERTY_ID);
    }

    @Nested
    @DisplayName("createObjectType - 创建对象类型")
    class CreateObjectTypeTests {

        @Test
        @DisplayName("应成功创建对象类型")
        void shouldCreateObjectTypeSuccessfully() {
            CreateObjectTypeRequest request = CreateObjectTypeRequest.builder()
                    .ontologyId(ONTOLOGY_ID).name("customer").displayName("客户")
                    .description("客户对象类型").primaryKey("customer_id").build();

            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(testOntology));
            when(objectTypeRepository.existsByOntologyIdAndName(ONTOLOGY_ID, "customer")).thenReturn(false);
            when(objectTypeRepository.save(any(ObjectType.class))).thenAnswer(inv -> {
                ObjectType ot = inv.getArgument(0);
                ot.setId("new-id");
                return ot;
            });

            ObjectTypeResponse response = objectTypeService.createObjectType(request);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("new-id");
            assertThat(response.getName()).isEqualTo("customer");
            verify(objectTypeRepository).save(any(ObjectType.class));
        }

        @Test
        @DisplayName("本体不存在时应抛出异常")
        void shouldThrowExceptionWhenOntologyNotFound() {
            CreateObjectTypeRequest request = CreateObjectTypeRequest.builder()
                    .ontologyId("non-existent").name("test").displayName("测试").primaryKey("id").build();

            when(ontologyRepository.findById("non-existent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> objectTypeService.createObjectType(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("名称重复时应抛出异常")
        void shouldThrowExceptionWhenNameDuplicate() {
            CreateObjectTypeRequest request = CreateObjectTypeRequest.builder()
                    .ontologyId(ONTOLOGY_ID).name("person").displayName("人员").primaryKey("id").build();

            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(testOntology));
            when(objectTypeRepository.existsByOntologyIdAndName(ONTOLOGY_ID, "person")).thenReturn(true);

            assertThatThrownBy(() -> objectTypeService.createObjectType(request))
                    .isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    @DisplayName("getObjectTypeById - 获取对象类型")
    class GetObjectTypeByIdTests {

        @Test
        @DisplayName("应成功获取对象类型详情")
        void shouldGetObjectTypeByIdSuccessfully() {
            testObjectType.getProperties().add(testProperty);
            when(objectTypeRepository.findById(OBJECT_TYPE_ID)).thenReturn(Optional.of(testObjectType));

            ObjectTypeDetailResponse response = objectTypeService.getObjectTypeById(OBJECT_TYPE_ID);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(OBJECT_TYPE_ID);
            assertThat(response.getProperties()).hasSize(1);
        }

        @Test
        @DisplayName("对象类型不存在时应抛出异常")
        void shouldThrowExceptionWhenNotFound() {
            when(objectTypeRepository.findById("non-existent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> objectTypeService.getObjectTypeById("non-existent"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createProperty - 创建属性")
    class CreatePropertyTests {

        @Test
        @DisplayName("应成功创建属性")
        void shouldCreatePropertySuccessfully() {
            CreatePropertyRequest request = CreatePropertyRequest.builder()
                    .objectTypeId(OBJECT_TYPE_ID).name("email").displayName("邮箱")
                    .dataType(PropertyDataType.STRING).isRequired(true).build();

            when(objectTypeRepository.findById(OBJECT_TYPE_ID)).thenReturn(Optional.of(testObjectType));
            when(propertyRepository.existsByObjectTypeIdAndName(OBJECT_TYPE_ID, "email")).thenReturn(false);
            when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> {
                Property p = inv.getArgument(0);
                p.setId("new-property-id");
                return p;
            });

            PropertyResponse response = objectTypeService.createProperty(request);

            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("email");
            verify(propertyRepository).save(any(Property.class));
        }

        @Test
        @DisplayName("属性名重复时应抛出异常")
        void shouldThrowExceptionWhenPropertyNameDuplicate() {
            CreatePropertyRequest request = CreatePropertyRequest.builder()
                    .objectTypeId(OBJECT_TYPE_ID).name("name").displayName("姓名")
                    .dataType(PropertyDataType.STRING).build();

            when(objectTypeRepository.findById(OBJECT_TYPE_ID)).thenReturn(Optional.of(testObjectType));
            when(propertyRepository.existsByObjectTypeIdAndName(OBJECT_TYPE_ID, "name")).thenReturn(true);

            assertThatThrownBy(() -> objectTypeService.createProperty(request))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("应成功创建带约束的属性")
        void shouldCreatePropertyWithConstraints() {
            List<ConstraintDefinition> constraints = List.of(
                    ConstraintDefinition.builder().type("MIN_LENGTH").value(5).errorMessage("长度不能少于5").build(),
                    ConstraintDefinition.builder().type("PATTERN").value("^[a-z]+$").errorMessage("只能包含小写字母").build()
            );

            CreatePropertyRequest request = CreatePropertyRequest.builder()
                    .objectTypeId(OBJECT_TYPE_ID).name("username").displayName("用户名")
                    .dataType(PropertyDataType.STRING).constraints(constraints).build();

            when(objectTypeRepository.findById(OBJECT_TYPE_ID)).thenReturn(Optional.of(testObjectType));
            when(propertyRepository.existsByObjectTypeIdAndName(OBJECT_TYPE_ID, "username")).thenReturn(false);
            when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> inv.getArgument(0));

            PropertyResponse response = objectTypeService.createProperty(request);

            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("username");
        }
    }

    @Nested
    @DisplayName("updateProperty - 更新属性")
    class UpdatePropertyTests {

        @Test
        @DisplayName("应成功更新属性")
        void shouldUpdatePropertySuccessfully() {
            UpdatePropertyRequest request = UpdatePropertyRequest.builder()
                    .displayName("更新后的姓名").isRequired(true).build();

            when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(testProperty));
            when(propertyRepository.update(any(Property.class))).thenAnswer(inv -> inv.getArgument(0));

            PropertyResponse response = objectTypeService.updateProperty(PROPERTY_ID, request);

            assertThat(response).isNotNull();
            assertThat(response.getDisplayName()).isEqualTo("更新后的姓名");
        }
    }

    @Nested
    @DisplayName("deleteProperty - 删除属性")
    class DeletePropertyTests {

        @Test
        @DisplayName("应成功删除属性")
        void shouldDeletePropertySuccessfully() {
            when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(testProperty));
            doNothing().when(propertyRepository).deleteById(PROPERTY_ID);

            objectTypeService.deleteProperty(PROPERTY_ID);

            verify(propertyRepository).deleteById(PROPERTY_ID);
        }
    }
}

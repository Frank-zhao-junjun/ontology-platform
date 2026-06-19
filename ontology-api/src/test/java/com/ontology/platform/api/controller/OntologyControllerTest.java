package com.ontology.platform.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.*;
import com.ontology.platform.application.service.OntologyService;
import com.ontology.platform.api.config.GlobalExceptionHandler;
import com.ontology.platform.common.enums.OntologyStatus;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.common.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OntologyController测试
 * 
 * 测试范围：
 * - REST API端点测试
 * - 请求参数验证
 * - 响应格式验证
 * - 异常处理
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OntologyController测试")
class OntologyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OntologyService ontologyService;

    @Mock
    private com.ontology.platform.application.service.semantic.SemanticService semanticService;

    @InjectMocks
    private OntologyController ontologyController;

    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/v1/ontologies";
    private static final String TEST_ONTOLOGY_ID = UUID.randomUUID().toString();
    private static final String TEST_USER_ID = "test-user";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ontologyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Nested
    @DisplayName("POST /v1/ontologies - 创建本体")
    class CreateOntologyTests {

        @Test
        @DisplayName("应成功创建本体并返回201")
        void shouldCreateOntologySuccessfully() throws Exception {
            // Arrange
            CreateOntologyRequest request = CreateOntologyRequest.builder()
                    .name("test_ontology")
                    .displayName("测试本体")
                    .description("这是一个测试本体")
                    .build();

            OntologyResponse response = OntologyResponse.builder()
                    .id(TEST_ONTOLOGY_ID)
                    .name("test_ontology")
                    .displayName("测试本体")
                    .description("这是一个测试本体")
                    .version("0.1.0")
                    .status(OntologyStatus.DRAFT)
                    .objectTypeCount(0)
                    .createdAt(Instant.now())
                    .build();

            when(ontologyService.createOntology(any(CreateOntologyRequest.class), eq(TEST_USER_ID)))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-User-Id", TEST_USER_ID)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(TEST_ONTOLOGY_ID))
                    .andExpect(jsonPath("$.data.name").value("test_ontology"))
                    .andExpect(jsonPath("$.data.status").value("DRAFT"));

            verify(ontologyService).createOntology(any(CreateOntologyRequest.class), eq(TEST_USER_ID));
        }

        @Test
        @DisplayName("本体名称已存在应返回400")
        void shouldReturn400WhenNameExists() throws Exception {
            // Arrange
            CreateOntologyRequest request = CreateOntologyRequest.builder()
                    .name("existing_ontology")
                    .displayName("已存在的本体")
                    .build();

            when(ontologyService.createOntology(any(CreateOntologyRequest.class), anyString()))
                    .thenThrow(new ValidationException("本体名称已存在"));

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(is(not(0))))
                    .andExpect(jsonPath("$.message").value(containsString("本体名称已存在")));
        }

        @Test
        @DisplayName("空名称应返回400")
        void shouldReturn400WhenNameIsEmpty() throws Exception {
            // Arrange
            CreateOntologyRequest request = CreateOntologyRequest.builder()
                    .name("")
                    .displayName("测试本体")
                    .build();

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(is(not(0))));
        }

        @Test
        @DisplayName("空显示名称应返回400")
        void shouldReturn400WhenDisplayNameIsEmpty() throws Exception {
            // Arrange
            CreateOntologyRequest request = CreateOntologyRequest.builder()
                    .name("valid_name")
                    .displayName("")
                    .build();

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(is(not(0))));
        }
    }

    @Nested
    @DisplayName("GET /v1/ontologies/{id} - 获取本体")
    class GetOntologyTests {

        @Test
        @DisplayName("应成功获取本体")
        void shouldGetOntologySuccessfully() throws Exception {
            // Arrange
            OntologyDetailResponse response = OntologyDetailResponse.builder()
                    .id(TEST_ONTOLOGY_ID)
                    .name("test_ontology")
                    .displayName("测试本体")
                    .description("测试描述")
                    .version("1.0.0")
                    .status(OntologyStatus.PUBLISHED)
                    .objectTypeCount(2)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .publishedAt(Instant.now())
                    .objectTypes(List.of())
                    .build();

            when(ontologyService.getOntologyById(TEST_ONTOLOGY_ID))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + TEST_ONTOLOGY_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(TEST_ONTOLOGY_ID))
                    .andExpect(jsonPath("$.data.name").value("test_ontology"))
                    .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
        }

        @Test
        @DisplayName("本体不存在应返回404")
        void shouldReturn404WhenOntologyNotFound() throws Exception {
            // Arrange
            when(ontologyService.getOntologyById("non-existing-id"))
                    .thenThrow(new ResourceNotFoundException("Ontology", "non-existing-id"));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/non-existing-id"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(is(not(0))))
                    .andExpect(jsonPath("$.message").value(containsString("not found")));
        }
    }

    @Nested
    @DisplayName("GET /v1/ontologies - 列表查询")
    class ListOntologiesTests {

        @Test
        @DisplayName("应返回本体列表")
        void shouldReturnOntologyList() throws Exception {
            // Arrange
            List<OntologyResponse> responses = List.of(
                    OntologyResponse.builder()
                            .id(TEST_ONTOLOGY_ID)
                            .name("ontology_1")
                            .displayName("本体1")
                            .status(OntologyStatus.DRAFT)
                            .build(),
                    OntologyResponse.builder()
                            .id(UUID.randomUUID().toString())
                            .name("ontology_2")
                            .displayName("本体2")
                            .status(OntologyStatus.PUBLISHED)
                            .build()
            );

            when(ontologyService.listOntologies("default", 1, 20))
                    .thenReturn(responses);

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                            .param("tenantId", "default")
                            .param("page", "1")
                            .param("pageSize", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].name").value("ontology_1"))
                    .andExpect(jsonPath("$.data[1].name").value("ontology_2"));
        }

        @Test
        @DisplayName("空列表应返回空数组")
        void shouldReturnEmptyList() throws Exception {
            // Arrange
            when(ontologyService.listOntologies(anyString(), anyInt(), anyInt()))
                    .thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("应使用默认分页参数")
        void shouldUseDefaultPagination() throws Exception {
            // Arrange
            when(ontologyService.listOntologies("default", 1, 20))
                    .thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk());

            verify(ontologyService).listOntologies("default", 1, 20);
        }
    }

    @Nested
    @DisplayName("PUT /v1/ontologies/{id} - 更新本体")
    class UpdateOntologyTests {

        @Test
        @DisplayName("应成功更新本体")
        void shouldUpdateOntologySuccessfully() throws Exception {
            // Arrange
            UpdateOntologyRequest request = UpdateOntologyRequest.builder()
                    .displayName("更新后的名称")
                    .description("更新后的描述")
                    .build();

            OntologyResponse response = OntologyResponse.builder()
                    .id(TEST_ONTOLOGY_ID)
                    .name("test_ontology")
                    .displayName("更新后的名称")
                    .description("更新后的描述")
                    .status(OntologyStatus.DRAFT)
                    .build();

            when(ontologyService.updateOntology(eq(TEST_ONTOLOGY_ID), any(UpdateOntologyRequest.class)))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/" + TEST_ONTOLOGY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.displayName").value("更新后的名称"));
        }

        @Test
        @DisplayName("本体不存在应返回404")
        void shouldReturn404WhenOntologyNotFound() throws Exception {
            // Arrange
            UpdateOntologyRequest request = UpdateOntologyRequest.builder()
                    .displayName("新名称")
                    .build();

            when(ontologyService.updateOntology(eq("non-existing-id"), any(UpdateOntologyRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Ontology", "non-existing-id"));

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/non-existing-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(is(not(0))));
        }
    }

    @Nested
    @DisplayName("DELETE /v1/ontologies/{id} - 删除本体")
    class DeleteOntologyTests {

        @Test
        @DisplayName("应成功删除本体")
        void shouldDeleteOntologySuccessfully() throws Exception {
            // Arrange
            doNothing().when(ontologyService).deleteOntology(TEST_ONTOLOGY_ID);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + TEST_ONTOLOGY_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            verify(ontologyService).deleteOntology(TEST_ONTOLOGY_ID);
        }

        @Test
        @DisplayName("本体不存在应返回404")
        void shouldReturn404WhenOntologyNotFound() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("Ontology", "non-existing-id"))
                    .when(ontologyService).deleteOntology("non-existing-id");

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/non-existing-id"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(is(not(0))));
        }
    }

    @Nested
    @DisplayName("POST /v1/ontologies/{id}/publish - 发布本体")
    class PublishOntologyTests {

        @Test
        @DisplayName("应成功发布本体")
        void shouldPublishOntologySuccessfully() throws Exception {
            // Arrange
            OntologyResponse response = OntologyResponse.builder()
                    .id(TEST_ONTOLOGY_ID)
                    .name("test_ontology")
                    .status(OntologyStatus.PUBLISHED)
                    .publishedAt(Instant.now())
                    .build();

            when(ontologyService.publishOntology(TEST_ONTOLOGY_ID))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/" + TEST_ONTOLOGY_ID + "/publish"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
        }
    }

    @Nested
    @DisplayName("POST /v1/ontologies/{id}/archive - 归档本体")
    class ArchiveOntologyTests {

        @Test
        @DisplayName("应成功归档本体")
        void shouldArchiveOntologySuccessfully() throws Exception {
            // Arrange
            OntologyResponse response = OntologyResponse.builder()
                    .id(TEST_ONTOLOGY_ID)
                    .name("test_ontology")
                    .status(OntologyStatus.ARCHIVED)
                    .build();

            when(ontologyService.archiveOntology(TEST_ONTOLOGY_ID))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/" + TEST_ONTOLOGY_ID + "/archive"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.status").value("ARCHIVED"));
        }
    }

    @Nested
    @DisplayName("POST /v1/ontologies/{id}/validate - 验证本体")
    class ValidateOntologyTests {

        @Test
        @DisplayName("应返回验证结果")
        void shouldReturnValidationResult() throws Exception {
            // Arrange
            ValidationResultResponse response = ValidationResultResponse.builder()
                    .valid(true)
                    .summary(ValidationResultResponse.ValidationSummary.builder()
                            .passed(5)
                            .warnings(0)
                            .errors(0)
                            .build())
                    .issues(List.of())
                    .build();

            when(ontologyService.validateOntology(TEST_ONTOLOGY_ID))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/" + TEST_ONTOLOGY_ID + "/validate"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.valid").value(true))
                    .andExpect(jsonPath("$.data.summary.passed").value(5));
        }
    }
}

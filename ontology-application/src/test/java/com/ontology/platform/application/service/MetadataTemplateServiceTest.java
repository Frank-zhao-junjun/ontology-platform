package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateMetadataTemplateRequest;
import com.ontology.platform.application.dto.domain.MetadataTemplateResponse;
import com.ontology.platform.infrastructure.persistence.MetadataTemplatePO;
import com.ontology.platform.infrastructure.persistence.MetadataTemplatePOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MetadataTemplateService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MetadataTemplateService Test")
class MetadataTemplateServiceTest {

    @Mock
    private MetadataTemplatePOMapper mapper;

    private MetadataTemplateService service;

    @Captor
    private ArgumentCaptor<MetadataTemplatePO> poCaptor;

    @BeforeEach
    void setUp() {
        service = new MetadataTemplateService(mapper);
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should create successfully and return response")
        void createSuccess() {
            when(mapper.insert(any())).thenReturn(1);

            CreateMetadataTemplateRequest request = CreateMetadataTemplateRequest.builder()
                    .name("标准物料模板")
                    .nameEn("Standard Material Template")
                    .description("物料主数据的标准元数据模板")
                    .domain("material")
                    .templateType("entity")
                    .build();

            MetadataTemplateResponse response = service.create("ontology-1", request, "user-1");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotBlank();
            assertThat(response.getName()).isEqualTo("标准物料模板");
            assertThat(response.getNameEn()).isEqualTo("Standard Material Template");
            assertThat(response.getDescription()).isEqualTo("物料主数据的标准元数据模板");
            assertThat(response.getDomain()).isEqualTo("material");
            assertThat(response.getTemplateType()).isEqualTo("entity");
            assertThat(response.getCreatedAt()).isNotNull();
            assertThat(response.getUpdatedAt()).isNotNull();

            verify(mapper).insert(poCaptor.capture());
            MetadataTemplatePO saved = poCaptor.getValue();
            assertThat(saved.getId()).isEqualTo(response.getId());
            assertThat(saved.getName()).isEqualTo("标准物料模板");
            assertThat(saved.getNameEn()).isEqualTo("Standard Material Template");
            assertThat(saved.getDescription()).isEqualTo("物料主数据的标准元数据模板");
            assertThat(saved.getDomain()).isEqualTo("material");
            assertThat(saved.getTemplateType()).isEqualTo("entity");
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("should return response when record exists")
        void getByIdFound() {
            Instant now = Instant.now();
            MetadataTemplatePO po = MetadataTemplatePO.builder()
                    .id("tpl-123")
                    .ontologyId("ontology-1")
                    .name("标准物料模板")
                    .nameEn("Standard Material Template")
                    .description("物料主数据的标准元数据模板")
                    .domain("material")
                    .templateType("entity")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            when(mapper.selectById("tpl-123")).thenReturn(po);

            MetadataTemplateResponse response = service.getById("tpl-123");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("tpl-123");
            assertThat(response.getName()).isEqualTo("标准物料模板");
            assertThat(response.getNameEn()).isEqualTo("Standard Material Template");
            assertThat(response.getDescription()).isEqualTo("物料主数据的标准元数据模板");
            assertThat(response.getDomain()).isEqualTo("material");
            assertThat(response.getTemplateType()).isEqualTo("entity");
            assertThat(response.getCreatedAt()).isEqualTo(now);
            assertThat(response.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("should return null when record does not exist")
        void getByIdNotFound() {
            when(mapper.selectById("nonexistent")).thenReturn(null);

            MetadataTemplateResponse response = service.getById("nonexistent");

            assertThat(response).isNull();
        }
    }

    @Nested
    @DisplayName("listByOntologyId()")
    class ListByOntologyIdTests {

        @Test
        @DisplayName("should return list of responses when records exist")
        void listByOntologyIdSuccess() {
            Instant now = Instant.now();
            MetadataTemplatePO po1 = MetadataTemplatePO.builder()
                    .id("tpl-1")
                    .ontologyId("ontology-1")
                    .name("物料模板")
                    .nameEn("Material Template")
                    .domain("material")
                    .templateType("entity")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            MetadataTemplatePO po2 = MetadataTemplatePO.builder()
                    .id("tpl-2")
                    .ontologyId("ontology-1")
                    .name("供应商模板")
                    .nameEn("Supplier Template")
                    .domain("supplier")
                    .templateType("entity")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            when(mapper.selectByOntologyId("ontology-1")).thenReturn(List.of(po1, po2));

            List<MetadataTemplateResponse> responses = service.listByOntologyId("ontology-1");

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getId()).isEqualTo("tpl-1");
            assertThat(responses.get(0).getName()).isEqualTo("物料模板");
            assertThat(responses.get(0).getDomain()).isEqualTo("material");
            assertThat(responses.get(1).getId()).isEqualTo("tpl-2");
            assertThat(responses.get(1).getName()).isEqualTo("供应商模板");
            assertThat(responses.get(1).getDomain()).isEqualTo("supplier");
        }

        @Test
        @DisplayName("should return empty list when no records exist")
        void listByOntologyIdEmpty() {
            when(mapper.selectByOntologyId("ontology-1")).thenReturn(Collections.emptyList());

            List<MetadataTemplateResponse> responses = service.listByOntologyId("ontology-1");

            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("should delete when record exists")
        void deleteSuccess() {
            MetadataTemplatePO po = MetadataTemplatePO.builder()
                    .id("tpl-123")
                    .build();

            when(mapper.selectById("tpl-123")).thenReturn(po);

            service.delete("tpl-123");

            verify(mapper).deleteById("tpl-123");
        }

        @Test
        @DisplayName("should skip delete when record does not exist")
        void deleteNotFound() {
            when(mapper.selectById("nonexistent")).thenReturn(null);

            service.delete("nonexistent");

            verify(mapper, never()).deleteById(anyString());
        }
    }
}

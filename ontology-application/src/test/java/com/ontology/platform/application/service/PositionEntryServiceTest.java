package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreatePositionEntryRequest;
import com.ontology.platform.application.dto.domain.PositionEntryResponse;
import com.ontology.platform.infrastructure.persistence.PositionEntryPO;
import com.ontology.platform.infrastructure.persistence.PositionEntryPOMapper;
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
 * Unit tests for {@link PositionEntryService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PositionEntryService Test")
class PositionEntryServiceTest {

    @Mock
    private PositionEntryPOMapper mapper;

    private PositionEntryService service;

    @Captor
    private ArgumentCaptor<PositionEntryPO> poCaptor;

    @BeforeEach
    void setUp() {
        service = new PositionEntryService(mapper);
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should create successfully and return response")
        void createSuccess() {
            when(mapper.insert(any())).thenReturn(1);

            CreatePositionEntryRequest request = CreatePositionEntryRequest.builder()
                    .name("软件工程师")
                    .nameEn("Software Engineer")
                    .description("负责软件开发")
                    .departmentId("dept-001")
                    .responsibilities("编写代码、代码审查")
                    .build();

            PositionEntryResponse response = service.create("ontology-1", request, "user-1");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotBlank();
            assertThat(response.getName()).isEqualTo("软件工程师");
            assertThat(response.getNameEn()).isEqualTo("Software Engineer");
            assertThat(response.getDescription()).isEqualTo("负责软件开发");
            assertThat(response.getDepartmentId()).isEqualTo("dept-001");
            assertThat(response.getResponsibilities()).isEqualTo("编写代码、代码审查");
            assertThat(response.getCreatedAt()).isNotNull();
            assertThat(response.getUpdatedAt()).isNotNull();

            verify(mapper).insert(poCaptor.capture());
            PositionEntryPO saved = poCaptor.getValue();
            assertThat(saved.getId()).isEqualTo(response.getId());
            assertThat(saved.getName()).isEqualTo("软件工程师");
            assertThat(saved.getNameEn()).isEqualTo("Software Engineer");
            assertThat(saved.getDescription()).isEqualTo("负责软件开发");
            assertThat(saved.getDepartmentId()).isEqualTo("dept-001");
            assertThat(saved.getResponsibilities()).isEqualTo("编写代码、代码审查");
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("should return response when record exists")
        void getByIdFound() {
            Instant now = Instant.now();
            PositionEntryPO po = PositionEntryPO.builder()
                    .id("pos-123")
                    .ontologyId("ontology-1")
                    .name("软件工程师")
                    .nameEn("Software Engineer")
                    .description("负责软件开发")
                    .departmentId("dept-001")
                    .responsibilities("编写代码、代码审查")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            when(mapper.selectById("pos-123")).thenReturn(po);

            PositionEntryResponse response = service.getById("pos-123");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("pos-123");
            assertThat(response.getName()).isEqualTo("软件工程师");
            assertThat(response.getNameEn()).isEqualTo("Software Engineer");
            assertThat(response.getDescription()).isEqualTo("负责软件开发");
            assertThat(response.getDepartmentId()).isEqualTo("dept-001");
            assertThat(response.getResponsibilities()).isEqualTo("编写代码、代码审查");
            assertThat(response.getCreatedAt()).isEqualTo(now);
            assertThat(response.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("should return null when record does not exist")
        void getByIdNotFound() {
            when(mapper.selectById("nonexistent")).thenReturn(null);

            PositionEntryResponse response = service.getById("nonexistent");

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
            PositionEntryPO po1 = PositionEntryPO.builder()
                    .id("pos-1")
                    .ontologyId("ontology-1")
                    .name("软件工程师")
                    .nameEn("Software Engineer")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            PositionEntryPO po2 = PositionEntryPO.builder()
                    .id("pos-2")
                    .ontologyId("ontology-1")
                    .name("产品经理")
                    .nameEn("Product Manager")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            when(mapper.selectByOntologyId("ontology-1")).thenReturn(List.of(po1, po2));

            List<PositionEntryResponse> responses = service.listByOntologyId("ontology-1");

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getId()).isEqualTo("pos-1");
            assertThat(responses.get(0).getName()).isEqualTo("软件工程师");
            assertThat(responses.get(1).getId()).isEqualTo("pos-2");
            assertThat(responses.get(1).getName()).isEqualTo("产品经理");
        }

        @Test
        @DisplayName("should return empty list when no records exist")
        void listByOntologyIdEmpty() {
            when(mapper.selectByOntologyId("ontology-1")).thenReturn(Collections.emptyList());

            List<PositionEntryResponse> responses = service.listByOntologyId("ontology-1");

            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("should delete when record exists")
        void deleteSuccess() {
            PositionEntryPO po = PositionEntryPO.builder()
                    .id("pos-123")
                    .build();

            when(mapper.selectById("pos-123")).thenReturn(po);

            service.delete("pos-123");

            verify(mapper).deleteById("pos-123");
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

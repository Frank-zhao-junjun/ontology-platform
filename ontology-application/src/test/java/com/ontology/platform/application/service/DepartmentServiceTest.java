package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateDepartmentRequest;
import com.ontology.platform.application.dto.domain.DepartmentResponse;
import com.ontology.platform.infrastructure.persistence.DepartmentPO;
import com.ontology.platform.infrastructure.persistence.DepartmentPOMapper;
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
 * Unit tests for {@link DepartmentService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentService Test")
class DepartmentServiceTest {

    @Mock
    private DepartmentPOMapper mapper;

    private DepartmentService service;

    @Captor
    private ArgumentCaptor<DepartmentPO> poCaptor;

    @BeforeEach
    void setUp() {
        service = new DepartmentService(mapper);
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should create successfully and return response")
        void createSuccess() {
            when(mapper.insert(any())).thenReturn(1);

            CreateDepartmentRequest request = CreateDepartmentRequest.builder()
                    .name("研发部")
                    .nameEn("R&D Department")
                    .description("负责产品研发")
                    .parentDepartmentId("dept-001")
                    .build();

            DepartmentResponse response = service.create("ontology-1", request, "user-1");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotBlank();
            assertThat(response.getName()).isEqualTo("研发部");
            assertThat(response.getNameEn()).isEqualTo("R&D Department");
            assertThat(response.getDescription()).isEqualTo("负责产品研发");
            assertThat(response.getParentDepartmentId()).isEqualTo("dept-001");
            assertThat(response.getCreatedAt()).isNotNull();
            assertThat(response.getUpdatedAt()).isNotNull();

            verify(mapper).insert(poCaptor.capture());
            DepartmentPO saved = poCaptor.getValue();
            assertThat(saved.getId()).isEqualTo(response.getId());
            assertThat(saved.getName()).isEqualTo("研发部");
            assertThat(saved.getNameEn()).isEqualTo("R&D Department");
            assertThat(saved.getDescription()).isEqualTo("负责产品研发");
            assertThat(saved.getParentDepartmentId()).isEqualTo("dept-001");
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("should return response when record exists")
        void getByIdFound() {
            Instant now = Instant.now();
            DepartmentPO po = DepartmentPO.builder()
                    .id("dept-123")
                    .ontologyId("ontology-1")
                    .name("研发部")
                    .nameEn("R&D Department")
                    .description("负责产品研发")
                    .parentDepartmentId("dept-001")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            when(mapper.selectById("dept-123")).thenReturn(po);

            DepartmentResponse response = service.getById("dept-123");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("dept-123");
            assertThat(response.getName()).isEqualTo("研发部");
            assertThat(response.getNameEn()).isEqualTo("R&D Department");
            assertThat(response.getDescription()).isEqualTo("负责产品研发");
            assertThat(response.getParentDepartmentId()).isEqualTo("dept-001");
            assertThat(response.getCreatedAt()).isEqualTo(now);
            assertThat(response.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("should return null when record does not exist")
        void getByIdNotFound() {
            when(mapper.selectById("nonexistent")).thenReturn(null);

            DepartmentResponse response = service.getById("nonexistent");

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
            DepartmentPO po1 = DepartmentPO.builder()
                    .id("dept-1")
                    .ontologyId("ontology-1")
                    .name("研发部")
                    .nameEn("R&D")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            DepartmentPO po2 = DepartmentPO.builder()
                    .id("dept-2")
                    .ontologyId("ontology-1")
                    .name("市场部")
                    .nameEn("Marketing")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            when(mapper.selectByOntologyId("ontology-1")).thenReturn(List.of(po1, po2));

            List<DepartmentResponse> responses = service.listByOntologyId("ontology-1");

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getId()).isEqualTo("dept-1");
            assertThat(responses.get(0).getName()).isEqualTo("研发部");
            assertThat(responses.get(1).getId()).isEqualTo("dept-2");
            assertThat(responses.get(1).getName()).isEqualTo("市场部");
        }

        @Test
        @DisplayName("should return empty list when no records exist")
        void listByOntologyIdEmpty() {
            when(mapper.selectByOntologyId("ontology-1")).thenReturn(Collections.emptyList());

            List<DepartmentResponse> responses = service.listByOntologyId("ontology-1");

            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("should delete when record exists")
        void deleteSuccess() {
            DepartmentPO po = DepartmentPO.builder()
                    .id("dept-123")
                    .build();

            when(mapper.selectById("dept-123")).thenReturn(po);

            service.delete("dept-123");

            verify(mapper).deleteById("dept-123");
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

package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.BusinessTermResponse;
import com.ontology.platform.application.dto.domain.CreateBusinessTermRequest;
import com.ontology.platform.infrastructure.persistence.BusinessTermPO;
import com.ontology.platform.infrastructure.persistence.BusinessTermPOMapper;
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
 * Unit tests for {@link BusinessTermService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessTermService Test")
class BusinessTermServiceTest {

    @Mock
    private BusinessTermPOMapper mapper;

    private BusinessTermService service;

    @Captor
    private ArgumentCaptor<BusinessTermPO> poCaptor;

    @BeforeEach
    void setUp() {
        service = new BusinessTermService(mapper);
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should create successfully and return response")
        void createSuccess() {
            when(mapper.insert(any())).thenReturn(1);

            CreateBusinessTermRequest request = CreateBusinessTermRequest.builder()
                    .name("客户生命周期")
                    .nameEn("Customer Lifecycle")
                    .definition("客户从初次接触到终止合作的完整过程")
                    .synonyms("[\"客户旅程\",\"Customer Journey\"]")
                    .build();

            BusinessTermResponse response = service.create("ontology-1", request, "user-1");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotBlank();
            assertThat(response.getName()).isEqualTo("客户生命周期");
            assertThat(response.getNameEn()).isEqualTo("Customer Lifecycle");
            assertThat(response.getDefinition()).isEqualTo("客户从初次接触到终止合作的完整过程");
            assertThat(response.getSynonyms()).isEqualTo("[\"客户旅程\",\"Customer Journey\"]");
            assertThat(response.getCreatedAt()).isNotNull();
            assertThat(response.getUpdatedAt()).isNotNull();

            verify(mapper).insert(poCaptor.capture());
            BusinessTermPO saved = poCaptor.getValue();
            assertThat(saved.getId()).isEqualTo(response.getId());
            assertThat(saved.getName()).isEqualTo("客户生命周期");
            assertThat(saved.getNameEn()).isEqualTo("Customer Lifecycle");
            assertThat(saved.getDefinition()).isEqualTo("客户从初次接触到终止合作的完整过程");
            assertThat(saved.getSynonyms()).isEqualTo("[\"客户旅程\",\"Customer Journey\"]");
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("should return response when record exists")
        void getByIdFound() {
            Instant now = Instant.now();
            BusinessTermPO po = BusinessTermPO.builder()
                    .id("term-123")
                    .ontologyId("ontology-1")
                    .name("客户生命周期")
                    .nameEn("Customer Lifecycle")
                    .definition("客户从初次接触到终止合作的完整过程")
                    .synonyms("[\"客户旅程\",\"Customer Journey\"]")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            when(mapper.selectById("term-123")).thenReturn(po);

            BusinessTermResponse response = service.getById("term-123");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("term-123");
            assertThat(response.getName()).isEqualTo("客户生命周期");
            assertThat(response.getNameEn()).isEqualTo("Customer Lifecycle");
            assertThat(response.getDefinition()).isEqualTo("客户从初次接触到终止合作的完整过程");
            assertThat(response.getSynonyms()).isEqualTo("[\"客户旅程\",\"Customer Journey\"]");
            assertThat(response.getCreatedAt()).isEqualTo(now);
            assertThat(response.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("should return null when record does not exist")
        void getByIdNotFound() {
            when(mapper.selectById("nonexistent")).thenReturn(null);

            BusinessTermResponse response = service.getById("nonexistent");

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
            BusinessTermPO po1 = BusinessTermPO.builder()
                    .id("term-1")
                    .ontologyId("ontology-1")
                    .name("客户生命周期")
                    .nameEn("Customer Lifecycle")
                    .definition("定义1")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            BusinessTermPO po2 = BusinessTermPO.builder()
                    .id("term-2")
                    .ontologyId("ontology-1")
                    .name("净推荐值")
                    .nameEn("Net Promoter Score")
                    .definition("定义2")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            when(mapper.selectByOntologyId("ontology-1")).thenReturn(List.of(po1, po2));

            List<BusinessTermResponse> responses = service.listByOntologyId("ontology-1");

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getId()).isEqualTo("term-1");
            assertThat(responses.get(0).getName()).isEqualTo("客户生命周期");
            assertThat(responses.get(1).getId()).isEqualTo("term-2");
            assertThat(responses.get(1).getName()).isEqualTo("净推荐值");
        }

        @Test
        @DisplayName("should return empty list when no records exist")
        void listByOntologyIdEmpty() {
            when(mapper.selectByOntologyId("ontology-1")).thenReturn(Collections.emptyList());

            List<BusinessTermResponse> responses = service.listByOntologyId("ontology-1");

            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("should delete when record exists")
        void deleteSuccess() {
            BusinessTermPO po = BusinessTermPO.builder()
                    .id("term-123")
                    .build();

            when(mapper.selectById("term-123")).thenReturn(po);

            service.delete("term-123");

            verify(mapper).deleteById("term-123");
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

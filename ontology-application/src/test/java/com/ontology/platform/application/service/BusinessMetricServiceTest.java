package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.BusinessMetricResponse;
import com.ontology.platform.application.dto.domain.CreateBusinessMetricRequest;
import com.ontology.platform.infrastructure.persistence.BusinessMetricPO;
import com.ontology.platform.infrastructure.persistence.BusinessMetricPOMapper;
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
 * Unit tests for {@link BusinessMetricService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessMetricService Test")
class BusinessMetricServiceTest {

    @Mock
    private BusinessMetricPOMapper mapper;

    private BusinessMetricService service;

    @Captor
    private ArgumentCaptor<BusinessMetricPO> poCaptor;

    @BeforeEach
    void setUp() {
        service = new BusinessMetricService(mapper);
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should create successfully and return response")
        void createSuccess() {
            when(mapper.insert(any())).thenReturn(1);

            CreateBusinessMetricRequest request = CreateBusinessMetricRequest.builder()
                    .name("营收增长率")
                    .nameEn("Revenue Growth Rate")
                    .description("年度营收同比增长率")
                    .formula("(current - previous) / previous * 100")
                    .dataSourceRef("financial_db")
                    .period("yearly")
                    .targetEntity("Company")
                    .build();

            BusinessMetricResponse response = service.create("ontology-1", request, "user-1");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotBlank();
            assertThat(response.getName()).isEqualTo("营收增长率");
            assertThat(response.getNameEn()).isEqualTo("Revenue Growth Rate");
            assertThat(response.getDescription()).isEqualTo("年度营收同比增长率");
            assertThat(response.getFormula()).isEqualTo("(current - previous) / previous * 100");
            assertThat(response.getDataSourceRef()).isEqualTo("financial_db");
            assertThat(response.getPeriod()).isEqualTo("yearly");
            assertThat(response.getTargetEntity()).isEqualTo("Company");
            assertThat(response.getCreatedAt()).isNotNull();
            assertThat(response.getUpdatedAt()).isNotNull();

            verify(mapper).insert(poCaptor.capture());
            BusinessMetricPO saved = poCaptor.getValue();
            assertThat(saved.getId()).isEqualTo(response.getId());
            assertThat(saved.getName()).isEqualTo("营收增长率");
            assertThat(saved.getNameEn()).isEqualTo("Revenue Growth Rate");
            assertThat(saved.getDescription()).isEqualTo("年度营收同比增长率");
            assertThat(saved.getFormula()).isEqualTo("(current - previous) / previous * 100");
            assertThat(saved.getDataSourceRef()).isEqualTo("financial_db");
            assertThat(saved.getPeriod()).isEqualTo("yearly");
            assertThat(saved.getTargetEntity()).isEqualTo("Company");
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("should return response when record exists")
        void getByIdFound() {
            Instant now = Instant.now();
            BusinessMetricPO po = BusinessMetricPO.builder()
                    .id("metric-123")
                    .ontologyId("ontology-1")
                    .name("营收增长率")
                    .nameEn("Revenue Growth Rate")
                    .description("年度营收同比增长率")
                    .formula("(current - previous) / previous * 100")
                    .dataSourceRef("financial_db")
                    .period("yearly")
                    .targetEntity("Company")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            when(mapper.selectById("metric-123")).thenReturn(po);

            BusinessMetricResponse response = service.getById("metric-123");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("metric-123");
            assertThat(response.getName()).isEqualTo("营收增长率");
            assertThat(response.getNameEn()).isEqualTo("Revenue Growth Rate");
            assertThat(response.getFormula()).isEqualTo("(current - previous) / previous * 100");
            assertThat(response.getDataSourceRef()).isEqualTo("financial_db");
            assertThat(response.getPeriod()).isEqualTo("yearly");
            assertThat(response.getTargetEntity()).isEqualTo("Company");
            assertThat(response.getCreatedAt()).isEqualTo(now);
            assertThat(response.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("should return null when record does not exist")
        void getByIdNotFound() {
            when(mapper.selectById("nonexistent")).thenReturn(null);

            BusinessMetricResponse response = service.getById("nonexistent");

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
            BusinessMetricPO po1 = BusinessMetricPO.builder()
                    .id("metric-1")
                    .ontologyId("ontology-1")
                    .name("营收增长率")
                    .nameEn("Revenue Growth Rate")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            BusinessMetricPO po2 = BusinessMetricPO.builder()
                    .id("metric-2")
                    .ontologyId("ontology-1")
                    .name("客户满意度")
                    .nameEn("Customer Satisfaction")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            when(mapper.selectByOntologyId("ontology-1")).thenReturn(List.of(po1, po2));

            List<BusinessMetricResponse> responses = service.listByOntologyId("ontology-1");

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getId()).isEqualTo("metric-1");
            assertThat(responses.get(0).getName()).isEqualTo("营收增长率");
            assertThat(responses.get(1).getId()).isEqualTo("metric-2");
            assertThat(responses.get(1).getName()).isEqualTo("客户满意度");
        }

        @Test
        @DisplayName("should return empty list when no records exist")
        void listByOntologyIdEmpty() {
            when(mapper.selectByOntologyId("ontology-1")).thenReturn(Collections.emptyList());

            List<BusinessMetricResponse> responses = service.listByOntologyId("ontology-1");

            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("should delete when record exists")
        void deleteSuccess() {
            BusinessMetricPO po = BusinessMetricPO.builder()
                    .id("metric-123")
                    .build();

            when(mapper.selectById("metric-123")).thenReturn(po);

            service.delete("metric-123");

            verify(mapper).deleteById("metric-123");
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

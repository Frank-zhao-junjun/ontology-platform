package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateOrchestrationRequest;
import com.ontology.platform.application.dto.domain.OrchestrationResponse;
import com.ontology.platform.infrastructure.persistence.OrchestrationPO;
import com.ontology.platform.infrastructure.persistence.OrchestrationPOMapper;
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
 * Unit tests for {@link OrchestrationService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrchestrationService Test")
class OrchestrationServiceTest {

    @Mock
    private OrchestrationPOMapper mapper;

    private OrchestrationService service;

    @Captor
    private ArgumentCaptor<OrchestrationPO> poCaptor;

    @BeforeEach
    void setUp() {
        service = new OrchestrationService(mapper);
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should create successfully and return response")
        void createSuccess() {
            when(mapper.insert(any())).thenReturn(1);

            CreateOrchestrationRequest request = CreateOrchestrationRequest.builder()
                    .name("订单处理流程")
                    .description("从下单到交付的完整流程编排")
                    .entryPoints("create_order,payment_callback")
                    .build();

            OrchestrationResponse response = service.create("ontology-1", request, "user-1");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotBlank();
            assertThat(response.getName()).isEqualTo("订单处理流程");
            assertThat(response.getDescription()).isEqualTo("从下单到交付的完整流程编排");
            assertThat(response.getEntryPoints()).isEqualTo("create_order,payment_callback");
            assertThat(response.getCreatedAt()).isNotNull();
            assertThat(response.getUpdatedAt()).isNotNull();

            verify(mapper).insert(poCaptor.capture());
            OrchestrationPO saved = poCaptor.getValue();
            assertThat(saved.getId()).isEqualTo(response.getId());
            assertThat(saved.getName()).isEqualTo("订单处理流程");
            assertThat(saved.getDescription()).isEqualTo("从下单到交付的完整流程编排");
            assertThat(saved.getEntryPoints()).isEqualTo("create_order,payment_callback");
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("should return response when record exists")
        void getByIdFound() {
            Instant now = Instant.now();
            OrchestrationPO po = OrchestrationPO.builder()
                    .id("orch-123")
                    .ontologyId("ontology-1")
                    .name("订单处理流程")
                    .description("从下单到交付的完整流程编排")
                    .entryPoints("create_order,payment_callback")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            when(mapper.selectById("orch-123")).thenReturn(po);

            OrchestrationResponse response = service.getById("orch-123");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("orch-123");
            assertThat(response.getName()).isEqualTo("订单处理流程");
            assertThat(response.getDescription()).isEqualTo("从下单到交付的完整流程编排");
            assertThat(response.getEntryPoints()).isEqualTo("create_order,payment_callback");
            assertThat(response.getCreatedAt()).isEqualTo(now);
            assertThat(response.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("should return null when record does not exist")
        void getByIdNotFound() {
            when(mapper.selectById("nonexistent")).thenReturn(null);

            OrchestrationResponse response = service.getById("nonexistent");

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
            OrchestrationPO po1 = OrchestrationPO.builder()
                    .id("orch-1")
                    .ontologyId("ontology-1")
                    .name("订单处理流程")
                    .entryPoints("create_order")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            OrchestrationPO po2 = OrchestrationPO.builder()
                    .id("orch-2")
                    .ontologyId("ontology-1")
                    .name("审批流程")
                    .entryPoints("submit_request")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            when(mapper.selectByOntologyId("ontology-1")).thenReturn(List.of(po1, po2));

            List<OrchestrationResponse> responses = service.listByOntologyId("ontology-1");

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getId()).isEqualTo("orch-1");
            assertThat(responses.get(0).getName()).isEqualTo("订单处理流程");
            assertThat(responses.get(1).getId()).isEqualTo("orch-2");
            assertThat(responses.get(1).getName()).isEqualTo("审批流程");
        }

        @Test
        @DisplayName("should return empty list when no records exist")
        void listByOntologyIdEmpty() {
            when(mapper.selectByOntologyId("ontology-1")).thenReturn(Collections.emptyList());

            List<OrchestrationResponse> responses = service.listByOntologyId("ontology-1");

            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("should delete when record exists")
        void deleteSuccess() {
            OrchestrationPO po = OrchestrationPO.builder()
                    .id("orch-123")
                    .build();

            when(mapper.selectById("orch-123")).thenReturn(po);

            service.delete("orch-123");

            verify(mapper).deleteById("orch-123");
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

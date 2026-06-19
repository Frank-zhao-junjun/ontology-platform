package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateProcessStepRequest;
import com.ontology.platform.application.dto.domain.ProcessStepResponse;
import com.ontology.platform.infrastructure.persistence.ProcessStepPO;
import com.ontology.platform.infrastructure.persistence.ProcessStepPOMapper;
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
 * Unit tests for {@link ProcessStepService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessStepService Test")
class ProcessStepServiceTest {

    @Mock
    private ProcessStepPOMapper mapper;

    private ProcessStepService service;

    @Captor
    private ArgumentCaptor<ProcessStepPO> poCaptor;

    @BeforeEach
    void setUp() {
        service = new ProcessStepService(mapper);
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should create successfully and return response")
        void createSuccess() {
            when(mapper.insert(any())).thenReturn(1);

            CreateProcessStepRequest request = CreateProcessStepRequest.builder()
                    .orchestrationId("orch-001")
                    .name("验证订单")
                    .stepType("validation")
                    .description("验证订单信息是否完整")
                    .sortOrder(1)
                    .config("{\"timeout\": 30}")
                    .build();

            ProcessStepResponse response = service.create("ontology-1", request, "user-1");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotBlank();
            assertThat(response.getOrchestrationId()).isEqualTo("orch-001");
            assertThat(response.getName()).isEqualTo("验证订单");
            assertThat(response.getStepType()).isEqualTo("validation");
            assertThat(response.getDescription()).isEqualTo("验证订单信息是否完整");
            assertThat(response.getSortOrder()).isEqualTo(1);
            assertThat(response.getConfig()).isEqualTo("{\"timeout\": 30}");
            assertThat(response.getCreatedAt()).isNotNull();
            assertThat(response.getUpdatedAt()).isNotNull();

            verify(mapper).insert(poCaptor.capture());
            ProcessStepPO saved = poCaptor.getValue();
            assertThat(saved.getId()).isEqualTo(response.getId());
            assertThat(saved.getOrchestrationId()).isEqualTo("orch-001");
            assertThat(saved.getName()).isEqualTo("验证订单");
            assertThat(saved.getStepType()).isEqualTo("validation");
            assertThat(saved.getDescription()).isEqualTo("验证订单信息是否完整");
            assertThat(saved.getSortOrder()).isEqualTo(1);
            assertThat(saved.getConfig()).isEqualTo("{\"timeout\": 30}");
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("should return response when record exists")
        void getByIdFound() {
            Instant now = Instant.now();
            ProcessStepPO po = ProcessStepPO.builder()
                    .id("step-123")
                    .ontologyId("ontology-1")
                    .orchestrationId("orch-001")
                    .name("验证订单")
                    .stepType("validation")
                    .description("验证订单信息是否完整")
                    .sortOrder(1)
                    .config("{\"timeout\": 30}")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            when(mapper.selectById("step-123")).thenReturn(po);

            ProcessStepResponse response = service.getById("step-123");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("step-123");
            assertThat(response.getOrchestrationId()).isEqualTo("orch-001");
            assertThat(response.getName()).isEqualTo("验证订单");
            assertThat(response.getStepType()).isEqualTo("validation");
            assertThat(response.getDescription()).isEqualTo("验证订单信息是否完整");
            assertThat(response.getSortOrder()).isEqualTo(1);
            assertThat(response.getConfig()).isEqualTo("{\"timeout\": 30}");
            assertThat(response.getCreatedAt()).isEqualTo(now);
            assertThat(response.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("should return null when record does not exist")
        void getByIdNotFound() {
            when(mapper.selectById("nonexistent")).thenReturn(null);

            ProcessStepResponse response = service.getById("nonexistent");

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
            ProcessStepPO po1 = ProcessStepPO.builder()
                    .id("step-1")
                    .ontologyId("ontology-1")
                    .orchestrationId("orch-001")
                    .name("验证订单")
                    .stepType("validation")
                    .sortOrder(1)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            ProcessStepPO po2 = ProcessStepPO.builder()
                    .id("step-2")
                    .ontologyId("ontology-1")
                    .orchestrationId("orch-001")
                    .name("处理支付")
                    .stepType("action")
                    .sortOrder(2)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            when(mapper.selectByOntologyId("ontology-1")).thenReturn(List.of(po1, po2));

            List<ProcessStepResponse> responses = service.listByOntologyId("ontology-1");

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getId()).isEqualTo("step-1");
            assertThat(responses.get(0).getName()).isEqualTo("验证订单");
            assertThat(responses.get(0).getSortOrder()).isEqualTo(1);
            assertThat(responses.get(1).getId()).isEqualTo("step-2");
            assertThat(responses.get(1).getName()).isEqualTo("处理支付");
            assertThat(responses.get(1).getSortOrder()).isEqualTo(2);
        }

        @Test
        @DisplayName("should return empty list when no records exist")
        void listByOntologyIdEmpty() {
            when(mapper.selectByOntologyId("ontology-1")).thenReturn(Collections.emptyList());

            List<ProcessStepResponse> responses = service.listByOntologyId("ontology-1");

            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("should delete when record exists")
        void deleteSuccess() {
            ProcessStepPO po = ProcessStepPO.builder()
                    .id("step-123")
                    .build();

            when(mapper.selectById("step-123")).thenReturn(po);

            service.delete("step-123");

            verify(mapper).deleteById("step-123");
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

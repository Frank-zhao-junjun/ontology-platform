package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.AgentIntentResponse;
import com.ontology.platform.application.dto.domain.CreateAgentIntentRequest;
import com.ontology.platform.infrastructure.persistence.AgentIntentPO;
import com.ontology.platform.infrastructure.persistence.AgentIntentPOMapper;
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
 * Unit tests for {@link AgentIntentService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AgentIntentService Test")
class AgentIntentServiceTest {

    @Mock
    private AgentIntentPOMapper mapper;

    private AgentIntentService service;

    @Captor
    private ArgumentCaptor<AgentIntentPO> poCaptor;

    @BeforeEach
    void setUp() {
        service = new AgentIntentService(mapper);
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should create successfully and return response")
        void createSuccess() {
            when(mapper.insert(any())).thenReturn(1);

            CreateAgentIntentRequest request = CreateAgentIntentRequest.builder()
                    .name("查询订单状态")
                    .description("根据订单号查询订单的当前状态")
                    .triggerPhrases("[\"查订单\",\"订单状态\",\"我的订单\"]")
                    .actionId("action-query-order")
                    .build();

            AgentIntentResponse response = service.create("ontology-1", request, "user-1");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotBlank();
            assertThat(response.getName()).isEqualTo("查询订单状态");
            assertThat(response.getDescription()).isEqualTo("根据订单号查询订单的当前状态");
            assertThat(response.getTriggerPhrases()).isEqualTo("[\"查订单\",\"订单状态\",\"我的订单\"]");
            assertThat(response.getActionId()).isEqualTo("action-query-order");
            assertThat(response.getCreatedAt()).isNotNull();
            assertThat(response.getUpdatedAt()).isNotNull();

            verify(mapper).insert(poCaptor.capture());
            AgentIntentPO saved = poCaptor.getValue();
            assertThat(saved.getId()).isEqualTo(response.getId());
            assertThat(saved.getName()).isEqualTo("查询订单状态");
            assertThat(saved.getDescription()).isEqualTo("根据订单号查询订单的当前状态");
            assertThat(saved.getTriggerPhrases()).isEqualTo("[\"查订单\",\"订单状态\",\"我的订单\"]");
            assertThat(saved.getActionId()).isEqualTo("action-query-order");
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("should return response when record exists")
        void getByIdFound() {
            Instant now = Instant.now();
            AgentIntentPO po = AgentIntentPO.builder()
                    .id("intent-123")
                    .ontologyId("ontology-1")
                    .name("查询订单状态")
                    .description("根据订单号查询订单的当前状态")
                    .triggerPhrases("[\"查订单\",\"订单状态\",\"我的订单\"]")
                    .actionId("action-query-order")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            when(mapper.selectById("intent-123")).thenReturn(po);

            AgentIntentResponse response = service.getById("intent-123");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("intent-123");
            assertThat(response.getName()).isEqualTo("查询订单状态");
            assertThat(response.getDescription()).isEqualTo("根据订单号查询订单的当前状态");
            assertThat(response.getTriggerPhrases()).isEqualTo("[\"查订单\",\"订单状态\",\"我的订单\"]");
            assertThat(response.getActionId()).isEqualTo("action-query-order");
            assertThat(response.getCreatedAt()).isEqualTo(now);
            assertThat(response.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("should return null when record does not exist")
        void getByIdNotFound() {
            when(mapper.selectById("nonexistent")).thenReturn(null);

            AgentIntentResponse response = service.getById("nonexistent");

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
            AgentIntentPO po1 = AgentIntentPO.builder()
                    .id("intent-1")
                    .ontologyId("ontology-1")
                    .name("查询订单状态")
                    .description("查询订单")
                    .triggerPhrases("[\"查订单\"]")
                    .actionId("action-1")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            AgentIntentPO po2 = AgentIntentPO.builder()
                    .id("intent-2")
                    .ontologyId("ontology-1")
                    .name("创建工单")
                    .description("创建工单")
                    .triggerPhrases("[\"创建工单\"]")
                    .actionId("action-2")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            when(mapper.selectByOntologyId("ontology-1")).thenReturn(List.of(po1, po2));

            List<AgentIntentResponse> responses = service.listByOntologyId("ontology-1");

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getId()).isEqualTo("intent-1");
            assertThat(responses.get(0).getName()).isEqualTo("查询订单状态");
            assertThat(responses.get(1).getId()).isEqualTo("intent-2");
            assertThat(responses.get(1).getName()).isEqualTo("创建工单");
        }

        @Test
        @DisplayName("should return empty list when no records exist")
        void listByOntologyIdEmpty() {
            when(mapper.selectByOntologyId("ontology-1")).thenReturn(Collections.emptyList());

            List<AgentIntentResponse> responses = service.listByOntologyId("ontology-1");

            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("should delete when record exists")
        void deleteSuccess() {
            AgentIntentPO po = AgentIntentPO.builder()
                    .id("intent-123")
                    .build();

            when(mapper.selectById("intent-123")).thenReturn(po);

            service.delete("intent-123");

            verify(mapper).deleteById("intent-123");
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

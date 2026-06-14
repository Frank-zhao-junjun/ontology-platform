package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.EpcStep;
import com.ontology.platform.infrastructure.converter.EpcStepConverter;
import com.ontology.platform.infrastructure.persistence.EpcStepPO;
import com.ontology.platform.infrastructure.persistence.EpcStepPOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

@DisplayName("EpcStepRepositoryImpl 单元测试")
class EpcStepRepositoryImplTest {

    private EpcStepRepositoryImpl repository;
    private EpcStepPOMapper mapper;
    private EpcStepConverter converter;

    private static final String TEST_ID = UUID.randomUUID().toString();
    private static final String TEST_ONTOLOGY_ID = UUID.randomUUID().toString();
    private static final String TEST_FLOW_NAME = "order_flow";
    private static final String TEST_ACTION_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        mapper = Mockito.mock(EpcStepPOMapper.class);
        converter = new EpcStepConverter();
        repository = new EpcStepRepositoryImpl(mapper, converter);
    }

    @Nested
    @DisplayName("findById - 根据ID查询")
    class FindByIdTests {

        @Test
        @DisplayName("存在时返回对应实体")
        void shouldReturnEntityWhenFound() {
            // Arrange
            EpcStepPO po = createTestPO();
            Mockito.when(mapper.selectById(TEST_ID)).thenReturn(po);

            // Act
            Optional<EpcStep> result = repository.findById(TEST_ID);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(TEST_ID);
            assertThat(result.get().getOntologyId()).isEqualTo(TEST_ONTOLOGY_ID);
            assertThat(result.get().getFlowName()).isEqualTo(TEST_FLOW_NAME);
            assertThat(result.get().getStepOrder()).isEqualTo(1);
            assertThat(result.get().getActionId()).isEqualTo(TEST_ACTION_ID);
            assertThat(result.get().getTimeoutMs()).isEqualTo(60000);
        }

        @Test
        @DisplayName("不存在时返回空Optional")
        void shouldReturnEmptyWhenNotFound() {
            // Arrange
            Mockito.when(mapper.selectById("non-existing")).thenReturn(null);

            // Act
            Optional<EpcStep> result = repository.findById("non-existing");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOntologyId - 根据本体ID查询")
    class FindByOntologyIdTests {

        @Test
        @DisplayName("返回匹配的步骤列表")
        void shouldReturnStepsByOntologyId() {
            // Arrange
            EpcStepPO po1 = createTestPO();
            EpcStepPO po2 = EpcStepPO.builder()
                    .id(UUID.randomUUID().toString())
                    .ontologyId(TEST_ONTOLOGY_ID)
                    .flowName(TEST_FLOW_NAME)
                    .stepOrder(2)
                    .triggerEventId(UUID.randomUUID().toString())
                    .actionId(UUID.randomUUID().toString())
                    .conditions("[]")
                    .guards("[]")
                    .timeoutMs(30000)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            Mockito.when(mapper.selectByOntologyId(TEST_ONTOLOGY_ID)).thenReturn(List.of(po1, po2));

            // Act
            List<EpcStep> result = repository.findByOntologyId(TEST_ONTOLOGY_ID);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(s -> s.getOntologyId().equals(TEST_ONTOLOGY_ID));
        }

        @Test
        @DisplayName("无匹配时返回空列表")
        void shouldReturnEmptyListWhenNoMatch() {
            // Arrange
            Mockito.when(mapper.selectByOntologyId("unknown")).thenReturn(List.of());

            // Act
            List<EpcStep> result = repository.findByOntologyId("unknown");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOntologyIdAndFlowName - 根据本体ID和流程名称查询")
    class FindByOntologyIdAndFlowNameTests {

        @Test
        @DisplayName("返回匹配的步骤列表")
        void shouldReturnStepsByOntologyIdAndFlowName() {
            // Arrange
            EpcStepPO po = createTestPO();
            Mockito.when(mapper.selectByOntologyIdAndFlowName(TEST_ONTOLOGY_ID, TEST_FLOW_NAME))
                    .thenReturn(List.of(po));

            // Act
            List<EpcStep> result = repository.findByOntologyIdAndFlowName(TEST_ONTOLOGY_ID, TEST_FLOW_NAME);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getOntologyId()).isEqualTo(TEST_ONTOLOGY_ID);
            assertThat(result.get(0).getFlowName()).isEqualTo(TEST_FLOW_NAME);
        }

        @Test
        @DisplayName("无匹配时返回空列表")
        void shouldReturnEmptyListWhenNoMatch() {
            // Arrange
            Mockito.when(mapper.selectByOntologyIdAndFlowName("unknown", "unknown"))
                    .thenReturn(List.of());

            // Act
            List<EpcStep> result = repository.findByOntologyIdAndFlowName("unknown", "unknown");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByFlowNameOrderByStepOrder - 根据流程名称排序查询")
    class FindByFlowNameOrderByStepOrderTests {

        @Test
        @DisplayName("返回按stepOrder排序的步骤列表")
        void shouldReturnStepsOrderedByStepOrder() {
            // Arrange
            EpcStepPO po1 = EpcStepPO.builder()
                    .id(UUID.randomUUID().toString())
                    .ontologyId(TEST_ONTOLOGY_ID)
                    .flowName(TEST_FLOW_NAME)
                    .stepOrder(1)
                    .actionId(UUID.randomUUID().toString())
                    .conditions("[]")
                    .guards("[]")
                    .timeoutMs(60000)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            EpcStepPO po2 = EpcStepPO.builder()
                    .id(UUID.randomUUID().toString())
                    .ontologyId(TEST_ONTOLOGY_ID)
                    .flowName(TEST_FLOW_NAME)
                    .stepOrder(2)
                    .actionId(UUID.randomUUID().toString())
                    .conditions("[]")
                    .guards("[]")
                    .timeoutMs(30000)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            // mapper returns already ordered list
            Mockito.when(mapper.selectByFlowNameOrderByStepOrder(TEST_FLOW_NAME))
                    .thenReturn(List.of(po1, po2));

            // Act
            List<EpcStep> result = repository.findByFlowNameOrderByStepOrder(TEST_FLOW_NAME);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getStepOrder()).isEqualTo(1);
            assertThat(result.get(1).getStepOrder()).isEqualTo(2);
            assertThat(result).allMatch(s -> s.getFlowName().equals(TEST_FLOW_NAME));
        }

        @Test
        @DisplayName("无匹配时返回空列表")
        void shouldReturnEmptyListWhenNoMatch() {
            // Arrange
            Mockito.when(mapper.selectByFlowNameOrderByStepOrder("unknown")).thenReturn(List.of());

            // Act
            List<EpcStep> result = repository.findByFlowNameOrderByStepOrder("unknown");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save - 保存步骤（插入或更新）")
    class SaveTests {

        @Test
        @DisplayName("新记录时执行insert并设置createdAt和updatedAt")
        void shouldInsertWhenNew() {
            // Arrange
            EpcStep entity = EpcStep.builder()
                    .id(TEST_ID)
                    .ontologyId(TEST_ONTOLOGY_ID)
                    .flowName(TEST_FLOW_NAME)
                    .stepOrder(1)
                    .actionId(TEST_ACTION_ID)
                    .conditions("[]")
                    .guards("[]")
                    .timeoutMs(60000)
                    .build();
            Mockito.when(mapper.selectById(TEST_ID)).thenReturn(null);

            // Act
            EpcStep result = repository.save(entity);

            // Assert
            assertThat(result).isSameAs(entity);
            Mockito.verify(mapper).insert(any(EpcStepPO.class));
            Mockito.verify(mapper, Mockito.never()).updateById(any());
        }

        @Test
        @DisplayName("已存在记录时执行update并更新updatedAt")
        void shouldUpdateWhenExists() {
            // Arrange
            EpcStep entity = EpcStep.builder()
                    .id(TEST_ID)
                    .ontologyId(TEST_ONTOLOGY_ID)
                    .flowName(TEST_FLOW_NAME)
                    .stepOrder(2)
                    .actionId(TEST_ACTION_ID)
                    .conditions("[]")
                    .guards("[]")
                    .timeoutMs(30000)
                    .build();
            Mockito.when(mapper.selectById(TEST_ID)).thenReturn(createTestPO());

            // Act
            EpcStep result = repository.save(entity);

            // Assert
            assertThat(result).isSameAs(entity);
            Mockito.verify(mapper, Mockito.never()).insert(any());
            Mockito.verify(mapper).updateById(any(EpcStepPO.class));
        }

        @Test
        @DisplayName("insert时PO的createdAt不应为null")
        void shouldSetCreatedAtOnInsert() {
            // Arrange
            EpcStep entity = EpcStep.builder()
                    .id(TEST_ID)
                    .ontologyId(TEST_ONTOLOGY_ID)
                    .flowName(TEST_FLOW_NAME)
                    .stepOrder(1)
                    .actionId(TEST_ACTION_ID)
                    .conditions("[]")
                    .guards("[]")
                    .timeoutMs(60000)
                    .build();
            Mockito.when(mapper.selectById(TEST_ID)).thenReturn(null);

            // Act
            repository.save(entity);

            // Assert
            Mockito.verify(mapper).insert(Mockito.argThat(po ->
                    po.getCreatedAt() != null && po.getUpdatedAt() != null));
        }

        @Test
        @DisplayName("update时PO的updatedAt应更新")
        void shouldSetUpdatedAtOnUpdate() {
            // Arrange
            Instant oldUpdatedAt = Instant.now().minusSeconds(3600);
            EpcStep entity = EpcStep.builder()
                    .id(TEST_ID)
                    .ontologyId(TEST_ONTOLOGY_ID)
                    .flowName(TEST_FLOW_NAME)
                    .stepOrder(1)
                    .actionId(TEST_ACTION_ID)
                    .conditions("[]")
                    .guards("[]")
                    .timeoutMs(60000)
                    .createdAt(Instant.now())
                    .updatedAt(oldUpdatedAt)
                    .build();
            EpcStepPO existingPo = createTestPO();
            Mockito.when(mapper.selectById(TEST_ID)).thenReturn(existingPo);

            // Act
            repository.save(entity);

            // Assert
            Mockito.verify(mapper).updateById(Mockito.argThat(po ->
                    po.getUpdatedAt() != null && !po.getUpdatedAt().equals(oldUpdatedAt)));
        }
    }

    @Nested
    @DisplayName("deleteById - 硬删除")
    class DeleteByIdTests {

        @Test
        @DisplayName("调用mapper的deleteById")
        void shouldHardDeleteById() {
            // Act
            repository.deleteById(TEST_ID);

            // Assert
            Mockito.verify(mapper).deleteById(TEST_ID);
        }

        @Test
        @DisplayName("任意ID均可正常调用")
        void shouldHandleAnyId() {
            // Act
            repository.deleteById("any-id");

            // Assert
            Mockito.verify(mapper).deleteById("any-id");
        }
    }

    // ==================== Helper Methods ====================

    private EpcStepPO createTestPO() {
        return EpcStepPO.builder()
                .id(TEST_ID)
                .ontologyId(TEST_ONTOLOGY_ID)
                .flowName(TEST_FLOW_NAME)
                .stepOrder(1)
                .triggerEventId(UUID.randomUUID().toString())
                .actionId(TEST_ACTION_ID)
                .conditions("[]")
                .guards("[]")
                .timeoutMs(60000)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.Causality;
import com.ontology.platform.infrastructure.converter.CausalityConverter;
import com.ontology.platform.infrastructure.persistence.CausalityPO;
import com.ontology.platform.infrastructure.persistence.CausalityPOMapper;
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

@DisplayName("CausalityRepositoryImpl 单元测试")
class CausalityRepositoryImplTest {

    private CausalityRepositoryImpl repository;
    private CausalityPOMapper mapper;
    private CausalityConverter converter;

    private static final String TEST_ID = UUID.randomUUID().toString();
    private static final String TEST_ONTOLOGY_ID = UUID.randomUUID().toString();
    private static final String TEST_CAUSE_EVENT_ID = UUID.randomUUID().toString();
    private static final String TEST_EFFECT_EVENT_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        mapper = Mockito.mock(CausalityPOMapper.class);
        converter = new CausalityConverter();
        repository = new CausalityRepositoryImpl(mapper, converter);
    }

    @Nested
    @DisplayName("findById - 根据ID查询")
    class FindByIdTests {

        @Test
        @DisplayName("存在时返回对应实体")
        void shouldReturnEntityWhenFound() {
            // Arrange
            CausalityPO po = createTestPO();
            Mockito.when(mapper.selectById(TEST_ID)).thenReturn(po);

            // Act
            Optional<Causality> result = repository.findById(TEST_ID);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(TEST_ID);
            assertThat(result.get().getOntologyId()).isEqualTo(TEST_ONTOLOGY_ID);
            assertThat(result.get().getCauseEventId()).isEqualTo(TEST_CAUSE_EVENT_ID);
            assertThat(result.get().getEffectEventId()).isEqualTo(TEST_EFFECT_EVENT_ID);
            assertThat(result.get().getDelayMs()).isEqualTo(500);
            assertThat(result.get().getCondition()).isEqualTo("status == 'COMPLETED'");
        }

        @Test
        @DisplayName("不存在时返回空Optional")
        void shouldReturnEmptyWhenNotFound() {
            // Arrange
            Mockito.when(mapper.selectById("non-existing")).thenReturn(null);

            // Act
            Optional<Causality> result = repository.findById("non-existing");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOntologyId - 根据本体ID查询")
    class FindByOntologyIdTests {

        @Test
        @DisplayName("返回匹配的因果关系列表")
        void shouldReturnCausalitiesByOntologyId() {
            // Arrange
            CausalityPO po1 = createTestPO();
            CausalityPO po2 = CausalityPO.builder()
                    .id(UUID.randomUUID().toString())
                    .ontologyId(TEST_ONTOLOGY_ID)
                    .causeEventId(UUID.randomUUID().toString())
                    .effectEventId(UUID.randomUUID().toString())
                    .description("second causality")
                    .delayMs(1000)
                    .condition("true")
                    .createdAt(Instant.now())
                    .build();
            Mockito.when(mapper.selectByOntologyId(TEST_ONTOLOGY_ID)).thenReturn(List.of(po1, po2));

            // Act
            List<Causality> result = repository.findByOntologyId(TEST_ONTOLOGY_ID);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(c -> c.getOntologyId().equals(TEST_ONTOLOGY_ID));
        }

        @Test
        @DisplayName("无匹配时返回空列表")
        void shouldReturnEmptyListWhenNoMatch() {
            // Arrange
            Mockito.when(mapper.selectByOntologyId("unknown")).thenReturn(List.of());

            // Act
            List<Causality> result = repository.findByOntologyId("unknown");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCauseEventId - 根据原因事件ID查询")
    class FindByCauseEventIdTests {

        @Test
        @DisplayName("返回匹配的因果关系列表")
        void shouldReturnCausalitiesByCauseEventId() {
            // Arrange
            CausalityPO po = createTestPO();
            Mockito.when(mapper.selectByCauseEventId(TEST_CAUSE_EVENT_ID)).thenReturn(List.of(po));

            // Act
            List<Causality> result = repository.findByCauseEventId(TEST_CAUSE_EVENT_ID);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCauseEventId()).isEqualTo(TEST_CAUSE_EVENT_ID);
        }

        @Test
        @DisplayName("无匹配时返回空列表")
        void shouldReturnEmptyListWhenNoMatch() {
            // Arrange
            Mockito.when(mapper.selectByCauseEventId("unknown")).thenReturn(List.of());

            // Act
            List<Causality> result = repository.findByCauseEventId("unknown");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEffectEventId - 根据结果事件ID查询")
    class FindByEffectEventIdTests {

        @Test
        @DisplayName("返回匹配的因果关系列表")
        void shouldReturnCausalitiesByEffectEventId() {
            // Arrange
            CausalityPO po = createTestPO();
            Mockito.when(mapper.selectByEffectEventId(TEST_EFFECT_EVENT_ID)).thenReturn(List.of(po));

            // Act
            List<Causality> result = repository.findByEffectEventId(TEST_EFFECT_EVENT_ID);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEffectEventId()).isEqualTo(TEST_EFFECT_EVENT_ID);
        }

        @Test
        @DisplayName("无匹配时返回空列表")
        void shouldReturnEmptyListWhenNoMatch() {
            // Arrange
            Mockito.when(mapper.selectByEffectEventId("unknown")).thenReturn(List.of());

            // Act
            List<Causality> result = repository.findByEffectEventId("unknown");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save - 保存因果关系（插入或更新）")
    class SaveTests {

        @Test
        @DisplayName("新记录时执行insert")
        void shouldInsertWhenNew() {
            // Arrange
            Causality entity = Causality.builder()
                    .id(TEST_ID)
                    .ontologyId(TEST_ONTOLOGY_ID)
                    .causeEventId(TEST_CAUSE_EVENT_ID)
                    .effectEventId(TEST_EFFECT_EVENT_ID)
                    .description("test causality")
                    .delayMs(500)
                    .condition("status == 'COMPLETED'")
                    .createdAt(Instant.now())
                    .build();
            Mockito.when(mapper.selectById(TEST_ID)).thenReturn(null);

            // Act
            Causality result = repository.save(entity);

            // Assert
            assertThat(result).isSameAs(entity);
            Mockito.verify(mapper).insert(any(CausalityPO.class));
            Mockito.verify(mapper, Mockito.never()).updateById(any());
        }

        @Test
        @DisplayName("已存在记录时执行update")
        void shouldUpdateWhenExists() {
            // Arrange
            Causality entity = Causality.builder()
                    .id(TEST_ID)
                    .ontologyId(TEST_ONTOLOGY_ID)
                    .causeEventId(TEST_CAUSE_EVENT_ID)
                    .effectEventId(TEST_EFFECT_EVENT_ID)
                    .description("updated description")
                    .delayMs(1000)
                    .condition("new_condition")
                    .createdAt(Instant.now())
                    .build();
            Mockito.when(mapper.selectById(TEST_ID)).thenReturn(createTestPO());

            // Act
            Causality result = repository.save(entity);

            // Assert
            assertThat(result).isSameAs(entity);
            Mockito.verify(mapper, Mockito.never()).insert(any());
            Mockito.verify(mapper).updateById(any(CausalityPO.class));
        }

        @Test
        @DisplayName("insert时PO包含所有字段")
        void shouldInsertWithAllFields() {
            // Arrange
            Causality entity = Causality.builder()
                    .id(TEST_ID)
                    .ontologyId(TEST_ONTOLOGY_ID)
                    .causeEventId(TEST_CAUSE_EVENT_ID)
                    .effectEventId(TEST_EFFECT_EVENT_ID)
                    .description("test causality")
                    .delayMs(500)
                    .condition("status == 'COMPLETED'")
                    .createdAt(Instant.now())
                    .build();
            Mockito.when(mapper.selectById(TEST_ID)).thenReturn(null);

            // Act
            repository.save(entity);

            // Assert
            Mockito.verify(mapper).insert(Mockito.argThat(po ->
                    TEST_ID.equals(po.getId()) &&
                    TEST_ONTOLOGY_ID.equals(po.getOntologyId()) &&
                    TEST_CAUSE_EVENT_ID.equals(po.getCauseEventId()) &&
                    TEST_EFFECT_EVENT_ID.equals(po.getEffectEventId()) &&
                    po.getDelayMs() == 500 &&
                    "status == 'COMPLETED'".equals(po.getCondition())));
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

    private CausalityPO createTestPO() {
        return CausalityPO.builder()
                .id(TEST_ID)
                .ontologyId(TEST_ONTOLOGY_ID)
                .causeEventId(TEST_CAUSE_EVENT_ID)
                .effectEventId(TEST_EFFECT_EVENT_ID)
                .description("test causality")
                .delayMs(500)
                .condition("status == 'COMPLETED'")
                .createdAt(Instant.now())
                .build();
    }
}

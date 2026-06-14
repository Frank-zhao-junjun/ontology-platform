package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.DomainEvent;
import com.ontology.platform.infrastructure.converter.DomainEventConverter;
import com.ontology.platform.infrastructure.persistence.DomainEventPO;
import com.ontology.platform.infrastructure.persistence.DomainEventPOMapper;
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

@DisplayName("DomainEventRepositoryImpl 单元测试")
class DomainEventRepositoryImplTest {

    private DomainEventRepositoryImpl repository;
    private DomainEventPOMapper mapper;
    private DomainEventConverter converter;

    private static final String TEST_ID = UUID.randomUUID().toString();
    private static final String TEST_ONTOLOGY_ID = UUID.randomUUID().toString();
    private static final String TEST_ENTITY_ID = UUID.randomUUID().toString();
    private static final String TEST_EVENT_TYPE = "CREATED";

    @BeforeEach
    void setUp() {
        mapper = Mockito.mock(DomainEventPOMapper.class);
        converter = new DomainEventConverter();
        repository = new DomainEventRepositoryImpl(mapper, converter);
    }

    @Nested
    @DisplayName("findById - 根据ID查询")
    class FindByIdTests {

        @Test
        @DisplayName("存在时返回对应实体")
        void shouldReturnEntityWhenFound() {
            // Arrange
            DomainEventPO po = createTestPO();
            Mockito.when(mapper.selectById(TEST_ID)).thenReturn(po);

            // Act
            Optional<DomainEvent> result = repository.findById(TEST_ID);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(TEST_ID);
            assertThat(result.get().getOntologyId()).isEqualTo(TEST_ONTOLOGY_ID);
            assertThat(result.get().getEntityId()).isEqualTo(TEST_ENTITY_ID);
            assertThat(result.get().getEventType()).isEqualTo(TEST_EVENT_TYPE);
            assertThat(result.get().getName()).isEqualTo("test-event");
            assertThat(result.get().getDeleted()).isFalse();
        }

        @Test
        @DisplayName("不存在时返回空Optional")
        void shouldReturnEmptyWhenNotFound() {
            // Arrange
            Mockito.when(mapper.selectById("non-existing")).thenReturn(null);

            // Act
            Optional<DomainEvent> result = repository.findById("non-existing");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOntologyId - 根据本体ID查询")
    class FindByOntologyIdTests {

        @Test
        @DisplayName("返回匹配的事件列表")
        void shouldReturnEventsByOntologyId() {
            // Arrange
            DomainEventPO po1 = createTestPO();
            DomainEventPO po2 = DomainEventPO.builder()
                    .id(UUID.randomUUID().toString())
                    .ontologyId(TEST_ONTOLOGY_ID)
                    .entityId("entity-2")
                    .name("test-event-2")
                    .eventType("UPDATED")
                    .severity("WARN")
                    .payloadSchema("{}")
                    .deleted(false)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            Mockito.when(mapper.selectByOntologyId(TEST_ONTOLOGY_ID)).thenReturn(List.of(po1, po2));

            // Act
            List<DomainEvent> result = repository.findByOntologyId(TEST_ONTOLOGY_ID);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(e -> e.getOntologyId().equals(TEST_ONTOLOGY_ID));
        }

        @Test
        @DisplayName("无匹配时返回空列表")
        void shouldReturnEmptyListWhenNoMatch() {
            // Arrange
            Mockito.when(mapper.selectByOntologyId("unknown")).thenReturn(List.of());

            // Act
            List<DomainEvent> result = repository.findByOntologyId("unknown");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOntologyIdAndEntityId - 根据本体ID和实体ID查询")
    class FindByOntologyIdAndEntityIdTests {

        @Test
        @DisplayName("返回匹配的事件列表")
        void shouldReturnEventsByOntologyIdAndEntityId() {
            // Arrange
            DomainEventPO po = createTestPO();
            Mockito.when(mapper.selectByOntologyIdAndEntityId(TEST_ONTOLOGY_ID, TEST_ENTITY_ID))
                    .thenReturn(List.of(po));

            // Act
            List<DomainEvent> result = repository.findByOntologyIdAndEntityId(TEST_ONTOLOGY_ID, TEST_ENTITY_ID);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEntityId()).isEqualTo(TEST_ENTITY_ID);
        }
    }

    @Nested
    @DisplayName("findByOntologyIdAndEventType - 根据本体ID和事件类型查询")
    class FindByOntologyIdAndEventTypeTests {

        @Test
        @DisplayName("返回匹配的事件列表")
        void shouldReturnEventsByOntologyIdAndEventType() {
            // Arrange
            DomainEventPO po = createTestPO();
            Mockito.when(mapper.selectByOntologyIdAndEventType(TEST_ONTOLOGY_ID, TEST_EVENT_TYPE))
                    .thenReturn(List.of(po));

            // Act
            List<DomainEvent> result = repository.findByOntologyIdAndEventType(TEST_ONTOLOGY_ID, TEST_EVENT_TYPE);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEventType()).isEqualTo(TEST_EVENT_TYPE);
        }
    }

    @Nested
    @DisplayName("save - 保存事件（插入或更新）")
    class SaveTests {

        @Test
        @DisplayName("新记录时执行insert并设置createdAt和updatedAt")
        void shouldInsertWhenNew() {
            // Arrange
            DomainEvent entity = DomainEvent.builder()
                    .id(TEST_ID)
                    .ontologyId(TEST_ONTOLOGY_ID)
                    .entityId(TEST_ENTITY_ID)
                    .name("test-event")
                    .eventType(TEST_EVENT_TYPE)
                    .severity("INFO")
                    .payloadSchema("{}")
                    .deleted(false)
                    .build();
            Mockito.when(mapper.selectById(TEST_ID)).thenReturn(null);

            // Act
            DomainEvent result = repository.save(entity);

            // Assert
            assertThat(result).isSameAs(entity);
            Mockito.verify(mapper).insert(any(DomainEventPO.class));
            Mockito.verify(mapper, Mockito.never()).updateById(any());
        }

        @Test
        @DisplayName("已存在记录时执行update并更新updatedAt")
        void shouldUpdateWhenExists() {
            // Arrange
            DomainEvent entity = DomainEvent.builder()
                    .id(TEST_ID)
                    .ontologyId(TEST_ONTOLOGY_ID)
                    .entityId(TEST_ENTITY_ID)
                    .name("test-event")
                    .eventType(TEST_EVENT_TYPE)
                    .severity("INFO")
                    .payloadSchema("{}")
                    .deleted(false)
                    .build();
            Mockito.when(mapper.selectById(TEST_ID)).thenReturn(createTestPO());

            // Act
            DomainEvent result = repository.save(entity);

            // Assert
            assertThat(result).isSameAs(entity);
            Mockito.verify(mapper, Mockito.never()).insert(any());
            Mockito.verify(mapper).updateById(any(DomainEventPO.class));
        }

        @Test
        @DisplayName("insert时PO的createdAt不应为null")
        void shouldSetCreatedAtOnInsert() {
            // Arrange
            DomainEvent entity = DomainEvent.builder()
                    .id(TEST_ID)
                    .ontologyId(TEST_ONTOLOGY_ID)
                    .entityId(TEST_ENTITY_ID)
                    .name("test-event")
                    .eventType(TEST_EVENT_TYPE)
                    .severity("INFO")
                    .payloadSchema("{}")
                    .deleted(false)
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
            DomainEvent entity = DomainEvent.builder()
                    .id(TEST_ID)
                    .ontologyId(TEST_ONTOLOGY_ID)
                    .entityId(TEST_ENTITY_ID)
                    .name("test-event")
                    .eventType(TEST_EVENT_TYPE)
                    .severity("INFO")
                    .payloadSchema("{}")
                    .deleted(false)
                    .createdAt(Instant.now())
                    .updatedAt(oldUpdatedAt)
                    .build();
            DomainEventPO existingPo = createTestPO();
            Mockito.when(mapper.selectById(TEST_ID)).thenReturn(existingPo);

            // Act
            repository.save(entity);

            // Assert
            Mockito.verify(mapper).updateById(Mockito.argThat(po ->
                    po.getUpdatedAt() != null && !po.getUpdatedAt().equals(oldUpdatedAt)));
        }
    }

    @Nested
    @DisplayName("deleteById - 软删除")
    class DeleteByIdTests {

        @Test
        @DisplayName("存在时设置deleted=true并更新")
        void shouldSoftDeleteWhenExists() {
            // Arrange
            DomainEventPO po = createTestPO();
            Mockito.when(mapper.selectById(TEST_ID)).thenReturn(po);

            // Act
            repository.deleteById(TEST_ID);

            // Assert
            Mockito.verify(mapper).updateById(Mockito.argThat(updatedPo ->
                    updatedPo.getDeleted() == true && updatedPo.getUpdatedAt() != null));
        }

        @Test
        @DisplayName("不存在时不做任何操作")
        void shouldDoNothingWhenNotExists() {
            // Arrange
            Mockito.when(mapper.selectById("non-existing")).thenReturn(null);

            // Act
            repository.deleteById("non-existing");

            // Assert
            Mockito.verify(mapper, Mockito.never()).updateById(any());
        }
    }

    // ==================== Helper Methods ====================

    private DomainEventPO createTestPO() {
        return DomainEventPO.builder()
                .id(TEST_ID)
                .ontologyId(TEST_ONTOLOGY_ID)
                .entityId(TEST_ENTITY_ID)
                .name("test-event")
                .displayName("Test Event")
                .description("A test domain event")
                .eventType(TEST_EVENT_TYPE)
                .severity("INFO")
                .payloadSchema("{}")
                .source("test")
                .deleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

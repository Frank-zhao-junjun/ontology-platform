package com.ontology.platform.infrastructure.repository.behavior;

import com.ontology.platform.domain.entity.StateMachine;
import com.ontology.platform.infrastructure.converter.StateMachineConverter;
import com.ontology.platform.infrastructure.persistence.StateMachinePO;
import com.ontology.platform.infrastructure.persistence.StateMachinePOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

@DisplayName("StateMachineRepositoryImpl Unit Tests")
class StateMachineRepositoryImplTest {

    private StateMachineRepositoryImpl repository;
    private StateMachinePOMapper mapper;
    private StateMachineConverter converter;

    @BeforeEach
    void setUp() {
        mapper = Mockito.mock(StateMachinePOMapper.class);
        converter = new StateMachineConverter();
        repository = new StateMachineRepositoryImpl(mapper, converter);
    }

    // ----------------------------------------------------------------
    // 1. findById – hit
    // ----------------------------------------------------------------
    @Test
    @DisplayName("findById should return entity when PO exists in DB")
    void findById_hit() {
        // given
        StateMachinePO po = StateMachinePO.builder()
                .id("sm-001")
                .ontologyId("onto-1")
                .entityId("entity-1")
                .name("order-workflow")
                .initialState("CREATED")
                .states("[\"CREATED\",\"PAID\",\"SHIPPED\",\"DELIVERED\"]")
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2025-01-01T00:00:00Z"))
                .deleted(false)
                .build();

        Mockito.when(mapper.selectById("sm-001")).thenReturn(po);

        // when
        Optional<StateMachine> result = repository.findById("sm-001");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("sm-001");
        assertThat(result.get().getOntologyId()).isEqualTo("onto-1");
        assertThat(result.get().getName()).isEqualTo("order-workflow");
        assertThat(result.get().getInitialState()).isEqualTo("CREATED");
        assertThat(result.get().getDeleted()).isFalse();
    }

    // ----------------------------------------------------------------
    // 2. findById – miss
    // ----------------------------------------------------------------
    @Test
    @DisplayName("findById should return empty when PO does not exist")
    void findById_miss() {
        // given
        Mockito.when(mapper.selectById("non-existent")).thenReturn(null);

        // when
        Optional<StateMachine> result = repository.findById("non-existent");

        // then
        assertThat(result).isEmpty();
    }

    // ----------------------------------------------------------------
    // 3. findByOntologyId
    // ----------------------------------------------------------------
    @Test
    @DisplayName("findByOntologyId should return list of entities")
    void findByOntologyId() {
        // given
        StateMachinePO po1 = StateMachinePO.builder()
                .id("sm-001").ontologyId("onto-1").name("wf1")
                .initialState("A").states("[\"A\",\"B\"]")
                .deleted(false).build();
        StateMachinePO po2 = StateMachinePO.builder()
                .id("sm-002").ontologyId("onto-1").name("wf2")
                .initialState("X").states("[\"X\",\"Y\",\"Z\"]")
                .deleted(false).build();

        Mockito.when(mapper.selectByOntologyId("onto-1")).thenReturn(List.of(po1, po2));

        // when
        List<StateMachine> result = repository.findByOntologyId("onto-1");

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("wf1");
        assertThat(result.get(1).getName()).isEqualTo("wf2");
    }

    // ----------------------------------------------------------------
    // 4. findByOntologyIdAndEntityId
    // ----------------------------------------------------------------
    @Test
    @DisplayName("findByOntologyIdAndEntityId should return filtered list")
    void findByOntologyIdAndEntityId() {
        // given
        StateMachinePO po = StateMachinePO.builder()
                .id("sm-003").ontologyId("onto-1").entityId("entity-2")
                .name("approval-workflow")
                .initialState("PENDING").states("[\"PENDING\",\"APPROVED\",\"REJECTED\"]")
                .deleted(false).build();

        Mockito.when(mapper.selectByOntologyIdAndEntityId("onto-1", "entity-2"))
                .thenReturn(List.of(po));

        // when
        List<StateMachine> result = repository.findByOntologyIdAndEntityId("onto-1", "entity-2");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEntityId()).isEqualTo("entity-2");
        assertThat(result.get(0).getInitialState()).isEqualTo("PENDING");
    }

    // ----------------------------------------------------------------
    // 5. save – insert (new entity)
    // ----------------------------------------------------------------
    @Test
    @DisplayName("save should insert when entity does not exist in DB")
    void save_insert() {
        // given
        StateMachine entity = StateMachine.builder()
                .id("sm-new")
                .ontologyId("onto-1")
                .entityId("entity-1")
                .name("new-workflow")
                .initialState("START")
                .states("[\"START\",\"END\"]")
                .deleted(false)
                .build();

        Mockito.when(mapper.selectById("sm-new")).thenReturn(null);

        // when
        StateMachine saved = repository.save(entity);

        // then
        assertThat(saved).isSameAs(entity);
        Mockito.verify(mapper).insert(any(StateMachinePO.class));
        Mockito.verify(mapper, Mockito.never()).updateById(any());
    }

    // ----------------------------------------------------------------
    // 6. save – update (existing entity)
    // ----------------------------------------------------------------
    @Test
    @DisplayName("save should update when entity already exists in DB")
    void save_update() {
        // given
        StateMachine entity = StateMachine.builder()
                .id("sm-existing")
                .ontologyId("onto-1")
                .entityId("entity-1")
                .name("updated-workflow")
                .initialState("PENDING")
                .states("[\"PENDING\",\"DONE\"]")
                .deleted(false)
                .build();

        StateMachinePO existingPo = StateMachinePO.builder()
                .id("sm-existing")
                .build();

        Mockito.when(mapper.selectById("sm-existing")).thenReturn(existingPo);

        // when
        StateMachine saved = repository.save(entity);

        // then
        assertThat(saved).isSameAs(entity);
        Mockito.verify(mapper).updateById(any(StateMachinePO.class));
        Mockito.verify(mapper, Mockito.never()).insert(any());
    }

    // ----------------------------------------------------------------
    // 7. save – sets createdAt when null
    // ----------------------------------------------------------------
    @Test
    @DisplayName("save should set createdAt on PO when entity has null createdAt")
    void save_setsCreatedAt() {
        // given
        StateMachine entity = StateMachine.builder()
                .id("sm-null-ct")
                .ontologyId("onto-1")
                .entityId("entity-1")
                .name("no-ct-workflow")
                .initialState("A").states("[\"A\"]")
                .createdAt(null)
                .deleted(false)
                .build();

        Mockito.when(mapper.selectById("sm-null-ct")).thenReturn(null);

        // when
        repository.save(entity);

        // then
        Mockito.verify(mapper).insert(argThat(po ->
                po.getCreatedAt() != null && po.getUpdatedAt() != null
        ));
    }

    // ----------------------------------------------------------------
    // 8. deleteById – soft delete (entity exists)
    // ----------------------------------------------------------------
    @Test
    @DisplayName("deleteById should soft-delete the PO when it exists")
    void deleteById_hit() {
        // given
        StateMachinePO po = StateMachinePO.builder()
                .id("sm-to-delete")
                .deleted(false)
                .build();

        Mockito.when(mapper.selectById("sm-to-delete")).thenReturn(po);

        // when
        repository.deleteById("sm-to-delete");

        // then
        assertThat(po.getDeleted()).isTrue();
        assertThat(po.getUpdatedAt()).isNotNull();
        Mockito.verify(mapper).updateById(po);
    }

    // ----------------------------------------------------------------
    // 9. deleteById – no-op when entity does not exist
    // ----------------------------------------------------------------
    @Test
    @DisplayName("deleteById should do nothing when PO does not exist")
    void deleteById_miss() {
        // given
        Mockito.when(mapper.selectById("non-existent")).thenReturn(null);

        // when
        repository.deleteById("non-existent");

        // then
        Mockito.verify(mapper, Mockito.never()).updateById(any());
    }

    // ----------------------------------------------------------------
    // 10. findByOntologyId – empty list
    // ----------------------------------------------------------------
    @Test
    @DisplayName("findByOntologyId should return empty list when no results")
    void findByOntologyId_empty() {
        // given
        Mockito.when(mapper.selectByOntologyId("onto-empty")).thenReturn(List.of());

        // when
        List<StateMachine> result = repository.findByOntologyId("onto-empty");

        // then
        assertThat(result).isEmpty();
    }
}

package com.ontology.platform.infrastructure.repository.behavior;

import com.ontology.platform.domain.entity.StateTransition;
import com.ontology.platform.infrastructure.converter.StateTransitionConverter;
import com.ontology.platform.infrastructure.persistence.StateTransitionPO;
import com.ontology.platform.infrastructure.persistence.StateTransitionPOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

@DisplayName("StateTransitionRepositoryImpl Unit Tests")
class StateTransitionRepositoryImplTest {

    private StateTransitionRepositoryImpl repository;
    private StateTransitionPOMapper mapper;
    private StateTransitionConverter converter;

    @BeforeEach
    void setUp() {
        mapper = Mockito.mock(StateTransitionPOMapper.class);
        converter = new StateTransitionConverter();
        repository = new StateTransitionRepositoryImpl(mapper, converter);
    }

    // ----------------------------------------------------------------
    // 1. findById – hit
    // ----------------------------------------------------------------
    @Test
    @DisplayName("findById should return entity when PO exists in DB")
    void findById_hit() {
        // given
        StateTransitionPO po = StateTransitionPO.builder()
                .id("st-001")
                .stateMachineId("sm-001")
                .fromState("CREATED")
                .toState("PAID")
                .triggerName("pay_order")
                .guardCondition("order.total > 0")
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();

        Mockito.when(mapper.selectById("st-001")).thenReturn(po);

        // when
        Optional<StateTransition> result = repository.findById("st-001");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("st-001");
        assertThat(result.get().getStateMachineId()).isEqualTo("sm-001");
        assertThat(result.get().getFromState()).isEqualTo("CREATED");
        assertThat(result.get().getToState()).isEqualTo("PAID");
        assertThat(result.get().getTrigger()).isEqualTo("pay_order");
        assertThat(result.get().getGuardCondition()).isEqualTo("order.total > 0");
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
        Optional<StateTransition> result = repository.findById("non-existent");

        // then
        assertThat(result).isEmpty();
    }

    // ----------------------------------------------------------------
    // 3. findByStateMachineId
    // ----------------------------------------------------------------
    @Test
    @DisplayName("findByStateMachineId should return list of transitions")
    void findByStateMachineId() {
        // given
        StateTransitionPO po1 = StateTransitionPO.builder()
                .id("st-001").stateMachineId("sm-001")
                .fromState("A").toState("B").triggerName("go")
                .build();
        StateTransitionPO po2 = StateTransitionPO.builder()
                .id("st-002").stateMachineId("sm-001")
                .fromState("B").toState("C").triggerName("next")
                .guardCondition("x > 1")
                .build();

        Mockito.when(mapper.selectByStateMachineId("sm-001")).thenReturn(List.of(po1, po2));

        // when
        List<StateTransition> result = repository.findByStateMachineId("sm-001");

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTrigger()).isEqualTo("go");
        assertThat(result.get(1).getTrigger()).isEqualTo("next");
        assertThat(result.get(1).getGuardCondition()).isEqualTo("x > 1");
    }

    // ----------------------------------------------------------------
    // 4. findByStateMachineId – empty
    // ----------------------------------------------------------------
    @Test
    @DisplayName("findByStateMachineId should return empty list when no results")
    void findByStateMachineId_empty() {
        // given
        Mockito.when(mapper.selectByStateMachineId("sm-unknown")).thenReturn(List.of());

        // when
        List<StateTransition> result = repository.findByStateMachineId("sm-unknown");

        // then
        assertThat(result).isEmpty();
    }

    // ----------------------------------------------------------------
    // 5. save – insert (new entity)
    // ----------------------------------------------------------------
    @Test
    @DisplayName("save should insert when entity does not exist in DB")
    void save_insert() {
        // given
        StateTransition entity = StateTransition.builder()
                .id("st-new")
                .stateMachineId("sm-001")
                .fromState("PAID")
                .toState("SHIPPED")
                .trigger("ship")
                .guardCondition("payment.confirmed")
                .build();

        Mockito.when(mapper.selectById("st-new")).thenReturn(null);

        // when
        StateTransition saved = repository.save(entity);

        // then
        assertThat(saved).isSameAs(entity);
        Mockito.verify(mapper).insert(any(StateTransitionPO.class));
        Mockito.verify(mapper, Mockito.never()).updateById(any());
    }

    // ----------------------------------------------------------------
    // 6. save – update (existing entity)
    // ----------------------------------------------------------------
    @Test
    @DisplayName("save should update when entity already exists in DB")
    void save_update() {
        // given
        StateTransition entity = StateTransition.builder()
                .id("st-existing")
                .stateMachineId("sm-001")
                .fromState("SHIPPED")
                .toState("DELIVERED")
                .trigger("deliver")
                .guardCondition("shipping.tracking")
                .build();

        StateTransitionPO existingPo = StateTransitionPO.builder()
                .id("st-existing")
                .build();

        Mockito.when(mapper.selectById("st-existing")).thenReturn(existingPo);

        // when
        StateTransition saved = repository.save(entity);

        // then
        assertThat(saved).isSameAs(entity);
        Mockito.verify(mapper).updateById(any(StateTransitionPO.class));
        Mockito.verify(mapper, Mockito.never()).insert(any());
    }

    // ----------------------------------------------------------------
    // 7. deleteById – hard delete
    // ----------------------------------------------------------------
    @Test
    @DisplayName("deleteById should call mapper.deleteById (hard delete)")
    void deleteById() {
        // when
        repository.deleteById("st-to-delete");

        // then
        Mockito.verify(mapper).deleteById("st-to-delete");
    }

    // ----------------------------------------------------------------
    // 8. save – preserves all fields on round-trip conversion
    // ----------------------------------------------------------------
    @Test
    @DisplayName("save should correctly convert all entity fields to PO")
    void save_preservesAllFields() {
        // given
        Instant now = Instant.now();
        StateTransition entity = StateTransition.builder()
                .id("st-roundtrip")
                .stateMachineId("sm-001")
                .fromState("A")
                .toState("B")
                .trigger("trigger_x")
                .guardCondition("condition_y")
                .createdAt(now)
                .build();

        Mockito.when(mapper.selectById("st-roundtrip")).thenReturn(null);

        // when
        repository.save(entity);

        // then
        Mockito.verify(mapper).insert(argThat(po ->
                "st-roundtrip".equals(po.getId()) &&
                        "sm-001".equals(po.getStateMachineId()) &&
                        "A".equals(po.getFromState()) &&
                        "B".equals(po.getToState()) &&
                        "trigger_x".equals(po.getTriggerName()) &&
                        "condition_y".equals(po.getGuardCondition()) &&
                        now.equals(po.getCreatedAt())
        ));
    }

    // ----------------------------------------------------------------
    // 9. findById – null PO returns empty
    // ----------------------------------------------------------------
    @Test
    @DisplayName("findById should return empty when mapper returns null")
    void findById_nullPo() {
        // given
        Mockito.when(mapper.selectById(anyString())).thenReturn(null);

        // when
        Optional<StateTransition> result = repository.findById("anything");

        // then
        assertThat(result).isEmpty();
    }
}

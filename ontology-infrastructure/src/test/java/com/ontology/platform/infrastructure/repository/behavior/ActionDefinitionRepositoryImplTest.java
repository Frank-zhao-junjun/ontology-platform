package com.ontology.platform.infrastructure.repository.behavior;

import com.ontology.platform.domain.entity.ActionDefinition;
import com.ontology.platform.infrastructure.converter.ActionDefinitionConverter;
import com.ontology.platform.infrastructure.persistence.ActionDefinitionPO;
import com.ontology.platform.infrastructure.persistence.ActionDefinitionPOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

@DisplayName("ActionDefinitionRepositoryImpl Unit Tests")
class ActionDefinitionRepositoryImplTest {

    private ActionDefinitionRepositoryImpl repository;
    private ActionDefinitionPOMapper mapper;
    private ActionDefinitionConverter converter;

    @BeforeEach
    void setUp() {
        mapper = Mockito.mock(ActionDefinitionPOMapper.class);
        converter = new ActionDefinitionConverter();
        repository = new ActionDefinitionRepositoryImpl(mapper, converter);
    }

    // ----------------------------------------------------------------
    // 1. findById – hit
    // ----------------------------------------------------------------
    @Test
    @DisplayName("findById should return entity when PO exists in DB")
    void findById_hit() {
        // given
        ActionDefinitionPO po = ActionDefinitionPO.builder()
                .id("ad-001")
                .ontologyId("onto-1")
                .entityId("entity-1")
                .name("approve")
                .displayName("Approve")
                .description("Approve the request")
                .actionType("MANUAL")
                .domain("workflow")
                .riskLevel("APPROVAL")
                .isAsync(false)
                .timeoutMs(60000)
                .inputSchema("{}")
                .outputSchema("{}")
                .preRules("[]")
                .postRules("[]")
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2025-01-01T00:00:00Z"))
                .deleted(false)
                .build();

        Mockito.when(mapper.selectById("ad-001")).thenReturn(po);

        // when
        Optional<ActionDefinition> result = repository.findById("ad-001");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("ad-001");
        assertThat(result.get().getOntologyId()).isEqualTo("onto-1");
        assertThat(result.get().getName()).isEqualTo("approve");
        assertThat(result.get().getRiskLevel()).isEqualTo("APPROVAL");
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
        Optional<ActionDefinition> result = repository.findById("non-existent");

        // then
        assertThat(result).isEmpty();
    }

    // ----------------------------------------------------------------
    // 3. findAll – delegating to findByOntologyId (custom)
    // ----------------------------------------------------------------
    @Test
    @DisplayName("findByOntologyId should return list of entities")
    void findByOntologyId() {
        // given
        ActionDefinitionPO po1 = ActionDefinitionPO.builder()
                .id("ad-001").ontologyId("onto-1").name("action1")
                .isAsync(false).timeoutMs(30000)
                .riskLevel("READ").deleted(false)
                .build();
        ActionDefinitionPO po2 = ActionDefinitionPO.builder()
                .id("ad-002").ontologyId("onto-1").name("action2")
                .isAsync(true).timeoutMs(60000)
                .riskLevel("WRITE").deleted(false)
                .build();

        Mockito.when(mapper.selectByOntologyId("onto-1")).thenReturn(List.of(po1, po2));

        // when
        List<ActionDefinition> result = repository.findByOntologyId("onto-1");

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("action1");
        assertThat(result.get(1).getName()).isEqualTo("action2");
    }

    // ----------------------------------------------------------------
    // 4. findByOntologyIdAndEntityId (custom)
    // ----------------------------------------------------------------
    @Test
    @DisplayName("findByOntologyIdAndEntityId should return filtered list")
    void findByOntologyIdAndEntityId() {
        // given
        ActionDefinitionPO po = ActionDefinitionPO.builder()
                .id("ad-003").ontologyId("onto-1").entityId("entity-2")
                .name("submit").actionType("AUTO")
                .isAsync(false).timeoutMs(10000)
                .riskLevel("WRITE").deleted(false)
                .build();

        Mockito.when(mapper.selectByOntologyIdAndEntityId("onto-1", "entity-2"))
                .thenReturn(List.of(po));

        // when
        List<ActionDefinition> result = repository.findByOntologyIdAndEntityId("onto-1", "entity-2");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEntityId()).isEqualTo("entity-2");
        assertThat(result.get(0).getActionType()).isEqualTo("AUTO");
    }

    // ----------------------------------------------------------------
    // 5. findByOntologyIdAndDomain (custom)
    // ----------------------------------------------------------------
    @Test
    @DisplayName("findByOntologyIdAndDomain should return filtered list")
    void findByOntologyIdAndDomain() {
        // given
        ActionDefinitionPO po = ActionDefinitionPO.builder()
                .id("ad-004").ontologyId("onto-1").domain("biz")
                .name("validate").actionType("AUTO")
                .isAsync(false).timeoutMs(5000)
                .riskLevel("READ").deleted(false)
                .build();

        Mockito.when(mapper.selectByOntologyIdAndDomain("onto-1", "biz"))
                .thenReturn(List.of(po));

        // when
        List<ActionDefinition> result = repository.findByOntologyIdAndDomain("onto-1", "biz");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDomain()).isEqualTo("biz");
    }

    // ----------------------------------------------------------------
    // 6. save – insert (new entity)
    // ----------------------------------------------------------------
    @Test
    @DisplayName("save should insert when entity does not exist in DB")
    void save_insert() {
        // given
        ActionDefinition entity = ActionDefinition.builder()
                .id("ad-new")
                .ontologyId("onto-1")
                .entityId("entity-1")
                .name("new-action")
                .actionType("AUTO")
                .domain("core")
                .riskLevel("READ")
                .isAsync(false)
                .timeoutMs(30000)
                .inputSchema("{}")
                .outputSchema("{}")
                .preRules("[]")
                .postRules("[]")
                .deleted(false)
                .build();

        Mockito.when(mapper.selectById("ad-new")).thenReturn(null);

        // when
        ActionDefinition saved = repository.save(entity);

        // then
        assertThat(saved).isSameAs(entity);
        Mockito.verify(mapper).insert(any(ActionDefinitionPO.class));
        Mockito.verify(mapper, Mockito.never()).updateById(any());
    }

    // ----------------------------------------------------------------
    // 7. save – update (existing entity)
    // ----------------------------------------------------------------
    @Test
    @DisplayName("save should update when entity already exists in DB")
    void save_update() {
        // given
        ActionDefinition entity = ActionDefinition.builder()
                .id("ad-existing")
                .ontologyId("onto-1")
                .entityId("entity-1")
                .name("updated-action")
                .actionType("MANUAL")
                .domain("workflow")
                .riskLevel("WRITE")
                .isAsync(false)
                .timeoutMs(45000)
                .inputSchema("{}")
                .outputSchema("{}")
                .preRules("[]")
                .postRules("[]")
                .deleted(false)
                .build();

        ActionDefinitionPO existingPo = ActionDefinitionPO.builder()
                .id("ad-existing")
                .build();

        Mockito.when(mapper.selectById("ad-existing")).thenReturn(existingPo);

        // when
        ActionDefinition saved = repository.save(entity);

        // then
        assertThat(saved).isSameAs(entity);
        Mockito.verify(mapper).updateById(any(ActionDefinitionPO.class));
        Mockito.verify(mapper, Mockito.never()).insert(any());
    }

    // ----------------------------------------------------------------
    // 8. deleteById – soft delete (entity exists)
    // ----------------------------------------------------------------
    @Test
    @DisplayName("deleteById should soft-delete the PO when it exists")
    void deleteById_hit() {
        // given
        ActionDefinitionPO po = ActionDefinitionPO.builder()
                .id("ad-to-delete")
                .deleted(false)
                .build();

        Mockito.when(mapper.selectById("ad-to-delete")).thenReturn(po);

        // when
        repository.deleteById("ad-to-delete");

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
    // 10. findById – null PO returned from mapper
    // ----------------------------------------------------------------
    @Test
    @DisplayName("findById should tolerate null returned by mapper")
    void findById_nullPo() {
        // given
        Mockito.when(mapper.selectById(anyString())).thenReturn(null);

        // when
        Optional<ActionDefinition> result = repository.findById("anything");

        // then
        assertThat(result).isEmpty();
    }
}

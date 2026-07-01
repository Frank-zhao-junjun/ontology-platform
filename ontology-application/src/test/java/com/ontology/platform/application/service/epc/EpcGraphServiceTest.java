package com.ontology.platform.application.service.epc;

import com.ontology.platform.infrastructure.persistence.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EpcGraphService Test")
class EpcGraphServiceTest {

    @Mock private EpcChainPOMapper epcChainMapper;
    @Mock private EpcNodePOMapper epcNodeMapper;
    @Mock private EpcEdgePOMapper epcEdgeMapper;
    @Mock private EpcModelRefPOMapper epcModelRefMapper;
    @Mock private EpcProfilePOMapper epcProfileMapper;
    @Mock private ActionDefinitionPOMapper actionDefinitionMapper;
    @Mock private DomainEventPOMapper domainEventMapper;

    private EpcGraphService createService() {
        return new EpcGraphService(
                epcChainMapper, epcNodeMapper, epcEdgeMapper, epcModelRefMapper, epcProfileMapper,
                actionDefinitionMapper, domainEventMapper);
    }

    @Test
    @DisplayName("should aggregate EPC coverage for ontology")
    void getCoverage() {
        EpcGraphService service = createService();

        when(epcChainMapper.selectByOntologyId("manufacturing-ontology")).thenReturn(List.of(
                EpcChainPO.builder()
                        .id("epc-po-release")
                        .ontologyId("manufacturing-ontology")
                        .name("生产订单下达链")
                        .aggregateRootId("production-order")
                        .chainType("production")
                        .build()));
        when(epcNodeMapper.selectCount(any())).thenReturn(2L);
        when(epcEdgeMapper.selectCount(any())).thenReturn(1L);
        when(epcModelRefMapper.selectCount(any())).thenReturn(2L);
        when(epcProfileMapper.selectCount(any())).thenReturn(0L);
        when(epcModelRefMapper.selectList(any())).thenReturn(List.of());
        when(actionDefinitionMapper.selectList(any())).thenReturn(List.of());
        when(domainEventMapper.selectList(any())).thenReturn(List.of());

        var coverage = service.getCoverage("manufacturing-ontology");

        assertThat(coverage.getChainCount()).isEqualTo(1);
        assertThat(coverage.getNodeCount()).isEqualTo(2);
        assertThat(coverage.getEdgeCount()).isEqualTo(1);
        assertThat(coverage.getModelRefCount()).isEqualTo(2);
        assertThat(coverage.getAggregateRootIds()).containsExactly("production-order");
        assertThat(coverage.getChains()).hasSize(1);
        assertThat(coverage.getChains().get(0).getNodeCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("should return zero coverage ratio when ontology has no chains")
    void getCoverage_emptyOntology_returnsZeroRatio() {
        EpcGraphService service = createService();

        when(epcChainMapper.selectByOntologyId("empty-ontology")).thenReturn(List.of());
        when(epcChainMapper.selectList(isNull())).thenReturn(List.of());
        when(actionDefinitionMapper.selectList(any())).thenReturn(List.of());
        when(domainEventMapper.selectList(any())).thenReturn(List.of());

        var coverage = service.getCoverage("empty-ontology");

        assertThat(coverage.getChainCount()).isEqualTo(0);
        assertThat(coverage.getNodeCount()).isEqualTo(0);
        assertThat(coverage.getEdgeCount()).isEqualTo(0);
        assertThat(coverage.getModelRefCount()).isEqualTo(0);
        assertThat(coverage.getProfileCount()).isEqualTo(0);
        assertThat(coverage.getAggregateRootsCovered()).isEqualTo(0);
        assertThat(coverage.getAggregateRootIds()).isEmpty();
        assertThat(coverage.getChains()).isEmpty();
        assertThat(coverage.getCoverageRatio()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("should not count null or blank aggregateRootId in numerator but count in denominator")
    void getCoverage_nullAndBlankAggregateRootId_notCountedInNumerator() {
        EpcGraphService service = createService();

        when(epcChainMapper.selectByOntologyId("test")).thenReturn(List.of(
                EpcChainPO.builder().id("chain-1").name("null root").aggregateRootId(null).chainType("A").build(),
                EpcChainPO.builder().id("chain-2").name("blank root").aggregateRootId("").chainType("B").build(),
                EpcChainPO.builder().id("chain-3").name("whitespace root").aggregateRootId("   ").chainType("C").build()));
        when(epcNodeMapper.selectCount(any())).thenReturn(0L);
        when(epcEdgeMapper.selectCount(any())).thenReturn(0L);
        when(epcModelRefMapper.selectCount(any())).thenReturn(0L);
        when(epcProfileMapper.selectCount(any())).thenReturn(0L);
        when(epcModelRefMapper.selectList(any())).thenReturn(List.of());
        when(actionDefinitionMapper.selectList(any())).thenReturn(List.of());
        when(domainEventMapper.selectList(any())).thenReturn(List.of());

        var coverage = service.getCoverage("test");

        // All 3 chains counted in denominator
        assertThat(coverage.getChainCount()).isEqualTo(3);
        // None counted in numerator because aggregateRootId is null / blank
        assertThat(coverage.getAggregateRootsCovered()).isEqualTo(0);
        assertThat(coverage.getAggregateRootIds()).isEmpty();
        assertThat(coverage.getCoverageRatio()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("should deduplicate aggregate root IDs when multiple chains share the same ID")
    void getCoverage_duplicateAggregateRootId_deduplicated() {
        EpcGraphService service = createService();

        when(epcChainMapper.selectByOntologyId("test")).thenReturn(List.of(
                EpcChainPO.builder().id("chain-1").name("Chain 1").aggregateRootId("root-1").chainType("A").build(),
                EpcChainPO.builder().id("chain-2").name("Chain 2").aggregateRootId("root-1").chainType("A").build(),
                EpcChainPO.builder().id("chain-3").name("Chain 3").aggregateRootId("root-2").chainType("B").build()));
        when(epcNodeMapper.selectCount(any())).thenReturn(0L);
        when(epcEdgeMapper.selectCount(any())).thenReturn(0L);
        when(epcModelRefMapper.selectCount(any())).thenReturn(0L);
        when(epcProfileMapper.selectCount(any())).thenReturn(0L);
        when(epcModelRefMapper.selectList(any())).thenReturn(List.of());
        when(actionDefinitionMapper.selectList(any())).thenReturn(List.of());
        when(domainEventMapper.selectList(any())).thenReturn(List.of());

        var coverage = service.getCoverage("test");

        assertThat(coverage.getChainCount()).isEqualTo(3);
        assertThat(coverage.getAggregateRootsCovered()).isEqualTo(2);
        assertThat(coverage.getAggregateRootIds()).containsExactlyInAnyOrder("root-1", "root-2");
        assertThat(coverage.getCoverageRatio()).isCloseTo(2.0 / 3.0, offset(1e-10));
    }

    @Test
    @DisplayName("should collect uncovered actions and events not referenced by any chain")
    void getCoverage_uncoveredActionsAndEvents_collected() {
        EpcGraphService service = createService();

        when(epcChainMapper.selectByOntologyId("test")).thenReturn(List.of(
                EpcChainPO.builder().id("chain-1").name("Chain 1").aggregateRootId("root-1").chainType("A").build()));
        when(epcNodeMapper.selectCount(any())).thenReturn(0L);
        when(epcEdgeMapper.selectCount(any())).thenReturn(0L);
        when(epcModelRefMapper.selectCount(any())).thenReturn(2L);
        when(epcProfileMapper.selectCount(any())).thenReturn(0L);

        // Chain-1 has model refs: action-1, action-2, event-1
        when(epcModelRefMapper.selectList(any())).thenReturn(List.of(
                EpcModelRefPO.builder().id("ref-1").chainId("chain-1").modelType("action").modelId("action-1").build(),
                EpcModelRefPO.builder().id("ref-2").chainId("chain-1").modelType("action").modelId("action-2").build(),
                EpcModelRefPO.builder().id("ref-3").chainId("chain-1").modelType("event").modelId("event-1").build()));

        // Action definitions: action-1 and action-2 are covered; action-3 and action-4 are not
        when(actionDefinitionMapper.selectList(any())).thenReturn(List.of(
                ActionDefinitionPO.builder().id("action-1").build(),
                ActionDefinitionPO.builder().id("action-2").build(),
                ActionDefinitionPO.builder().id("action-3").build(),
                ActionDefinitionPO.builder().id("action-4").build()));

        // Domain events: event-1 is covered; event-2 is not
        when(domainEventMapper.selectList(any())).thenReturn(List.of(
                DomainEventPO.builder().id("event-1").build(),
                DomainEventPO.builder().id("event-2").build()));

        var coverage = service.getCoverage("test");

        assertThat(coverage.getChainCount()).isEqualTo(1);
        assertThat(coverage.getAggregateRootsCovered()).isEqualTo(1);
        assertThat(coverage.getCoverageRatio()).isEqualTo(1.0);
        assertThat(coverage.getUncoveredActions()).containsExactlyInAnyOrder("action-3", "action-4");
        assertThat(coverage.getUncoveredEvents()).containsExactly("event-2");
    }

    @Test
    @DisplayName("should fall back to selectList(null) when ontologyId is null")
    void getCoverage_nullOntologyId_fallsBackToSelectList() {
        EpcGraphService service = createService();

        // When ontologyId is null, loadChains skips selectByOntologyId and goes directly to selectList(null)
        when(epcChainMapper.selectList(isNull())).thenReturn(List.of(
                EpcChainPO.builder().id("chain-1").name("Chain 1").aggregateRootId("root-1").chainType("A").build()));
        when(epcNodeMapper.selectCount(any())).thenReturn(0L);
        when(epcEdgeMapper.selectCount(any())).thenReturn(0L);
        when(epcModelRefMapper.selectCount(any())).thenReturn(0L);
        when(epcProfileMapper.selectCount(any())).thenReturn(0L);
        when(epcModelRefMapper.selectList(any())).thenReturn(List.of());
        when(actionDefinitionMapper.selectList(isNull())).thenReturn(List.of());
        when(domainEventMapper.selectList(isNull())).thenReturn(List.of());

        var coverage = service.getCoverage(null);

        assertThat(coverage.getChainCount()).isEqualTo(1);
        assertThat(coverage.getAggregateRootIds()).containsExactly("root-1");

        // selectByOntologyId should NOT be called when ontologyId is null
        verify(epcChainMapper, never()).selectByOntologyId(any());
        verify(epcChainMapper).selectList(isNull());
        verify(actionDefinitionMapper).selectList(isNull());
        verify(domainEventMapper).selectList(isNull());
    }

    @Test
    @DisplayName("should handle null returned from model ref selectList defensively")
    void getCoverage_nullModelRefList_handledDefensively() {
        EpcGraphService service = createService();

        when(epcChainMapper.selectByOntologyId("test")).thenReturn(List.of(
                EpcChainPO.builder().id("chain-1").name("Chain 1").aggregateRootId("root-1").chainType("A").build()));
        when(epcNodeMapper.selectCount(any())).thenReturn(0L);
        when(epcEdgeMapper.selectCount(any())).thenReturn(0L);
        when(epcModelRefMapper.selectCount(any())).thenReturn(0L);
        when(epcProfileMapper.selectCount(any())).thenReturn(0L);
        // Intentionally return null from selectList to test defensive null check
        when(epcModelRefMapper.selectList(any())).thenReturn(null);
        when(actionDefinitionMapper.selectList(any())).thenReturn(List.of());
        when(domainEventMapper.selectList(any())).thenReturn(List.of());

        var coverage = service.getCoverage("test");

        // Should not throw NPE; collectCoveredRefs skips null refs
        assertThat(coverage.getChainCount()).isEqualTo(1);
        assertThat(coverage.getUncoveredActions()).isEmpty();
        assertThat(coverage.getUncoveredEvents()).isEmpty();
    }

    @Test
    @DisplayName("should avoid division by zero and return 0.0 when there are no chains")
    void getCoverage_noChains_avoidsDivisionByZero() {
        EpcGraphService service = createService();

        // selectByOntologyId returns null → falls through; selectList(null) also returns null → empty list
        when(epcChainMapper.selectByOntologyId("test")).thenReturn(null);
        when(epcChainMapper.selectList(isNull())).thenReturn(null);
        when(actionDefinitionMapper.selectList(any())).thenReturn(List.of());
        when(domainEventMapper.selectList(any())).thenReturn(List.of());

        var coverage = service.getCoverage("test");

        assertThat(coverage.getChainCount()).isEqualTo(0);
        assertThat(coverage.getAggregateRootsCovered()).isEqualTo(0);
        assertThat(coverage.getCoverageRatio()).isEqualTo(0.0);
        assertThat(coverage.getChains()).isEmpty();
    }

    @Test
    @DisplayName("should return 1.0 coverage ratio when all chains have unique aggregate root IDs")
    void getCoverage_allChainsCovered_ratioIsOne() {
        EpcGraphService service = createService();

        when(epcChainMapper.selectByOntologyId("test")).thenReturn(List.of(
                EpcChainPO.builder().id("chain-1").name("Chain 1").aggregateRootId("root-1").chainType("A").build(),
                EpcChainPO.builder().id("chain-2").name("Chain 2").aggregateRootId("root-2").chainType("B").build(),
                EpcChainPO.builder().id("chain-3").name("Chain 3").aggregateRootId("root-3").chainType("C").build()));
        when(epcNodeMapper.selectCount(any())).thenReturn(0L);
        when(epcEdgeMapper.selectCount(any())).thenReturn(0L);
        when(epcModelRefMapper.selectCount(any())).thenReturn(0L);
        when(epcProfileMapper.selectCount(any())).thenReturn(0L);
        when(epcModelRefMapper.selectList(any())).thenReturn(List.of());
        when(actionDefinitionMapper.selectList(any())).thenReturn(List.of());
        when(domainEventMapper.selectList(any())).thenReturn(List.of());

        var coverage = service.getCoverage("test");

        assertThat(coverage.getChainCount()).isEqualTo(3);
        assertThat(coverage.getAggregateRootsCovered()).isEqualTo(3);
        assertThat(coverage.getCoverageRatio()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("should return 0.0 coverage ratio when chains exist but all aggregate root IDs are null")
    void getCoverage_chainsExistZeroAggregateRoots_ratioIsZero() {
        EpcGraphService service = createService();

        when(epcChainMapper.selectByOntologyId("test")).thenReturn(List.of(
                EpcChainPO.builder().id("chain-1").name("Chain 1").aggregateRootId(null).chainType("A").build(),
                EpcChainPO.builder().id("chain-2").name("Chain 2").aggregateRootId(null).chainType("B").build()));
        when(epcNodeMapper.selectCount(any())).thenReturn(0L);
        when(epcEdgeMapper.selectCount(any())).thenReturn(0L);
        when(epcModelRefMapper.selectCount(any())).thenReturn(0L);
        when(epcProfileMapper.selectCount(any())).thenReturn(0L);
        when(epcModelRefMapper.selectList(any())).thenReturn(List.of());
        when(actionDefinitionMapper.selectList(any())).thenReturn(List.of());
        when(domainEventMapper.selectList(any())).thenReturn(List.of());

        var coverage = service.getCoverage("test");

        assertThat(coverage.getChainCount()).isEqualTo(2);
        assertThat(coverage.getAggregateRootsCovered()).isEqualTo(0);
        assertThat(coverage.getCoverageRatio()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("should call mapper methods with correct invocation counts")
    void getCoverage_verifyMapperInteractions() {
        EpcGraphService service = createService();

        when(epcChainMapper.selectByOntologyId("test")).thenReturn(List.of(
                EpcChainPO.builder().id("chain-1").name("Chain 1").aggregateRootId("root-1").chainType("A").build(),
                EpcChainPO.builder().id("chain-2").name("Chain 2").aggregateRootId("root-2").chainType("B").build()));
        when(epcNodeMapper.selectCount(any())).thenReturn(5L);
        when(epcEdgeMapper.selectCount(any())).thenReturn(3L);
        when(epcModelRefMapper.selectCount(any())).thenReturn(2L);
        when(epcProfileMapper.selectCount(any())).thenReturn(1L);
        when(epcModelRefMapper.selectList(any())).thenReturn(List.of());
        when(actionDefinitionMapper.selectList(any())).thenReturn(List.of());
        when(domainEventMapper.selectList(any())).thenReturn(List.of());

        service.getCoverage("test");

        // selectByOntologyId called once
        verify(epcChainMapper).selectByOntologyId("test");
        // selectCount called once per chain (2 chains)
        verify(epcNodeMapper, times(2)).selectCount(any());
        verify(epcEdgeMapper, times(2)).selectCount(any());
        verify(epcModelRefMapper, times(2)).selectCount(any());
        verify(epcProfileMapper, times(2)).selectCount(any());
        // selectList for model refs called once per chain (2 chains)
        verify(epcModelRefMapper, times(2)).selectList(any());
        // selectList for action definitions called once
        verify(actionDefinitionMapper, times(1)).selectList(any());
        // selectList for domain events called once
        verify(domainEventMapper, times(1)).selectList(any());

        // No unexpected interactions on count-only mappers
        verifyNoMoreInteractions(epcNodeMapper);
        verifyNoMoreInteractions(epcEdgeMapper);
        verifyNoMoreInteractions(epcProfileMapper);
    }
}

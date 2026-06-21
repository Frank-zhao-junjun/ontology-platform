package com.ontology.platform.application.service.epc;

import com.ontology.platform.infrastructure.persistence.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EpcGraphService Test")
class EpcGraphServiceTest {

    @Mock private EpcChainPOMapper epcChainMapper;
    @Mock private EpcNodePOMapper epcNodeMapper;
    @Mock private EpcEdgePOMapper epcEdgeMapper;
    @Mock private EpcModelRefPOMapper epcModelRefMapper;
    @Mock private EpcProfilePOMapper epcProfileMapper;

    @Test
    @DisplayName("should aggregate EPC coverage for ontology")
    void getCoverage() {
        EpcGraphService service = new EpcGraphService(
                epcChainMapper, epcNodeMapper, epcEdgeMapper, epcModelRefMapper, epcProfileMapper);

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

        var coverage = service.getCoverage("manufacturing-ontology");

        assertThat(coverage.getChainCount()).isEqualTo(1);
        assertThat(coverage.getNodeCount()).isEqualTo(2);
        assertThat(coverage.getEdgeCount()).isEqualTo(1);
        assertThat(coverage.getModelRefCount()).isEqualTo(2);
        assertThat(coverage.getAggregateRootIds()).containsExactly("production-order");
        assertThat(coverage.getChains()).hasSize(1);
        assertThat(coverage.getChains().get(0).getNodeCount()).isEqualTo(2);
    }
}

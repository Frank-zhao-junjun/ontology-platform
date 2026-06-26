package com.ontology.platform.domain.dto.epc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpcCoverageResponse {
    private String ontologyId;
    private int chainCount;
    private int nodeCount;
    private int edgeCount;
    private int modelRefCount;
    private int profileCount;
    private int aggregateRootsCovered;
    @Builder.Default
    private List<String> aggregateRootIds = new ArrayList<>();
    private double coverageRatio;
    /** Actions not covered by any EPC chain */
    @Builder.Default
    private List<String> uncoveredActions = new ArrayList<>();
    /** Events not covered by any EPC chain */
    @Builder.Default
    private List<String> uncoveredEvents = new ArrayList<>();
    @Builder.Default
    private List<ChainSummary> chains = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChainSummary {
        private String id;
        private String name;
        private String aggregateRootId;
        private String chainType;
        private int nodeCount;
        private int edgeCount;
        private int modelRefCount;
    }
}

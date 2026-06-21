package com.ontology.platform.application.service.epc;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ontology.platform.domain.dto.epc.EpcCoverageResponse;
import com.ontology.platform.infrastructure.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class EpcGraphService {

    private final EpcChainPOMapper epcChainMapper;
    private final EpcNodePOMapper epcNodeMapper;
    private final EpcEdgePOMapper epcEdgeMapper;
    private final EpcModelRefPOMapper epcModelRefMapper;
    private final EpcProfilePOMapper epcProfileMapper;

    public EpcCoverageResponse getCoverage(String ontologyId) {
        List<EpcChainPO> chains = loadChains(ontologyId);

        int nodeCount = 0;
        int edgeCount = 0;
        int modelRefCount = 0;
        int profileCount = 0;
        Set<String> aggregateRootIds = new HashSet<>();
        List<EpcCoverageResponse.ChainSummary> summaries = new ArrayList<>();

        for (EpcChainPO chain : chains) {
            if (chain.getAggregateRootId() != null && !chain.getAggregateRootId().isBlank()) {
                aggregateRootIds.add(chain.getAggregateRootId());
            }

            int chainNodes = countNodes(chain.getId());
            int chainEdges = countEdges(chain.getId());
            int chainRefs = countModelRefs(chain.getId());
            int chainProfiles = countProfiles(chain.getId());

            nodeCount += chainNodes;
            edgeCount += chainEdges;
            modelRefCount += chainRefs;
            profileCount += chainProfiles;

            summaries.add(EpcCoverageResponse.ChainSummary.builder()
                    .id(chain.getId())
                    .name(chain.getName())
                    .aggregateRootId(chain.getAggregateRootId())
                    .chainType(chain.getChainType())
                    .nodeCount(chainNodes)
                    .edgeCount(chainEdges)
                    .modelRefCount(chainRefs)
                    .build());
        }

        double coverageRatio = summaries.isEmpty() ? 0.0
                : (double) aggregateRootIds.size() / summaries.size();

        return EpcCoverageResponse.builder()
                .ontologyId(ontologyId)
                .chainCount(summaries.size())
                .nodeCount(nodeCount)
                .edgeCount(edgeCount)
                .modelRefCount(modelRefCount)
                .profileCount(profileCount)
                .aggregateRootsCovered(aggregateRootIds.size())
                .aggregateRootIds(List.copyOf(aggregateRootIds))
                .coverageRatio(coverageRatio)
                .chains(summaries)
                .build();
    }

    private List<EpcChainPO> loadChains(String ontologyId) {
        if (ontologyId != null && !ontologyId.isBlank()) {
            List<EpcChainPO> scoped = epcChainMapper.selectByOntologyId(ontologyId);
            if (scoped != null && !scoped.isEmpty()) {
                return scoped;
            }
        }
        List<EpcChainPO> all = epcChainMapper.selectList(null);
        return all != null ? all : List.of();
    }

    private int countNodes(String chainId) {
        Long count = epcNodeMapper.selectCount(new QueryWrapper<EpcNodePO>().eq("chain_id", chainId));
        return count != null ? count.intValue() : 0;
    }

    private int countEdges(String chainId) {
        Long count = epcEdgeMapper.selectCount(new QueryWrapper<EpcEdgePO>().eq("chain_id", chainId));
        return count != null ? count.intValue() : 0;
    }

    private int countModelRefs(String chainId) {
        Long count = epcModelRefMapper.selectCount(new QueryWrapper<EpcModelRefPO>().eq("chain_id", chainId));
        return count != null ? count.intValue() : 0;
    }

    private int countProfiles(String chainId) {
        Long count = epcProfileMapper.selectCount(new QueryWrapper<EpcProfilePO>().eq("chain_id", chainId));
        return count != null ? count.intValue() : 0;
    }
}

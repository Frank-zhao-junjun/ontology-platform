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
    private final ActionDefinitionPOMapper actionDefinitionMapper;
    private final DomainEventPOMapper domainEventMapper;

    public EpcCoverageResponse getCoverage(String ontologyId) {
        List<EpcChainPO> chains = loadChains(ontologyId);

        int nodeCount = 0;
        int edgeCount = 0;
        int modelRefCount = 0;
        int profileCount = 0;
        Set<String> aggregateRootIds = new HashSet<>();
        Set<String> coveredActionIds = new HashSet<>();
        Set<String> coveredEventIds = new HashSet<>();
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

            // Collect covered actions and events from model refs
            collectCoveredRefs(chain.getId(), coveredActionIds, coveredEventIds);

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

        // Cross-reference: find uncovered actions and events
        List<String> uncoveredActions = findUncoveredActions(ontologyId, coveredActionIds);
        List<String> uncoveredEvents = findUncoveredEvents(ontologyId, coveredEventIds);

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
                .uncoveredActions(uncoveredActions)
                .uncoveredEvents(uncoveredEvents)
                .build();
    }

    private void collectCoveredRefs(String chainId, Set<String> actionIds, Set<String> eventIds) {
        List<EpcModelRefPO> refs = epcModelRefMapper.selectList(
                new QueryWrapper<EpcModelRefPO>().eq("chain_id", chainId));
        if (refs == null) return;
        for (EpcModelRefPO ref : refs) {
            if ("action".equalsIgnoreCase(ref.getModelType()) && ref.getModelId() != null) {
                actionIds.add(ref.getModelId());
            }
            if ("event".equalsIgnoreCase(ref.getModelType()) && ref.getModelId() != null) {
                eventIds.add(ref.getModelId());
            }
        }
    }

    private List<String> findUncoveredActions(String ontologyId, Set<String> coveredIds) {
        List<ActionDefinitionPO> all = loadActionDefinitions(ontologyId);
        return all.stream()
                .map(ActionDefinitionPO::getId)
                .filter(id -> !coveredIds.contains(id))
                .toList();
    }

    private List<String> findUncoveredEvents(String ontologyId, Set<String> coveredIds) {
        List<DomainEventPO> all = loadDomainEvents(ontologyId);
        return all.stream()
                .map(DomainEventPO::getId)
                .filter(id -> !coveredIds.contains(id))
                .toList();
    }

    private List<ActionDefinitionPO> loadActionDefinitions(String ontologyId) {
        if (ontologyId != null && !ontologyId.isBlank()) {
            return actionDefinitionMapper.selectList(
                    new QueryWrapper<ActionDefinitionPO>().eq("ontology_id", ontologyId));
        }
        List<ActionDefinitionPO> all = actionDefinitionMapper.selectList(null);
        return all != null ? all : List.of();
    }

    private List<DomainEventPO> loadDomainEvents(String ontologyId) {
        if (ontologyId != null && !ontologyId.isBlank()) {
            return domainEventMapper.selectList(
                    new QueryWrapper<DomainEventPO>().eq("ontology_id", ontologyId));
        }
        List<DomainEventPO> all = domainEventMapper.selectList(null);
        return all != null ? all : List.of();
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

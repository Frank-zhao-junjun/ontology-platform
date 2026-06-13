package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.EpcStep;
import com.ontology.platform.domain.repository.EpcStepRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class InMemoryEpcStepRepository implements EpcStepRepository {

    private final Map<String, EpcStep> store = new ConcurrentHashMap<>();

    @Override
    public Optional<EpcStep> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<EpcStep> findByOntologyId(String ontologyId) {
        return store.values().stream()
                .filter(e -> e.getOntologyId().equals(ontologyId))
                .collect(Collectors.toList());
    }

    @Override
    public List<EpcStep> findByOntologyIdAndFlowName(String ontologyId, String flowName) {
        return store.values().stream()
                .filter(e -> e.getOntologyId().equals(ontologyId)
                        && e.getFlowName().equals(flowName))
                .sorted(Comparator.comparingInt(EpcStep::getStepOrder))
                .collect(Collectors.toList());
    }

    @Override
    public List<EpcStep> findByFlowNameOrderByStepOrder(String flowName) {
        return store.values().stream()
                .filter(e -> e.getFlowName().equals(flowName))
                .sorted(Comparator.comparingInt(EpcStep::getStepOrder))
                .collect(Collectors.toList());
    }

    @Override
    public EpcStep save(EpcStep entity) {
        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}

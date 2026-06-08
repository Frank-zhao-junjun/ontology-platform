package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.Relation;
import com.ontology.platform.domain.repository.RelationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 关系仓储实现
 * Relation Repository Implementation
 * 使用内存存储作为占位实现，后续替换为PostgreSQL + Apache AGE存储
 */
@Slf4j
@Repository
public class RelationRepositoryImpl implements RelationRepository {

    // 内存存储（占位实现）
    private final ConcurrentMap<String, Relation> relationStore = new ConcurrentHashMap<>();

    @Override
    public Optional<Relation> findById(String id) {
        log.debug("Finding relation by id: {}", id);
        return Optional.ofNullable(relationStore.get(id));
    }

    @Override
    public List<Relation> findByOntologyId(String ontologyId) {
        log.debug("Finding relations by ontologyId: {}", ontologyId);
        return relationStore.values().stream()
                .filter(r -> ontologyId.equals(r.getOntologyId()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Relation> findByOntologyIdAndName(String ontologyId, String name) {
        log.debug("Finding relation by ontologyId and name: {}, {}", ontologyId, name);
        return relationStore.values().stream()
                .filter(r -> ontologyId.equals(r.getOntologyId()) && name.equals(r.getName()))
                .findFirst();
    }

    @Override
    public List<Relation> findBySourceTypeId(String sourceTypeId) {
        log.debug("Finding relations by sourceTypeId: {}", sourceTypeId);
        return relationStore.values().stream()
                .filter(r -> sourceTypeId.equals(r.getSourceTypeId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Relation> findByTargetTypeId(String targetTypeId) {
        log.debug("Finding relations by targetTypeId: {}", targetTypeId);
        return relationStore.values().stream()
                .filter(r -> targetTypeId.equals(r.getTargetTypeId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Relation> findBySourceTypeIdOrTargetTypeId(String sourceTypeId, String targetTypeId) {
        log.debug("Finding relations by sourceTypeId or targetTypeId: {}, {}", sourceTypeId, targetTypeId);
        return relationStore.values().stream()
                .filter(r -> sourceTypeId.equals(r.getSourceTypeId()) || targetTypeId.equals(r.getTargetTypeId()))
                .collect(Collectors.toList());
    }

    @Override
    public Relation save(Relation relation) {
        log.debug("Saving relation: {}", relation.getId());
        relationStore.put(relation.getId(), relation);
        return relation;
    }

    @Override
    public Relation update(Relation relation) {
        log.debug("Updating relation: {}", relation.getId());
        if (!relationStore.containsKey(relation.getId())) {
            throw new IllegalStateException("Relation not found: " + relation.getId());
        }
        relationStore.put(relation.getId(), relation);
        return relation;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Deleting relation by id: {}", id);
        relationStore.remove(id);
    }

    @Override
    public boolean existsByOntologyIdAndName(String ontologyId, String name) {
        log.debug("Checking if relation exists by ontologyId and name: {}, {}", ontologyId, name);
        return relationStore.values().stream()
                .anyMatch(r -> ontologyId.equals(r.getOntologyId()) && name.equals(r.getName()));
    }

    @Override
    public boolean existsByOntologyIdAndNameAndIdNot(String ontologyId, String name, String excludeId) {
        log.debug("Checking if relation exists by ontologyId and name excluding id: {}, {}, {}", ontologyId, name, excludeId);
        return relationStore.values().stream()
                .anyMatch(r -> ontologyId.equals(r.getOntologyId()) && name.equals(r.getName()) && !excludeId.equals(r.getId()));
    }

    @Override
    public long countByOntologyId(String ontologyId) {
        log.debug("Counting relations by ontologyId: {}", ontologyId);
        return relationStore.values().stream()
                .filter(r -> ontologyId.equals(r.getOntologyId()))
                .count();
    }
}

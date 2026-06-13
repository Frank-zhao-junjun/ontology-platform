package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.Relation;
import com.ontology.platform.domain.repository.RelationRepository;
import com.ontology.platform.infrastructure.converter.RelationConverter;
import com.ontology.platform.infrastructure.persistence.RelationPO;
import com.ontology.platform.infrastructure.persistence.RelationPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 关系仓储实现
 * Relation Repository Implementation
 * 基于 MyBatis-Plus + PostgreSQL relation_definition 表
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RelationRepositoryImpl implements RelationRepository {

    private final RelationPOMapper relationPOMapper;
    private final RelationConverter relationConverter;

    @Override
    public Optional<Relation> findById(String id) {
        log.debug("Finding relation by id: {}", id);
        RelationPO po = relationPOMapper.selectById(id);
        return Optional.ofNullable(relationConverter.toEntity(po));
    }

    @Override
    public List<Relation> findByOntologyId(String ontologyId) {
        log.debug("Finding relations by ontologyId: {}", ontologyId);
        List<RelationPO> poList = relationPOMapper.selectByOntologyId(ontologyId);
        return relationConverter.toEntityList(poList);
    }

    @Override
    public Optional<Relation> findByOntologyIdAndName(String ontologyId, String name) {
        log.debug("Finding relation by ontologyId and name: {}, {}", ontologyId, name);
        RelationPO po = relationPOMapper.selectByOntologyIdAndName(ontologyId, name);
        return Optional.ofNullable(relationConverter.toEntity(po));
    }

    @Override
    public List<Relation> findBySourceTypeId(String sourceTypeId) {
        log.debug("Finding relations by sourceTypeId: {}", sourceTypeId);
        List<RelationPO> poList = relationPOMapper.selectBySourceTypeId(sourceTypeId);
        return relationConverter.toEntityList(poList);
    }

    @Override
    public List<Relation> findByTargetTypeId(String targetTypeId) {
        log.debug("Finding relations by targetTypeId: {}", targetTypeId);
        List<RelationPO> poList = relationPOMapper.selectByTargetTypeId(targetTypeId);
        return relationConverter.toEntityList(poList);
    }

    @Override
    public List<Relation> findBySourceTypeIdOrTargetTypeId(String sourceTypeId, String targetTypeId) {
        log.debug("Finding relations by sourceTypeId or targetTypeId: {}, {}", sourceTypeId, targetTypeId);
        List<RelationPO> poList = relationPOMapper.selectBySourceTypeIdOrTargetTypeId(sourceTypeId, targetTypeId);
        return relationConverter.toEntityList(poList);
    }

    @Override
    public Relation save(Relation relation) {
        log.debug("Saving relation: {}", relation.getId());
        if (relation.getCreatedAt() == null) {
            relation.setCreatedAt(Instant.now());
        }
        relation.setUpdatedAt(Instant.now());

        RelationPO po = relationConverter.toPO(relation);
        relationPOMapper.insert(po);
        return relation;
    }

    @Override
    public Relation update(Relation relation) {
        log.debug("Updating relation: {}", relation.getId());
        if (relationPOMapper.selectById(relation.getId()) == null) {
            throw new IllegalStateException("Relation not found: " + relation.getId());
        }
        relation.setUpdatedAt(Instant.now());

        RelationPO po = relationConverter.toPO(relation);
        relationPOMapper.updateById(po);
        return relation;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Deleting relation by id: {}", id);
        relationPOMapper.deleteById(id);
    }

    @Override
    public boolean existsByOntologyIdAndName(String ontologyId, String name) {
        log.debug("Checking if relation exists by ontologyId and name: {}, {}", ontologyId, name);
        return relationPOMapper.countByOntologyIdAndName(ontologyId, name) > 0;
    }

    @Override
    public boolean existsByOntologyIdAndNameAndIdNot(String ontologyId, String name, String excludeId) {
        log.debug("Checking if relation exists by ontologyId and name excluding id: {}, {}, {}",
                ontologyId, name, excludeId);
        return relationPOMapper.countByOntologyIdAndNameExcludingId(ontologyId, name, excludeId) > 0;
    }

    @Override
    public long countByOntologyId(String ontologyId) {
        log.debug("Counting relations by ontologyId: {}", ontologyId);
        return relationPOMapper.countByOntologyId(ontologyId);
    }
}

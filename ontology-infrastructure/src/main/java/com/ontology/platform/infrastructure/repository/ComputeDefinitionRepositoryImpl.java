package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.ComputeDefinition;
import com.ontology.platform.domain.repository.ComputeDefinitionRepository;
import com.ontology.platform.infrastructure.converter.ComputeDefinitionConverter;
import com.ontology.platform.infrastructure.persistence.ComputeDefinitionPO;
import com.ontology.platform.infrastructure.persistence.ComputeDefinitionPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ComputeDefinitionRepositoryImpl implements ComputeDefinitionRepository {

    private final ComputeDefinitionPOMapper computeDefinitionPOMapper;
    private final ComputeDefinitionConverter computeDefinitionConverter;

    @Override
    public Optional<ComputeDefinition> findById(String id) {
        log.debug("Finding compute definition by id: {}", id);
        ComputeDefinitionPO po = computeDefinitionPOMapper.selectById(id);
        return Optional.ofNullable(computeDefinitionConverter.toEntity(po));
    }

    @Override
    public List<ComputeDefinition> findByOntologyId(String ontologyId) {
        log.debug("Finding compute definitions by ontologyId: {}", ontologyId);
        List<ComputeDefinitionPO> poList = computeDefinitionPOMapper.selectByOntologyId(ontologyId);
        return computeDefinitionConverter.toEntityList(poList);
    }

    @Override
    public ComputeDefinition save(ComputeDefinition entity) {
        log.debug("Saving compute definition: {}", entity.getId());
        ComputeDefinitionPO po = computeDefinitionConverter.toPO(entity);
        if (po.getCreatedAt() == null) {
            po.setCreatedAt(Instant.now());
        }
        po.setUpdatedAt(Instant.now());
        if (computeDefinitionPOMapper.selectById(entity.getId()) != null) {
            computeDefinitionPOMapper.updateById(po);
        } else {
            computeDefinitionPOMapper.insert(po);
        }
        return entity;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Soft-deleting compute definition: {}", id);
        ComputeDefinitionPO po = computeDefinitionPOMapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            computeDefinitionPOMapper.updateById(po);
        }
    }
}

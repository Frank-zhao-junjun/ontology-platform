package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.QueryDefinition;
import com.ontology.platform.domain.repository.QueryDefinitionRepository;
import com.ontology.platform.infrastructure.converter.QueryDefinitionConverter;
import com.ontology.platform.infrastructure.persistence.QueryDefinitionPO;
import com.ontology.platform.infrastructure.persistence.QueryDefinitionPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class QueryDefinitionRepositoryImpl implements QueryDefinitionRepository {

    private final QueryDefinitionPOMapper queryDefinitionPOMapper;
    private final QueryDefinitionConverter queryDefinitionConverter;

    @Override
    public Optional<QueryDefinition> findById(String id) {
        log.debug("Finding query definition by id: {}", id);
        QueryDefinitionPO po = queryDefinitionPOMapper.selectById(id);
        return Optional.ofNullable(queryDefinitionConverter.toEntity(po));
    }

    @Override
    public List<QueryDefinition> findByOntologyId(String ontologyId) {
        log.debug("Finding query definitions by ontologyId: {}", ontologyId);
        List<QueryDefinitionPO> poList = queryDefinitionPOMapper.selectByOntologyId(ontologyId);
        return queryDefinitionConverter.toEntityList(poList);
    }

    @Override
    public QueryDefinition save(QueryDefinition entity) {
        log.debug("Saving query definition: {}", entity.getId());
        QueryDefinitionPO po = queryDefinitionConverter.toPO(entity);
        if (po.getCreatedAt() == null) {
            po.setCreatedAt(Instant.now());
        }
        po.setUpdatedAt(Instant.now());
        if (queryDefinitionPOMapper.selectById(entity.getId()) != null) {
            queryDefinitionPOMapper.updateById(po);
        } else {
            queryDefinitionPOMapper.insert(po);
        }
        return entity;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Soft-deleting query definition: {}", id);
        QueryDefinitionPO po = queryDefinitionPOMapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            queryDefinitionPOMapper.updateById(po);
        }
    }
}

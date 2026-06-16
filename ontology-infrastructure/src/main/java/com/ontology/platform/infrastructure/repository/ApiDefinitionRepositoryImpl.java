package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.ApiDefinition;
import com.ontology.platform.domain.repository.ApiDefinitionRepository;
import com.ontology.platform.infrastructure.converter.ApiDefinitionConverter;
import com.ontology.platform.infrastructure.persistence.ApiDefinitionPO;
import com.ontology.platform.infrastructure.persistence.ApiDefinitionPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ApiDefinitionRepositoryImpl implements ApiDefinitionRepository {

    private final ApiDefinitionPOMapper apiDefinitionPOMapper;
    private final ApiDefinitionConverter apiDefinitionConverter;

    @Override
    public Optional<ApiDefinition> findById(String id) {
        log.debug("Finding api definition by id: {}", id);
        ApiDefinitionPO po = apiDefinitionPOMapper.selectById(id);
        return Optional.ofNullable(apiDefinitionConverter.toEntity(po));
    }

    @Override
    public List<ApiDefinition> findByOntologyId(String ontologyId) {
        log.debug("Finding api definitions by ontologyId: {}", ontologyId);
        List<ApiDefinitionPO> poList = apiDefinitionPOMapper.selectByOntologyId(ontologyId);
        return apiDefinitionConverter.toEntityList(poList);
    }

    @Override
    public ApiDefinition save(ApiDefinition entity) {
        log.debug("Saving api definition: {}", entity.getId());
        ApiDefinitionPO po = apiDefinitionConverter.toPO(entity);
        if (po.getCreatedAt() == null) {
            po.setCreatedAt(Instant.now());
        }
        po.setUpdatedAt(Instant.now());
        if (apiDefinitionPOMapper.selectById(entity.getId()) != null) {
            apiDefinitionPOMapper.updateById(po);
        } else {
            apiDefinitionPOMapper.insert(po);
        }
        return entity;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Soft-deleting api definition: {}", id);
        ApiDefinitionPO po = apiDefinitionPOMapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            apiDefinitionPOMapper.updateById(po);
        }
    }
}

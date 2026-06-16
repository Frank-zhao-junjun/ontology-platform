package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.IndicatorDefinition;
import com.ontology.platform.domain.repository.IndicatorDefinitionRepository;
import com.ontology.platform.infrastructure.converter.IndicatorDefinitionConverter;
import com.ontology.platform.infrastructure.persistence.IndicatorDefinitionPO;
import com.ontology.platform.infrastructure.persistence.IndicatorDefinitionPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class IndicatorDefinitionRepositoryImpl implements IndicatorDefinitionRepository {

    private final IndicatorDefinitionPOMapper indicatorDefinitionPOMapper;
    private final IndicatorDefinitionConverter indicatorDefinitionConverter;

    @Override
    public Optional<IndicatorDefinition> findById(String id) {
        log.debug("Finding indicator definition by id: {}", id);
        IndicatorDefinitionPO po = indicatorDefinitionPOMapper.selectById(id);
        return Optional.ofNullable(indicatorDefinitionConverter.toEntity(po));
    }

    @Override
    public List<IndicatorDefinition> findByOntologyId(String ontologyId) {
        log.debug("Finding indicator definitions by ontologyId: {}", ontologyId);
        List<IndicatorDefinitionPO> poList = indicatorDefinitionPOMapper.selectByOntologyId(ontologyId);
        return indicatorDefinitionConverter.toEntityList(poList);
    }

    @Override
    public IndicatorDefinition save(IndicatorDefinition entity) {
        log.debug("Saving indicator definition: {}", entity.getId());
        IndicatorDefinitionPO po = indicatorDefinitionConverter.toPO(entity);
        if (po.getCreatedAt() == null) {
            po.setCreatedAt(Instant.now());
        }
        po.setUpdatedAt(Instant.now());
        if (indicatorDefinitionPOMapper.selectById(entity.getId()) != null) {
            indicatorDefinitionPOMapper.updateById(po);
        } else {
            indicatorDefinitionPOMapper.insert(po);
        }
        return entity;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Soft-deleting indicator definition: {}", id);
        IndicatorDefinitionPO po = indicatorDefinitionPOMapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            indicatorDefinitionPOMapper.updateById(po);
        }
    }
}

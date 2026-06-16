package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.ProbeDefinition;
import com.ontology.platform.domain.repository.ProbeDefinitionRepository;
import com.ontology.platform.infrastructure.converter.ProbeDefinitionConverter;
import com.ontology.platform.infrastructure.persistence.ProbeDefinitionPO;
import com.ontology.platform.infrastructure.persistence.ProbeDefinitionPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProbeDefinitionRepositoryImpl implements ProbeDefinitionRepository {

    private final ProbeDefinitionPOMapper probeDefinitionPOMapper;
    private final ProbeDefinitionConverter probeDefinitionConverter;

    @Override
    public Optional<ProbeDefinition> findById(String id) {
        log.debug("Finding probe definition by id: {}", id);
        ProbeDefinitionPO po = probeDefinitionPOMapper.selectById(id);
        return Optional.ofNullable(probeDefinitionConverter.toEntity(po));
    }

    @Override
    public List<ProbeDefinition> findByOntologyId(String ontologyId) {
        log.debug("Finding probe definitions by ontologyId: {}", ontologyId);
        List<ProbeDefinitionPO> poList = probeDefinitionPOMapper.selectByOntologyId(ontologyId);
        return probeDefinitionConverter.toEntityList(poList);
    }

    @Override
    public ProbeDefinition save(ProbeDefinition entity) {
        log.debug("Saving probe definition: {}", entity.getId());
        ProbeDefinitionPO po = probeDefinitionConverter.toPO(entity);
        if (po.getCreatedAt() == null) {
            po.setCreatedAt(Instant.now());
        }
        po.setUpdatedAt(Instant.now());
        if (probeDefinitionPOMapper.selectById(entity.getId()) != null) {
            probeDefinitionPOMapper.updateById(po);
        } else {
            probeDefinitionPOMapper.insert(po);
        }
        return entity;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Soft-deleting probe definition: {}", id);
        ProbeDefinitionPO po = probeDefinitionPOMapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            probeDefinitionPOMapper.updateById(po);
        }
    }
}

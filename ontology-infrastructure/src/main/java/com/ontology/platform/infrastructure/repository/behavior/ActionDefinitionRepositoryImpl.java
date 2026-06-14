package com.ontology.platform.infrastructure.repository.behavior;

import com.ontology.platform.domain.entity.ActionDefinition;
import com.ontology.platform.domain.repository.behavior.ActionDefinitionRepository;
import com.ontology.platform.infrastructure.converter.ActionDefinitionConverter;
import com.ontology.platform.infrastructure.persistence.ActionDefinitionPO;
import com.ontology.platform.infrastructure.persistence.ActionDefinitionPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ActionDefinitionRepositoryImpl implements ActionDefinitionRepository {

    private final ActionDefinitionPOMapper actionDefinitionPOMapper;
    private final ActionDefinitionConverter actionDefinitionConverter;

    @Override
    public Optional<ActionDefinition> findById(String id) {
        log.debug("Finding action definition by id: {}", id);
        ActionDefinitionPO po = actionDefinitionPOMapper.selectById(id);
        return Optional.ofNullable(actionDefinitionConverter.toEntity(po));
    }

    @Override
    public List<ActionDefinition> findByOntologyId(String ontologyId) {
        log.debug("Finding action definitions by ontologyId: {}", ontologyId);
        List<ActionDefinitionPO> poList = actionDefinitionPOMapper.selectByOntologyId(ontologyId);
        return actionDefinitionConverter.toEntityList(poList);
    }

    @Override
    public List<ActionDefinition> findByOntologyIdAndEntityId(String ontologyId, String entityId) {
        log.debug("Finding action definitions by ontologyId+entityId: {}, {}", ontologyId, entityId);
        List<ActionDefinitionPO> poList = actionDefinitionPOMapper.selectByOntologyIdAndEntityId(ontologyId, entityId);
        return actionDefinitionConverter.toEntityList(poList);
    }

    @Override
    public List<ActionDefinition> findByOntologyIdAndDomain(String ontologyId, String domain) {
        log.debug("Finding action definitions by ontologyId+domain: {}, {}", ontologyId, domain);
        List<ActionDefinitionPO> poList = actionDefinitionPOMapper.selectByOntologyIdAndDomain(ontologyId, domain);
        return actionDefinitionConverter.toEntityList(poList);
    }

    @Override
    public ActionDefinition save(ActionDefinition entity) {
        log.debug("Saving action definition: {}", entity.getId());
        ActionDefinitionPO po = actionDefinitionConverter.toPO(entity);
        if (po.getCreatedAt() == null) {
            po.setCreatedAt(Instant.now());
        }
        po.setUpdatedAt(Instant.now());
        if (actionDefinitionPOMapper.selectById(entity.getId()) != null) {
            actionDefinitionPOMapper.updateById(po);
        } else {
            actionDefinitionPOMapper.insert(po);
        }
        return entity;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Soft-deleting action definition: {}", id);
        ActionDefinitionPO po = actionDefinitionPOMapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            actionDefinitionPOMapper.updateById(po);
        }
    }
}

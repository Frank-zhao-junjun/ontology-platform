package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.NotificationDefinition;
import com.ontology.platform.domain.repository.NotificationDefinitionRepository;
import com.ontology.platform.infrastructure.converter.NotificationDefinitionConverter;
import com.ontology.platform.infrastructure.persistence.NotificationDefinitionPO;
import com.ontology.platform.infrastructure.persistence.NotificationDefinitionPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationDefinitionRepositoryImpl implements NotificationDefinitionRepository {

    private final NotificationDefinitionPOMapper notificationDefinitionPOMapper;
    private final NotificationDefinitionConverter notificationDefinitionConverter;

    @Override
    public Optional<NotificationDefinition> findById(String id) {
        log.debug("Finding notification definition by id: {}", id);
        NotificationDefinitionPO po = notificationDefinitionPOMapper.selectById(id);
        return Optional.ofNullable(notificationDefinitionConverter.toEntity(po));
    }

    @Override
    public List<NotificationDefinition> findByOntologyId(String ontologyId) {
        log.debug("Finding notification definitions by ontologyId: {}", ontologyId);
        List<NotificationDefinitionPO> poList = notificationDefinitionPOMapper.selectByOntologyId(ontologyId);
        return notificationDefinitionConverter.toEntityList(poList);
    }

    @Override
    public NotificationDefinition save(NotificationDefinition entity) {
        log.debug("Saving notification definition: {}", entity.getId());
        NotificationDefinitionPO po = notificationDefinitionConverter.toPO(entity);
        if (po.getCreatedAt() == null) {
            po.setCreatedAt(Instant.now());
        }
        po.setUpdatedAt(Instant.now());
        if (notificationDefinitionPOMapper.selectById(entity.getId()) != null) {
            notificationDefinitionPOMapper.updateById(po);
        } else {
            notificationDefinitionPOMapper.insert(po);
        }
        return entity;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Soft-deleting notification definition: {}", id);
        NotificationDefinitionPO po = notificationDefinitionPOMapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            notificationDefinitionPOMapper.updateById(po);
        }
    }
}

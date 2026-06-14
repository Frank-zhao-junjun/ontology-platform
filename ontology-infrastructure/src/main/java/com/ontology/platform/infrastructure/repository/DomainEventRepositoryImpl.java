package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.DomainEvent;
import com.ontology.platform.domain.repository.DomainEventRepository;
import com.ontology.platform.infrastructure.converter.DomainEventConverter;
import com.ontology.platform.infrastructure.persistence.DomainEventPO;
import com.ontology.platform.infrastructure.persistence.DomainEventPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DomainEventRepositoryImpl implements DomainEventRepository {

    private final DomainEventPOMapper domainEventPOMapper;
    private final DomainEventConverter domainEventConverter;

    @Override
    public Optional<DomainEvent> findById(String id) {
        log.debug("Finding domain event by id: {}", id);
        DomainEventPO po = domainEventPOMapper.selectById(id);
        return Optional.ofNullable(domainEventConverter.toEntity(po));
    }

    @Override
    public List<DomainEvent> findByOntologyId(String ontologyId) {
        log.debug("Finding domain events by ontologyId: {}", ontologyId);
        List<DomainEventPO> poList = domainEventPOMapper.selectByOntologyId(ontologyId);
        return domainEventConverter.toEntityList(poList);
    }

    @Override
    public List<DomainEvent> findByOntologyIdAndEntityId(String ontologyId, String entityId) {
        log.debug("Finding domain events by ontologyId+entityId: {}, {}", ontologyId, entityId);
        List<DomainEventPO> poList = domainEventPOMapper.selectByOntologyIdAndEntityId(ontologyId, entityId);
        return domainEventConverter.toEntityList(poList);
    }

    @Override
    public List<DomainEvent> findByOntologyIdAndEventType(String ontologyId, String eventType) {
        log.debug("Finding domain events by ontologyId+eventType: {}, {}", ontologyId, eventType);
        List<DomainEventPO> poList = domainEventPOMapper.selectByOntologyIdAndEventType(ontologyId, eventType);
        return domainEventConverter.toEntityList(poList);
    }

    @Override
    public DomainEvent save(DomainEvent entity) {
        log.debug("Saving domain event: {}", entity.getId());
        DomainEventPO po = domainEventConverter.toPO(entity);
        if (po.getCreatedAt() == null) {
            po.setCreatedAt(Instant.now());
        }
        po.setUpdatedAt(Instant.now());
        if (domainEventPOMapper.selectById(entity.getId()) != null) {
            domainEventPOMapper.updateById(po);
        } else {
            domainEventPOMapper.insert(po);
        }
        return entity;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Soft-deleting domain event: {}", id);
        DomainEventPO po = domainEventPOMapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            domainEventPOMapper.updateById(po);
        }
    }
}

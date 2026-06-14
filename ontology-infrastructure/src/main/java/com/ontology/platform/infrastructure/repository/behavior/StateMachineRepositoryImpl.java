package com.ontology.platform.infrastructure.repository.behavior;

import com.ontology.platform.domain.entity.StateMachine;
import com.ontology.platform.domain.repository.behavior.StateMachineRepository;
import com.ontology.platform.infrastructure.converter.StateMachineConverter;
import com.ontology.platform.infrastructure.persistence.StateMachinePO;
import com.ontology.platform.infrastructure.persistence.StateMachinePOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StateMachineRepositoryImpl implements StateMachineRepository {

    private final StateMachinePOMapper stateMachinePOMapper;
    private final StateMachineConverter stateMachineConverter;

    @Override
    public Optional<StateMachine> findById(String id) {
        log.debug("Finding state machine by id: {}", id);
        StateMachinePO po = stateMachinePOMapper.selectById(id);
        return Optional.ofNullable(stateMachineConverter.toEntity(po));
    }

    @Override
    public List<StateMachine> findByOntologyId(String ontologyId) {
        log.debug("Finding state machines by ontologyId: {}", ontologyId);
        List<StateMachinePO> poList = stateMachinePOMapper.selectByOntologyId(ontologyId);
        return stateMachineConverter.toEntityList(poList);
    }

    @Override
    public List<StateMachine> findByOntologyIdAndEntityId(String ontologyId, String entityId) {
        log.debug("Finding state machines by ontologyId+entityId: {}, {}", ontologyId, entityId);
        List<StateMachinePO> poList = stateMachinePOMapper.selectByOntologyIdAndEntityId(ontologyId, entityId);
        return stateMachineConverter.toEntityList(poList);
    }

    @Override
    public StateMachine save(StateMachine entity) {
        log.debug("Saving state machine: {}", entity.getId());
        StateMachinePO po = stateMachineConverter.toPO(entity);
        if (po.getCreatedAt() == null) {
            po.setCreatedAt(Instant.now());
        }
        po.setUpdatedAt(Instant.now());
        if (stateMachinePOMapper.selectById(entity.getId()) != null) {
            stateMachinePOMapper.updateById(po);
        } else {
            stateMachinePOMapper.insert(po);
        }
        return entity;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Soft-deleting state machine: {}", id);
        StateMachinePO po = stateMachinePOMapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            stateMachinePOMapper.updateById(po);
        }
    }
}

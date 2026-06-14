package com.ontology.platform.infrastructure.repository.behavior;

import com.ontology.platform.domain.entity.StateTransition;
import com.ontology.platform.domain.repository.behavior.StateTransitionRepository;
import com.ontology.platform.infrastructure.converter.StateTransitionConverter;
import com.ontology.platform.infrastructure.persistence.StateTransitionPO;
import com.ontology.platform.infrastructure.persistence.StateTransitionPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StateTransitionRepositoryImpl implements StateTransitionRepository {

    private final StateTransitionPOMapper stateTransitionPOMapper;
    private final StateTransitionConverter stateTransitionConverter;

    @Override
    public Optional<StateTransition> findById(String id) {
        log.debug("Finding state transition by id: {}", id);
        StateTransitionPO po = stateTransitionPOMapper.selectById(id);
        return Optional.ofNullable(stateTransitionConverter.toEntity(po));
    }

    @Override
    public List<StateTransition> findByStateMachineId(String stateMachineId) {
        log.debug("Finding state transitions by stateMachineId: {}", stateMachineId);
        List<StateTransitionPO> poList = stateTransitionPOMapper.selectByStateMachineId(stateMachineId);
        return stateTransitionConverter.toEntityList(poList);
    }

    @Override
    public StateTransition save(StateTransition entity) {
        log.debug("Saving state transition: {}", entity.getId());
        StateTransitionPO po = stateTransitionConverter.toPO(entity);
        if (stateTransitionPOMapper.selectById(entity.getId()) != null) {
            stateTransitionPOMapper.updateById(po);
        } else {
            stateTransitionPOMapper.insert(po);
        }
        return entity;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Deleting state transition: {}", id);
        stateTransitionPOMapper.deleteById(id);
    }
}

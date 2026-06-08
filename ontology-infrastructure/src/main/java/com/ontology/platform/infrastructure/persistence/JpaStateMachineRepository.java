package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.StateMachine;
import com.ontology.platform.domain.repository.StateMachineRepository;
import com.ontology.platform.infrastructure.persistence.entity.StateMachineEntity;
import com.ontology.platform.infrastructure.repository.StateMachineJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaStateMachineRepository implements StateMachineRepository {
    private final StateMachineJpaRepository jpa;

    @Override
    public StateMachine save(StateMachine sm) {
        StateMachineEntity entity = PersistenceMapper.toEntity(sm);
        StateMachineEntity saved = jpa.save(entity);
        return PersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<StateMachine> findById(String id) {
        return jpa.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<StateMachine> findByContextId(String contextId) {
        return jpa.findByContextIdOrderByCreatedAtAsc(contextId).stream()
                .map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<StateMachine> findByObjectTypeId(String objectTypeId) {
        return jpa.findByObjectTypeId(objectTypeId).stream()
                .map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByContextIdAndObjectTypeId(String contextId, String objectTypeId) {
        return jpa.existsByContextIdAndObjectTypeId(contextId, objectTypeId);
    }
}

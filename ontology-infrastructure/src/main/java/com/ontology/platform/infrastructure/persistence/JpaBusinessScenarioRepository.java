package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.BusinessScenario;
import com.ontology.platform.domain.repository.BusinessScenarioRepository;
import com.ontology.platform.infrastructure.persistence.entity.BusinessScenarioEntity;
import com.ontology.platform.infrastructure.repository.BusinessScenarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaBusinessScenarioRepository implements BusinessScenarioRepository {
    private final BusinessScenarioJpaRepository jpa;

    @Override
    public BusinessScenario save(BusinessScenario scenario) {
        BusinessScenarioEntity entity = PersistenceMapper.toEntity(scenario);
        BusinessScenarioEntity saved = jpa.save(entity);
        return PersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<BusinessScenario> findById(String id) {
        return jpa.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<BusinessScenario> findByContextId(String contextId) {
        return jpa.findByContextIdOrderByCreatedAtAsc(contextId).stream()
                .map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByContextIdAndCode(String contextId, String code) {
        return jpa.existsByContextIdAndCode(contextId, code);
    }
}

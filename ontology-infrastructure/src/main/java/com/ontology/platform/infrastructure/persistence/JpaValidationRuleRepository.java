package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.ValidationRule;
import com.ontology.platform.domain.repository.ValidationRuleRepository;
import com.ontology.platform.infrastructure.repository.ValidationRuleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaValidationRuleRepository implements ValidationRuleRepository {
    private final ValidationRuleJpaRepository jpa;

    @Override
    public void save(ValidationRule rule) {
        jpa.save(PersistenceMapper.toEntity(rule));
    }

    @Override
    public Optional<ValidationRule> findById(String id) {
        return jpa.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<ValidationRule> findByContextIdAndManifestCode(String contextId, String manifestCode) {
        return jpa.findByContextIdAndManifestCode(contextId, manifestCode).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<ValidationRule> findByContextId(String contextId) {
        return jpa.findByContextId(contextId).stream().map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByContextIdAndManifestCode(String contextId, String manifestCode) {
        return jpa.existsByContextIdAndManifestCode(contextId, manifestCode);
    }
}

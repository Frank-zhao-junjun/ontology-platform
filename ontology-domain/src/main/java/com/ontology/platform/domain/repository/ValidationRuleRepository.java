package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.ValidationRule;

import java.util.List;
import java.util.Optional;

public interface ValidationRuleRepository {
    void save(ValidationRule rule);
    Optional<ValidationRule> findById(String id);
    Optional<ValidationRule> findByContextIdAndManifestCode(String contextId, String manifestCode);
    List<ValidationRule> findByContextId(String contextId);
    boolean existsByContextIdAndManifestCode(String contextId, String manifestCode);
}

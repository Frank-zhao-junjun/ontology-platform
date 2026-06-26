package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.GovernanceRole;
import java.util.List;
import java.util.Optional;

public interface GovernanceRoleRepository {
    GovernanceRole save(GovernanceRole role);
    Optional<GovernanceRole> findById(String id);
    List<GovernanceRole> findByOntologyId(String ontologyId);
    void deleteById(String id);
}

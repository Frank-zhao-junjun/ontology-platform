package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.Role;
import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    void save(Role role);
    Optional<Role> findById(String id);
    List<Role> findAll();
    boolean existsByContextIdAndCode(String contextId, String code);
}

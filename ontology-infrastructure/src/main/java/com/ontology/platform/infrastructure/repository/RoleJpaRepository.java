package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, String> {
    boolean existsByContextIdAndCode(String contextId, String code);
}

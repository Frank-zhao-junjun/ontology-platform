package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.BoundedContextEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BoundedContextJpaRepository extends JpaRepository<BoundedContextEntity, String> {
    Optional<BoundedContextEntity> findByCode(String code);
    boolean existsByCode(String code);
}

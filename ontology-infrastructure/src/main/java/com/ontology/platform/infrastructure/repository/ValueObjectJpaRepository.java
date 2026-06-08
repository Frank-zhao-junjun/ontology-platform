package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.ValueObjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ValueObjectJpaRepository extends JpaRepository<ValueObjectEntity, String> {
    List<ValueObjectEntity> findAllByOrderByCreatedAtAsc();
    boolean existsByCode(String code);
    Optional<ValueObjectEntity> findByCode(String code);
}

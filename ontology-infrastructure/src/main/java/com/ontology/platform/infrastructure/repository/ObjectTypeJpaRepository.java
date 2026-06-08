package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.ObjectTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ObjectTypeJpaRepository extends JpaRepository<ObjectTypeEntity, String> {
    List<ObjectTypeEntity> findByContextId(String contextId);
}

package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.PublishedManifestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PublishedManifestJpaRepository extends JpaRepository<PublishedManifestEntity, String> {
    List<PublishedManifestEntity> findByContextIdOrderByCreatedAtDesc(String contextId);
    int countByContextId(String contextId);
    Optional<PublishedManifestEntity> findFirstByContextIdOrderByCreatedAtDesc(String contextId);
}

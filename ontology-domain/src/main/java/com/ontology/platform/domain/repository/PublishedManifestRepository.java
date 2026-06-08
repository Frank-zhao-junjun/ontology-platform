package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.PublishedManifest;

import java.util.List;
import java.util.Optional;

public interface PublishedManifestRepository {
    void save(PublishedManifest manifest);
    List<PublishedManifest> findByContextId(String contextId);
    Optional<PublishedManifest> findLatestByContextId(String contextId);
    int countByContextId(String contextId);
}

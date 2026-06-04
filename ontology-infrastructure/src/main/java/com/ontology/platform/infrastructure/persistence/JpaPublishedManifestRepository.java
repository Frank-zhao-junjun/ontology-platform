package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.PublishedManifest;
import com.ontology.platform.domain.repository.PublishedManifestRepository;
import com.ontology.platform.infrastructure.repository.PublishedManifestJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaPublishedManifestRepository implements PublishedManifestRepository {
    private final PublishedManifestJpaRepository jpa;

    @Override
    public void save(PublishedManifest manifest) {
        jpa.save(PersistenceMapper.toEntity(manifest));
    }

    @Override
    public List<PublishedManifest> findByContextId(String contextId) {
        return jpa.findByContextIdOrderByCreatedAtDesc(contextId).stream()
                .map(PersistenceMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<PublishedManifest> findLatestByContextId(String contextId) {
        return jpa.findFirstByContextIdOrderByCreatedAtDesc(contextId).map(PersistenceMapper::toDomain);
    }

    @Override
    public int countByContextId(String contextId) {
        return jpa.countByContextId(contextId);
    }
}

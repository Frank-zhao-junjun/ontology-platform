package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.ReviewComment;
import com.ontology.platform.domain.repository.ReviewCommentRepository;
import com.ontology.platform.infrastructure.repository.ReviewCommentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaReviewCommentRepository implements ReviewCommentRepository {
    private final ReviewCommentJpaRepository jpa;

    @Override
    public void save(ReviewComment comment) {
        jpa.save(PersistenceMapper.toEntity(comment));
    }

    @Override
    public Optional<ReviewComment> findById(String id) {
        return jpa.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<ReviewComment> findByContextId(String contextId) {
        return jpa.findByContextIdOrderByCreatedAtDesc(contextId).stream()
                .map(PersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewComment> findByTarget(String targetType, String targetId) {
        return jpa.findByTargetTypeAndTargetId(targetType, targetId).stream()
                .map(PersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }
}

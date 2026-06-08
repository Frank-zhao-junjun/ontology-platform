package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.ReviewComment;

import java.util.List;
import java.util.Optional;

public interface ReviewCommentRepository {
    void save(ReviewComment comment);
    Optional<ReviewComment> findById(String id);
    List<ReviewComment> findByContextId(String contextId);
    List<ReviewComment> findByTarget(String targetType, String targetId);
}

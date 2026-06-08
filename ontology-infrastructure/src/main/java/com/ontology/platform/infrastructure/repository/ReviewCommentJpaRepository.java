package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.ReviewCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewCommentJpaRepository extends JpaRepository<ReviewCommentEntity, String> {
    List<ReviewCommentEntity> findByContextIdOrderByCreatedAtDesc(String contextId);
    List<ReviewCommentEntity> findByTargetTypeAndTargetId(String targetType, String targetId);
}

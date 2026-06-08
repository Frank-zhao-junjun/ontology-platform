package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "review_comments")
@Getter
@Setter
public class ReviewCommentEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "context_id", nullable = false, length = 36)
    private String contextId;
    @Column(name = "target_type", nullable = false, length = 30)
    private String targetType;
    @Column(name = "target_id", nullable = false, length = 36)
    private String targetId;
    @Column(nullable = false, length = 100)
    private String reviewer;
    @Column(nullable = false, length = 20)
    private String resolution;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(name = "created_at")
    private Instant createdAt;
    @Column(name = "resolved_at")
    private Instant resolvedAt;
}

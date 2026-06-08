package com.ontology.platform.domain.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * 审核批注 (US-G05)。
 * 批注可挂载到任意建模元素（聚合根/对象类型/行为/事件/校验规则）。
 * Demo/测试阶段不强制双人审核，批注为可选功能。
 */
@Getter
public class ReviewComment {
    private final String id, contextId, targetType, targetId, reviewer, content;
    private String resolution;
    private final Instant createdAt;
    private Instant resolvedAt;

    @Builder
    public ReviewComment(String id, String contextId, String targetType, String targetId,
                         String reviewer, String resolution, String content,
                         Instant createdAt, Instant resolvedAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.contextId = contextId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reviewer = reviewer;
        this.resolution = resolution != null ? resolution : "PENDING";
        this.content = content;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.resolvedAt = resolvedAt;
    }

    public static ReviewComment create(String contextId, String targetType, String targetId,
                                       String reviewer, String resolution, String content) {
        return ReviewComment.builder()
                .contextId(contextId).targetType(targetType).targetId(targetId)
                .reviewer(reviewer).resolution(resolution).content(content).build();
    }

    public void resolve(String resolution) {
        this.resolution = resolution;
        this.resolvedAt = Instant.now();
    }
}

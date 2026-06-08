package com.ontology.platform.api.dto;

import com.ontology.platform.domain.entity.ReviewComment;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class ReviewCommentResponse {
    private String id;
    private String contextId;
    private String targetType;
    private String targetId;
    private String reviewer;
    private String resolution;
    private String content;
    private Instant createdAt;
    private Instant resolvedAt;

    public static ReviewCommentResponse from(ReviewComment c) {
        return ReviewCommentResponse.builder()
                .id(c.getId()).contextId(c.getContextId())
                .targetType(c.getTargetType()).targetId(c.getTargetId())
                .reviewer(c.getReviewer()).resolution(c.getResolution())
                .content(c.getContent()).createdAt(c.getCreatedAt())
                .resolvedAt(c.getResolvedAt()).build();
    }

    public Map<String, Object> toMap() {
        return Map.of("id", id, "contextId", contextId, "targetType", targetType,
                "targetId", targetId, "reviewer", reviewer, "resolution", resolution,
                "content", content, "createdAt", createdAt != null ? createdAt.toString() : null,
                "resolvedAt", resolvedAt != null ? resolvedAt.toString() : null);
    }
}

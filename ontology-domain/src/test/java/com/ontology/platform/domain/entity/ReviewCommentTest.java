package com.ontology.platform.domain.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewCommentTest {

    @Test
    void createShouldSetDefaults() {
        ReviewComment c = ReviewComment.create("ctx-1", "BEHAVIOR", "bhv-1",
                "reviewer1", "APPROVED", "行为定义正确");

        assertThat(c.getId()).isNotBlank();
        assertThat(c.getContextId()).isEqualTo("ctx-1");
        assertThat(c.getTargetType()).isEqualTo("BEHAVIOR");
        assertThat(c.getTargetId()).isEqualTo("bhv-1");
        assertThat(c.getReviewer()).isEqualTo("reviewer1");
        assertThat(c.getResolution()).isEqualTo("APPROVED");
        assertThat(c.getContent()).isEqualTo("行为定义正确");
        assertThat(c.getCreatedAt()).isNotNull();
        assertThat(c.getResolvedAt()).isNull();
    }

    @Test
    void createShouldDefaultResolutionToPending() {
        ReviewComment c = ReviewComment.create("ctx-1", "AGGREGATE_ROOT", "ar-1",
                "reviewer1", null, "需要补充描述");

        assertThat(c.getResolution()).isEqualTo("PENDING");
    }

    @Test
    void resolveShouldUpdateResolutionAndSetTimestamp() {
        ReviewComment c = ReviewComment.create("ctx-1", "RULE", "rule-1",
                "reviewer1", "PENDING", "校验逻辑需调整");

        c.resolve("APPROVED");

        assertThat(c.getResolution()).isEqualTo("APPROVED");
        assertThat(c.getResolvedAt()).isNotNull();
    }

    @Test
    void builderShouldAllowCustomId() {
        ReviewComment c = ReviewComment.builder().id("custom-id").contextId("ctx-1")
                .targetType("EVENT").targetId("evt-1").reviewer("r1")
                .content("test").build();

        assertThat(c.getId()).isEqualTo("custom-id");
    }
}

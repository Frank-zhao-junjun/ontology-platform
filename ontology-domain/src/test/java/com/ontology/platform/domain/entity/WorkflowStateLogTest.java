package com.ontology.platform.domain.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowStateLogTest {

    @Test
    void recordShouldCreateLogWithDefaults() {
        WorkflowStateLog log = WorkflowStateLog.record("ctx-1", "DRAFT", "IN_REVIEW",
                "user1", "提交审核");

        assertThat(log.getId()).isNotBlank();
        assertThat(log.getContextId()).isEqualTo("ctx-1");
        assertThat(log.getFromState()).isEqualTo("DRAFT");
        assertThat(log.getToState()).isEqualTo("IN_REVIEW");
        assertThat(log.getOperatedBy()).isEqualTo("user1");
        assertThat(log.getComment()).isEqualTo("提交审核");
        assertThat(log.getOperatedAt()).isNotNull();
    }

    @Test
    void recordShouldAllowNullComment() {
        WorkflowStateLog log = WorkflowStateLog.record("ctx-1", "IN_REVIEW", "PUBLISHED",
                "admin", null);

        assertThat(log.getComment()).isNull();
        assertThat(log.getToState()).isEqualTo("PUBLISHED");
    }

    @Test
    void builderShouldAllowCustomId() {
        WorkflowStateLog log = WorkflowStateLog.builder().id("custom-id")
                .contextId("ctx-1").fromState("DRAFT").toState("IN_REVIEW")
                .operatedBy("user1").build();

        assertThat(log.getId()).isEqualTo("custom-id");
    }
}

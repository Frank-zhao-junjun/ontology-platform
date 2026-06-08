package com.ontology.platform.domain.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * 建模工作流状态流转日志 (US-G05)。
 * 记录限界上下文每次状态变更的完整审计轨迹。
 */
@Getter
public class WorkflowStateLog {
    private final String id, contextId, fromState, toState, operatedBy, comment;
    private final Instant operatedAt;

    @Builder
    public WorkflowStateLog(String id, String contextId, String fromState, String toState,
                            String operatedBy, String comment, Instant operatedAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.contextId = contextId;
        this.fromState = fromState;
        this.toState = toState;
        this.operatedBy = operatedBy;
        this.comment = comment;
        this.operatedAt = operatedAt != null ? operatedAt : Instant.now();
    }

    public static WorkflowStateLog record(String contextId, String fromState, String toState,
                                          String operatedBy, String comment) {
        return WorkflowStateLog.builder()
                .contextId(contextId).fromState(fromState).toState(toState)
                .operatedBy(operatedBy).comment(comment).build();
    }
}

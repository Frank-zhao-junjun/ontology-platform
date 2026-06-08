package com.ontology.platform.api.dto;

import com.ontology.platform.domain.entity.WorkflowStateLog;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class WorkflowStateLogResponse {
    private String id;
    private String contextId;
    private String fromState;
    private String toState;
    private String operatedBy;
    private Instant operatedAt;
    private String comment;

    public static WorkflowStateLogResponse from(WorkflowStateLog log) {
        return WorkflowStateLogResponse.builder()
                .id(log.getId()).contextId(log.getContextId())
                .fromState(log.getFromState()).toState(log.getToState())
                .operatedBy(log.getOperatedBy()).operatedAt(log.getOperatedAt())
                .comment(log.getComment()).build();
    }

    public Map<String, Object> toMap() {
        return Map.of("id", id, "contextId", contextId, "fromState", fromState,
                "toState", toState, "operatedBy", operatedBy,
                "operatedAt", operatedAt != null ? operatedAt.toString() : null, "comment", comment);
    }
}

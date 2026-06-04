package com.ontology.platform.domain.entity;

import com.ontology.platform.common.enums.DomainTag;
import com.ontology.platform.common.enums.WorkflowState;
import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;
import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BoundedContext {
    private final String id, name, code, description, ontologyId, createdBy;
    private final DomainTag domainTag;
    private WorkflowState workflowState;
    private final Instant createdAt;
    private Instant updatedAt;

    @Builder
    private BoundedContext(String id, String name, String code, String description,
                           DomainTag domainTag, String ontologyId, WorkflowState workflowState,
                           String createdBy, Instant createdAt, Instant updatedAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name; this.code = code; this.description = description;
        this.domainTag = domainTag;
        this.ontologyId = ontologyId != null ? ontologyId : UUID.randomUUID().toString();
        this.workflowState = workflowState != null ? workflowState : WorkflowState.DRAFT;
        this.createdBy = createdBy;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static BoundedContext create(String name, String code, String description, DomainTag domainTag, String createdBy) {
        return BoundedContext.builder().name(name).code(code).description(description).domainTag(domainTag).createdBy(createdBy).build();
    }

    public void submitForReview() { assertState(WorkflowState.DRAFT); this.workflowState = WorkflowState.IN_REVIEW; this.updatedAt = Instant.now(); }
    public void approveAndPublish() { assertState(WorkflowState.IN_REVIEW); this.workflowState = WorkflowState.PUBLISHED; this.updatedAt = Instant.now(); }
    public void rejectToDraft() { assertState(WorkflowState.IN_REVIEW); this.workflowState = WorkflowState.DRAFT; this.updatedAt = Instant.now(); }
    public boolean isPublished() { return this.workflowState == WorkflowState.PUBLISHED; }

    private void assertState(WorkflowState expected) {
        if (this.workflowState != expected) throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Expected " + expected + " but was " + this.workflowState);
    }
}

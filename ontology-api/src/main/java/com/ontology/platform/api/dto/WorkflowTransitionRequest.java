package com.ontology.platform.api.dto;

import lombok.Data;

@Data
public class WorkflowTransitionRequest {
    private String operatedBy = "user";
    private String comment;
}

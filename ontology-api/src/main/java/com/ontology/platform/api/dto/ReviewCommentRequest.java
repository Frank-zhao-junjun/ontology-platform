package com.ontology.platform.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewCommentRequest {
    @NotBlank
    private String targetType;   // AGGREGATE_ROOT | OBJECT_TYPE | BEHAVIOR | EVENT | RULE
    @NotBlank
    private String targetId;
    @NotBlank
    private String reviewer;
    private String resolution = "PENDING";  // PENDING | APPROVED | NEEDS_CHANGE | REJECTED
    @NotBlank
    private String content;
}

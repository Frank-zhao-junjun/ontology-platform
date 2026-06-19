package com.ontology.platform.application.dto.governance;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 提交审批请求 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitApprovalRequest {

    @NotBlank(message = "agentId is required")
    private String agentId;

    private String actionId;

    @NotBlank(message = "requestedOp is required")
    private String requestedOp;

    private String reason;
}

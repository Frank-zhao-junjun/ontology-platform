package com.ontology.platform.application.dto.governance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "提交审批请求DTO")
public class SubmitApprovalRequest {

    @NotBlank(message = "agentId is required")
    private String agentId;

    private String actionId;

    @NotBlank(message = "requestedOp is required")
    private String requestedOp;

    private String reason;
}

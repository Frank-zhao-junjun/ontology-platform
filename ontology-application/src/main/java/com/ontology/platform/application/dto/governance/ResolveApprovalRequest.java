package com.ontology.platform.application.dto.governance;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "审批处理请求DTO，包含审批动作（通过/拒绝）")
public class ResolveApprovalRequest {
    @NotBlank private String action;  // APPROVE or REJECT
    private String reason;
    @NotBlank private String resolvedBy;
}

package com.ontology.platform.application.dto.governance;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ResolveApprovalRequest {
    @NotBlank private String action;  // APPROVE or REJECT
    private String reason;
    @NotBlank private String resolvedBy;
}

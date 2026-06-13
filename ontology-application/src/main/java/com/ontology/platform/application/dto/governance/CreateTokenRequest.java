package com.ontology.platform.application.dto.governance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CreateTokenRequest {
    @NotBlank @Size(max=200)
    private String agentId;
    @NotBlank
    private String tenantId;
    @Size(max=500)
    private String displayName;
    private List<String> domains;
    private String defaultRole;
    @Builder.Default
    private long ttlDays = 90;
}

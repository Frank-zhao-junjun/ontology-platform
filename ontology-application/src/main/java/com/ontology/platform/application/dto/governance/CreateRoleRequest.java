package com.ontology.platform.application.dto.governance;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateRoleRequest {
    @NotBlank private String tokenId;
    @NotBlank private String domain;
    @NotBlank private String role;
}

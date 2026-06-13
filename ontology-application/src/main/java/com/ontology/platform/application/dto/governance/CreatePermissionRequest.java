package com.ontology.platform.application.dto.governance;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CreatePermissionRequest {
    @NotBlank private String roleId;
    @NotBlank private String resource;
    private List<String> operations;
    @NotBlank private String domain;
}

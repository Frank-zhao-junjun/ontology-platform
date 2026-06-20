package com.ontology.platform.application.dto.governance;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建权限请求DTO")
public class CreatePermissionRequest {
    @NotBlank private String roleId;
    @NotBlank private String resource;
    private List<String> operations;
    @NotBlank private String domain;
}

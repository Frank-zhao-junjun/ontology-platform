package com.ontology.platform.application.dto.governance;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建角色请求DTO")
public class CreateRoleRequest {
    @NotBlank private String tokenId;
    @NotBlank private String domain;
    @NotBlank private String role;
}

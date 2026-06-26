package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "GovernanceRole响应")
public class GovernanceRoleResponse {
    @Schema(description = "ID") private String id;
    @Schema(description = "本体ID") private String ontologyId;
    @Schema(description = "角色名称") private String name;
    @Schema(description = "描述") private String description;
    @Schema(description = "权限列表") @Builder.Default
    private List<PermissionEntry> permissions = new ArrayList<>();
    @Schema(description = "创建时间") private Instant createdAt;
    @Schema(description = "更新时间") private Instant updatedAt;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PermissionEntry {
        private String objectTypeId;
        @Builder.Default private List<String> ops = new ArrayList<>();
    }
}

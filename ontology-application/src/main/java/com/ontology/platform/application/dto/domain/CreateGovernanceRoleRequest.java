package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建GovernanceRole请求")
public class CreateGovernanceRoleRequest {
    @Schema(description = "角色名称") private String name;
    @Schema(description = "描述") private String description;
    @Schema(description = "权限列表") @Builder.Default
    private List<PermissionEntry> permissions = new ArrayList<>();

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PermissionEntry {
        @Schema(description = "对象类型ID") private String objectTypeId;
        @Schema(description = "操作列表") @Builder.Default
        private List<String> ops = new ArrayList<>();
    }
}

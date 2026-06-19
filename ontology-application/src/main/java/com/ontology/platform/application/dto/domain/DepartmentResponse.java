package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Department响应")
public class DepartmentResponse {
    @Schema(description = "ID") private String id;
    @Schema(description = "name")
    private String name;
    @Schema(description = "name_en")
    private String nameEn;
    @Schema(description = "description")
    private String description;
    @Schema(description = "parent_department_id")
    private String parentDepartmentId;
    @Schema(description = "创建时间") private Instant createdAt;
    @Schema(description = "更新时间") private Instant updatedAt;
}

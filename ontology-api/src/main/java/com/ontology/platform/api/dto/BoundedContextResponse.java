package com.ontology.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "限界上下文响应")
public class BoundedContextResponse {

    @Schema(description = "上下文ID") private String id;
    @Schema(description = "上下文名称") private String name;
    @Schema(description = "上下文代码") private String code;
    @Schema(description = "描述") private String description;
    @Schema(description = "领域标签") private String domainTag;
    @Schema(description = "绑定的本体ID") private String ontologyId;
    @Schema(description = "工作流状态") private String workflowState;
    @Schema(description = "创建人") private String createdBy;
    @Schema(description = "创建时间") private Instant createdAt;
    @Schema(description = "更新时间") private Instant updatedAt;

    public static BoundedContextResponse from(com.ontology.platform.domain.entity.BoundedContext ctx) {
        return BoundedContextResponse.builder()
                .id(ctx.getId()).name(ctx.getName()).code(ctx.getCode())
                .description(ctx.getDescription()).domainTag(ctx.getDomainTag().getCode())
                .ontologyId(ctx.getOntologyId()).workflowState(ctx.getWorkflowState().name())
                .createdBy(ctx.getCreatedBy()).createdAt(ctx.getCreatedAt()).updatedAt(ctx.getUpdatedAt())
                .build();
    }
}

package com.ontology.platform.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新对象类型请求")
public class UpdateObjectTypeRequest {

    @Size(min = 1, max = 200, message = "显示名称长度必须在1-200之间")
    @Schema(description = "显示名称", example = "客户")
    private String displayName;

    @Size(max = 1000, message = "描述长度不能超过1000")
    @Schema(description = "描述", example = "更新后的对象类型描述")
    private String description;

    @Schema(description = "主键属性名", example = "id")
    private String primaryKey;

    @Schema(description = "实现的接口列表")
    private List<String> interfaceNames;

    @Schema(description = "父对象类型ID")
    private String parentId;

    @Schema(description = "实体角色: aggregate_root | child_entity", example = "aggregate_root")
    private String entityRole;

    @Schema(description = "父聚合根 ID (entityRole=child_entity 时必填)")
    private String parentAggregateId;

    @Schema(description = "所属业务场景 ID")
    private String businessScenarioId;

    @Schema(description = "子领域")
    private String subDomain;

    @Schema(description = "扩展属性 JSONB", example = "{}")
    private String attributesJsonb;
}

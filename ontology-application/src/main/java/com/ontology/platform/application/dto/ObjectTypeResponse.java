package com.ontology.platform.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "对象类型响应")
public class ObjectTypeResponse {

    @Schema(description = "对象类型ID")
    private String id;
    @Schema(description = "所属本体ID")
    private String ontologyId;
    @Schema(description = "对象类型名称")
    private String name;
    @Schema(description = "显示名称")
    private String displayName;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "主键属性名")
    private String primaryKey;
    @Schema(description = "父对象类型ID")
    private String parentId;

    @Schema(description = "实体角色: aggregate_root | child_entity")
    private String entityRole;

    @Schema(description = "父聚合根 ID")
    private String parentAggregateId;

    @Schema(description = "所属业务场景 ID")
    private String businessScenarioId;

    @Schema(description = "子领域")
    private String subDomain;

    @Schema(description = "扩展属性 JSONB (metadataTemplateId, referenceKind, masterDataType 等)")
    private String attributesJsonb;

    @Builder.Default
    @Schema(description = "实现的接口列表")
    private List<String> interfaceNames = new ArrayList<>();

    @Schema(description = "实例数量")
    private int instanceCount;
    @Schema(description = "创建时间")
    private Instant createdAt;
    @Schema(description = "更新时间")
    private Instant updatedAt;
}

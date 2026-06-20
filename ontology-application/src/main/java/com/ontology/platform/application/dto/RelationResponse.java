package com.ontology.platform.application.dto;

import com.ontology.platform.common.enums.RelationCardinality;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "关系响应")
public class RelationResponse {

    @Schema(description = "关系ID")
    private String id;
    @Schema(description = "所属本体ID")
    private String ontologyId;
    @Schema(description = "源对象类型ID")
    private String sourceTypeId;
    @Schema(description = "目标对象类型ID")
    private String targetTypeId;
    @Schema(description = "关系名称")
    private String name;
    @Schema(description = "显示名称")
    private String displayName;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "基数")
    private RelationCardinality cardinality;
    @Schema(description = "反向关系名")
    private String reverseName;
    @Schema(description = "反向关系显示名")
    private String reverseDisplayName;

    @Builder.Default
    @Schema(description = "关系属性列表")
    private List<RelationPropertyDTO> properties = new java.util.ArrayList<>();

    @Schema(description = "创建时间")
    private Instant createdAt;
    @Schema(description = "更新时间")
    private Instant updatedAt;
}

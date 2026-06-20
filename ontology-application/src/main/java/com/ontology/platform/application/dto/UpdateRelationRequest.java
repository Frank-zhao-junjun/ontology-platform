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
@Schema(description = "更新关系请求")
public class UpdateRelationRequest {

    @Size(min = 1, max = 200, message = "显示名称长度必须在1-200之间")
    @Schema(description = "显示名称", example = "拥有")
    private String displayName;

    @Size(max = 1000, message = "描述长度不能超过1000")
    @Schema(description = "描述")
    private String description;

    @Schema(description = "反向关系名", example = "OWNED_BY")
    private String reverseName;
    @Schema(description = "反向关系显示名", example = "被拥有")
    private String reverseDisplayName;
    @Schema(description = "关系属性列表")
    private List<RelationPropertyDTO> properties;
}

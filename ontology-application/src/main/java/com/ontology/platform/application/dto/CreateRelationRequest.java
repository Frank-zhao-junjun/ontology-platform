package com.ontology.platform.application.dto;

import com.ontology.platform.common.enums.RelationCardinality;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建关系请求")
public class CreateRelationRequest {

    @NotBlank(message = "本体ID不能为空")
    @Schema(description = "所属本体ID")
    private String ontologyId;

    @NotBlank(message = "源对象类型ID不能为空")
    @Schema(description = "源对象类型ID")
    private String sourceTypeId;

    @NotBlank(message = "目标对象类型ID不能为空")
    @Schema(description = "目标对象类型ID")
    private String targetTypeId;

    @NotBlank(message = "关系名不能为空")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "关系名必须以大写字母开头，只能包含大写字母、数字和下划线")
    @Size(min = 1, max = 100, message = "关系名长度必须在1-100之间")
    @Schema(description = "关系名", example = "OWNS")
    private String name;

    @NotBlank(message = "显示名称不能为空")
    @Size(min = 1, max = 200, message = "显示名称长度必须在1-200之间")
    @Schema(description = "显示名称", example = "拥有")
    private String displayName;

    @Size(max = 1000, message = "描述长度不能超过1000")
    @Schema(description = "描述")
    private String description;

    @NotBlank(message = "基数不能为空")
    @Schema(description = "基数")
    private RelationCardinality cardinality;

    @Schema(description = "反向关系名", example = "OWNED_BY")
    private String reverseName;
    @Schema(description = "反向关系显示名", example = "被拥有")
    private String reverseDisplayName;
    @Schema(description = "关系属性列表")
    private List<RelationPropertyDTO> properties;
}

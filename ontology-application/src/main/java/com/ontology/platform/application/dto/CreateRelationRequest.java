package com.ontology.platform.application.dto;

import com.ontology.platform.common.enums.RelationCardinality;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * 创建关系请求DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRelationRequest {

    @NotBlank(message = "本体ID不能为空")
    private String ontologyId;

    @NotBlank(message = "源对象类型ID不能为空")
    private String sourceTypeId;

    @NotBlank(message = "目标对象类型ID不能为空")
    private String targetTypeId;

    @NotBlank(message = "关系名不能为空")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "关系名必须以大写字母开头，只能包含大写字母、数字和下划线")
    @Size(min = 1, max = 100, message = "关系名长度必须在1-100之间")
    private String name;

    @NotBlank(message = "显示名称不能为空")
    @Size(min = 1, max = 200, message = "显示名称长度必须在1-200之间")
    private String displayName;

    @Size(max = 1000, message = "描述长度不能超过1000")
    private String description;

    @NotBlank(message = "基数不能为空")
    private RelationCardinality cardinality;

    private String reverseName;
    private String reverseDisplayName;
    private List<RelationPropertyDTO> properties;
}

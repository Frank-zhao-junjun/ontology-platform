package com.ontology.platform.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建本体请求")
public class CreateOntologyRequest {

    @NotBlank(message = "本体名称不能为空")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "本体名称必须以小写字母开头，只能包含小写字母、数字和下划线")
    @Size(min = 1, max = 100, message = "本体名称长度必须在1-100之间")
    @Schema(description = "本体名称", example = "sales_ontology")
    private String name;

    @NotBlank(message = "显示名称不能为空")
    @Size(min = 1, max = 200, message = "显示名称长度必须在1-200之间")
    @Schema(description = "显示名称", example = "销售本体")
    private String displayName;

    @Size(max = 1000, message = "描述长度不能超过1000")
    @Schema(description = "描述", example = "销售相关对象类型和关系定义")
    private String description;
}

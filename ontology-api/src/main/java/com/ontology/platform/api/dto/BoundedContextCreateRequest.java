package com.ontology.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "创建限界上下文请求")
public class BoundedContextCreateRequest {

    @NotBlank(message = "名称不能为空")
    @Schema(description = "上下文名称", example = "生产制造")
    private String name;

    @NotBlank(message = "代码不能为空")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "代码只能包含小写字母、数字和下划线，且必须以字母开头")
    @Schema(description = "上下文代码（全局唯一）", example = "manufacturing")
    private String code;

    @Schema(description = "描述")
    private String description;

    @NotBlank(message = "领域标签不能为空")
    @Schema(description = "领域标签", example = "manufacturing",
            allowableValues = {"manufacturing", "quality", "equipment", "supply_chain"})
    private String domainTag;
}

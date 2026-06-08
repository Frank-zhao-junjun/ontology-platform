package com.ontology.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ObjectTypeCreateRequest {
    @NotBlank
    @Schema(description = "对象类型名称", example = "工序")
    private String name;

    @NotBlank
    @Pattern(regexp = "^[a-z][a-z0-9_]*$")
    @Size(min = 1, max = 50)
    @Schema(description = "对象类型代码", example = "Operation")
    private String code;

    @Schema(description = "对象种类: ENTITY(实体)/VALUE_OBJECT(值对象)/INTERFACE_ABSTRACT(接口抽象)", example = "ENTITY", defaultValue = "ENTITY")
    private String objectKind = "ENTITY";

    @Schema(description = "所属聚合根ID（可选）", example = "uuid-of-aggregate-root")
    private String aggregateRootId;

    @Schema(description = "父对象ID，用于继承（可选）", example = "uuid-of-parent-object")
    private String parentObjectId;

    @Schema(description = "描述（可选）", example = "生产工单的工序定义")
    private String description;
}

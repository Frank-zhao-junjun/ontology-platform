package com.ontology.platform.application.dto;

import com.ontology.platform.common.enums.PropertyDataType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "关系属性")
public class RelationPropertyDTO {

    @NotBlank(message = "属性名不能为空")
    @Schema(description = "属性名")
    private String name;

    @Schema(description = "显示名称")
    private String displayName;

    @NotNull(message = "数据类型不能为空")
    @Schema(description = "数据类型")
    private PropertyDataType dataType;

    @Schema(description = "是否必填")
    private boolean isRequired;
    @Schema(description = "默认值")
    private Object defaultValue;
}

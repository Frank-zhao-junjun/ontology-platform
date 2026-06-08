package com.ontology.platform.application.dto;

import com.ontology.platform.common.enums.PropertyDataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 关系属性DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationPropertyDTO {

    @NotBlank(message = "属性名不能为空")
    private String name;

    private String displayName;

    @NotNull(message = "数据类型不能为空")
    private PropertyDataType dataType;

    private boolean isRequired;
    private Object defaultValue;
}

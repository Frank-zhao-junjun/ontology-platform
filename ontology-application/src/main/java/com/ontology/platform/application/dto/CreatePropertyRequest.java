package com.ontology.platform.application.dto;

import com.ontology.platform.common.enums.PropertyDataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建属性请求DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePropertyRequest {

    @NotBlank(message = "对象类型ID不能为空")
    private String objectTypeId;

    @NotBlank(message = "属性名不能为空")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "属性名必须以小写字母开头，只能包含小写字母、数字和下划线")
    @Size(min = 1, max = 100, message = "属性名长度必须在1-100之间")
    private String name;

    @NotBlank(message = "显示名称不能为空")
    @Size(min = 1, max = 200, message = "显示名称长度必须在1-200之间")
    private String displayName;

    @Size(max = 1000, message = "描述长度不能超过1000")
    private String description;

    @NotNull(message = "数据类型不能为空")
    private PropertyDataType dataType;

    private boolean isRequired;
    private boolean isUnique;
    private boolean isSearchable;
    private boolean isSortable;
    private boolean isComputed;
    private Object defaultValue;
    private int sortOrder;
    @Builder.Default
    private List<ConstraintDefinition> constraints = new ArrayList<>();
}

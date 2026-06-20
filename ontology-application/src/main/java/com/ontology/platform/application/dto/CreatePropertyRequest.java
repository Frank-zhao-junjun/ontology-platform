package com.ontology.platform.application.dto;

import com.ontology.platform.common.enums.PropertyDataType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建属性请求")
public class CreatePropertyRequest {

    @NotBlank(message = "对象类型ID不能为空")
    @Schema(description = "所属对象类型ID")
    private String objectTypeId;

    @NotBlank(message = "属性名不能为空")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "属性名必须以小写字母开头，只能包含小写字母、数字和下划线")
    @Size(min = 1, max = 100, message = "属性名长度必须在1-100之间")
    @Schema(description = "属性名", example = "email")
    private String name;

    @NotBlank(message = "显示名称不能为空")
    @Size(min = 1, max = 200, message = "显示名称长度必须在1-200之间")
    @Schema(description = "显示名称", example = "邮箱")
    private String displayName;

    @Size(max = 1000, message = "描述长度不能超过1000")
    @Schema(description = "描述", example = "客户邮箱地址")
    private String description;

    @NotNull(message = "数据类型不能为空")
    @Schema(description = "数据类型")
    private PropertyDataType dataType;

    @Schema(description = "是否必填")
    private boolean isRequired;
    @Schema(description = "是否唯一")
    private boolean isUnique;
    @Schema(description = "是否可搜索")
    private boolean isSearchable;
    @Schema(description = "是否可排序")
    private boolean isSortable;
    @Schema(description = "是否计算字段")
    private boolean isComputed;
    @Schema(description = "默认值")
    private Object defaultValue;
    @Schema(description = "排序号")
    private int sortOrder;
    @Builder.Default
    @Schema(description = "约束列表")
    private List<ConstraintDefinition> constraints = new ArrayList<>();
}

package com.ontology.platform.application.dto;

import com.ontology.platform.common.enums.PropertyDataType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新属性请求")
public class UpdatePropertyRequest {

    @Size(min = 1, max = 200, message = "显示名称长度必须在1-200之间")
    @Schema(description = "显示名称", example = "邮箱")
    private String displayName;

    @Size(max = 1000, message = "描述长度不能超过1000")
    @Schema(description = "描述", example = "客户邮箱地址")
    private String description;

    @Schema(description = "数据类型")
    private PropertyDataType dataType;
    @Schema(description = "是否必填")
    private Boolean isRequired;
    @Schema(description = "是否唯一")
    private Boolean isUnique;
    @Schema(description = "是否可搜索")
    private Boolean isSearchable;
    @Schema(description = "是否可排序")
    private Boolean isSortable;
    @Schema(description = "默认值")
    private Object defaultValue;
    @Schema(description = "排序号")
    private Integer sortOrder;
}

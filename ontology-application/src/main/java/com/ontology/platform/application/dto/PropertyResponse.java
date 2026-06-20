package com.ontology.platform.application.dto;

import com.ontology.platform.common.enums.PropertyDataType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "属性响应")
public class PropertyResponse {

    @Schema(description = "属性ID")
    private String id;
    @Schema(description = "所属对象类型ID")
    private String objectTypeId;
    @Schema(description = "属性名称")
    private String name;
    @Schema(description = "显示名称")
    private String displayName;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "数据类型")
    private PropertyDataType dataType;
    @Schema(description = "是否计算字段")
    private boolean isComputed;
    @Schema(description = "是否必填")
    private boolean isRequired;
    @Schema(description = "是否唯一")
    private boolean isUnique;
    @Schema(description = "是否可搜索")
    private boolean isSearchable;
    @Schema(description = "是否可排序")
    private boolean isSortable;
    @Schema(description = "默认值")
    private Object defaultValue;
    @Schema(description = "排序号")
    private int sortOrder;
    @Schema(description = "创建时间")
    private Instant createdAt;
    @Schema(description = "更新时间")
    private Instant updatedAt;
}

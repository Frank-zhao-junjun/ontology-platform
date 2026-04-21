package com.ontology.platform.application.dto;

import com.ontology.platform.common.enums.PropertyDataType;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Map;

/**
 * 更新属性请求DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePropertyRequest {

    @Size(min = 1, max = 200, message = "显示名称长度必须在1-200之间")
    private String displayName;

    @Size(max = 1000, message = "描述长度不能超过1000")
    private String description;

    private PropertyDataType dataType;
    private Boolean isRequired;
    private Boolean isUnique;
    private Boolean isSearchable;
    private Boolean isSortable;
    private Object defaultValue;
    private Integer sortOrder;
}

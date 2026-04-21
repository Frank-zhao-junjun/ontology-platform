package com.ontology.platform.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

/**
 * 创建对象实例请求DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInstanceRequest {

    @NotBlank(message = "对象类型ID不能为空")
    private String objectTypeId;

    @NotNull(message = "属性不能为空")
    private Map<String, Object> properties;
}

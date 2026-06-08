package com.ontology.platform.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateValueObjectRequest {
    @NotBlank(message = "值对象名称不能为空")
    private String name;
    @NotBlank(message = "值对象编码不能为空")
    private String code;
    private String nameEn;
    private String description;
    private String propertiesJson = "[]";
}

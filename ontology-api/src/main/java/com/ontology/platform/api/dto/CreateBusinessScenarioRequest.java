package com.ontology.platform.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBusinessScenarioRequest {
    @NotBlank(message = "场景名称不能为空")
    private String name;
    @NotBlank(message = "场景编码不能为空")
    private String code;
    private String nameEn;
    private String description;
}

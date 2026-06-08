package com.ontology.platform.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建对象类型请求DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateObjectTypeRequest {

    @NotBlank(message = "本体ID不能为空")
    private String ontologyId;

    @NotBlank(message = "对象类型名称不能为空")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "名称必须以小写字母开头，只能包含小写字母、数字和下划线")
    @Size(min = 1, max = 100, message = "名称长度必须在1-100之间")
    private String name;

    @NotBlank(message = "显示名称不能为空")
    @Size(min = 1, max = 200, message = "显示名称长度必须在1-200之间")
    private String displayName;

    @Size(max = 1000, message = "描述长度不能超过1000")
    private String description;

    @NotBlank(message = "主键属性名不能为空")
    private String primaryKey;

    private String parentId;

    @Builder.Default
    private List<String> interfaceNames = new ArrayList<>();
}

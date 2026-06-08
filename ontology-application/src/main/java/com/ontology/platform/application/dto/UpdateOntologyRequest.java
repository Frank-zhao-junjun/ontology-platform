package com.ontology.platform.application.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 更新本体请求DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOntologyRequest {

    @Size(min = 1, max = 200, message = "显示名称长度必须在1-200之间")
    private String displayName;

    @Size(max = 1000, message = "描述长度不能超过1000")
    private String description;
}

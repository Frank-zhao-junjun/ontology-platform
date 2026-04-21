package com.ontology.platform.application.dto;

import lombok.*;

import java.util.Map;

/**
 * 更新对象实例请求DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInstanceRequest {

    private Map<String, Object> properties;
}

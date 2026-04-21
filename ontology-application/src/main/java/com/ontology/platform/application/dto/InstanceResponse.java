package com.ontology.platform.application.dto;

import lombok.*;

import java.time.Instant;
import java.util.Map;

/**
 * 对象实例响应DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstanceResponse {

    private String id;
    private String ontologyId;
    private String objectTypeId;
    private String objectTypeName;
    private String primaryKeyValue;
    private Map<String, Object> properties;
    private String status;
    private int version;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
}

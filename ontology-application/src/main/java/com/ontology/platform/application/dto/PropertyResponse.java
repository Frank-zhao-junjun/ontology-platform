package com.ontology.platform.application.dto;

import com.ontology.platform.common.enums.PropertyDataType;
import lombok.*;

import java.time.Instant;

/**
 * 属性响应DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyResponse {

    private String id;
    private String objectTypeId;
    private String name;
    private String displayName;
    private String description;
    private PropertyDataType dataType;
    private boolean isComputed;
    private boolean isRequired;
    private boolean isUnique;
    private boolean isSearchable;
    private boolean isSortable;
    private Object defaultValue;
    private int sortOrder;
    private Instant createdAt;
    private Instant updatedAt;
}

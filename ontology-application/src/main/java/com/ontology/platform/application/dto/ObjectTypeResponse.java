package com.ontology.platform.application.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 对象类型响应DTO
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectTypeResponse {

    private String id;
    private String ontologyId;
    private String name;
    private String displayName;
    private String description;
    private String primaryKey;
    private String parentId;

    @Builder.Default
    private List<String> interfaceNames = new ArrayList<>();

    private int instanceCount;
    private Instant createdAt;
    private Instant updatedAt;
}

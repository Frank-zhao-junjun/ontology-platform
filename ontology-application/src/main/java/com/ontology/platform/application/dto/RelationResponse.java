package com.ontology.platform.application.dto;

import com.ontology.platform.common.enums.RelationCardinality;
import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * 关系响应DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationResponse {

    private String id;
    private String ontologyId;
    private String sourceTypeId;
    private String targetTypeId;
    private String name;
    private String displayName;
    private String description;
    private RelationCardinality cardinality;
    private String reverseName;
    private String reverseDisplayName;

    @Builder.Default
    private List<RelationPropertyDTO> properties = new java.util.ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;
}

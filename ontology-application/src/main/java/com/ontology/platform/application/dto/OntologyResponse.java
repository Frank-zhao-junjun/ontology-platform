package com.ontology.platform.application.dto;

import com.ontology.platform.common.enums.OntologyStatus;
import lombok.*;

import java.time.Instant;

/**
 * 本体响应DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OntologyResponse {

    private String id;
    private String name;
    private String displayName;
    private String description;
    private String version;
    private OntologyStatus status;
    private Instant publishedAt;
    private int objectTypeCount;
    private int actionTypeCount;
    private Instant createdAt;
    private Instant updatedAt;
}

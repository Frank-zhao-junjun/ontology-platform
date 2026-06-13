package com.ontology.platform.application.dto.domain;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDefinitionResponse {
    private String id;
    private String name;
    private String displayName;
    private String description;
    private String eventType;
    private String severity;
    private String entityId;
    private String payloadSchema;
    private String source;
    private List<CausalityResponse> causalities;
}

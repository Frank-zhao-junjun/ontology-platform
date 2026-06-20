package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "事件定义响应DTO，包含事件类型、严重级别和负载Schema")
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

package com.ontology.platform.application.dto.domain;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CausalityResponse {
    private String id;
    private String causeEventId;
    private String causeEventName;
    private String effectEventId;
    private String effectEventName;
    private String description;
    private Integer delayMs;
    private String condition;
}

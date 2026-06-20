package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "因果响应DTO，描述事件之间的因果关系")
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

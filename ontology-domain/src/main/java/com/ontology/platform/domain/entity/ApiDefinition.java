package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiDefinition {
    private String id;
    private String ontologyId;
    private String apiName;
    private String description;
    private String url;
    private String httpMethod;
    private String requestSchema;
    private String responseSchema;
    private String authType;
    private Integer rateLimit;
    private Integer timeoutMs;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean deleted;

    public static ApiDefinition create(String ontologyId, String apiName, String url, String httpMethod) {
        return ApiDefinition.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .apiName(apiName)
                .url(url)
                .httpMethod(httpMethod != null ? httpMethod : "GET")
                .requestSchema("{}")
                .responseSchema("{}")
                .authType("NONE")
                .rateLimit(0)
                .timeoutMs(30000)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .deleted(false)
                .build();
    }
}

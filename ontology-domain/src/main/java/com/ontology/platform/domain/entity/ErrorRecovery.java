package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ErrorRecovery {
    private String id;
    private String actionId;
    private String errorPattern;
    private String recoveryStrategy;
    private Integer maxRetries;
    private String fallbackActionId;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public static ErrorRecovery create() {
        return ErrorRecovery.builder().id(UUID.randomUUID().toString())
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}

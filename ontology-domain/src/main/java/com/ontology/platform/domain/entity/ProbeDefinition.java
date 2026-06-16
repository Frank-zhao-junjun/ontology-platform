package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProbeDefinition {
    private String id;
    private String ontologyId;
    private String probeName;
    private String description;
    private String target;
    private String probeType;
    private Integer frequencySec;
    private Integer timeoutMs;
    private String alertCondition;
    private String alertSeverity;
    private Boolean enabled;
    private String config;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean deleted;

    public static ProbeDefinition create(String ontologyId, String probeName, String target) {
        return ProbeDefinition.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .probeName(probeName)
                .target(target)
                .probeType("HTTP")
                .frequencySec(300)
                .timeoutMs(5000)
                .alertSeverity("WARNING")
                .enabled(true)
                .config("{}")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .deleted(false)
                .build();
    }
}

package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDefinition {
    private String id;
    private String ontologyId;
    private String reportName;
    private String description;
    private String reportFormat;
    private String fields;
    private String dataSource;
    private String queryId;
    private String scheduleCron;
    private Boolean enabled;
    private String config;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean deleted;

    public static ReportDefinition create(String ontologyId, String reportName, String reportFormat) {
        return ReportDefinition.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .reportName(reportName)
                .reportFormat(reportFormat != null ? reportFormat : "TABLE")
                .fields("[]")
                .enabled(true)
                .config("{}")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .deleted(false)
                .build();
    }
}

package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorDefinition {
    private String id;
    private String ontologyId;
    private String indicatorName;
    private String description;
    private String formula;
    private String targetValue;
    private String unit;
    private String warningThreshold;
    private String criticalThreshold;
    private String aggregationType;
    private Integer frequencySec;
    private Boolean enabled;
    private String extendedData;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean deleted;

    public static IndicatorDefinition create(String ontologyId, String indicatorName, String formula) {
        return IndicatorDefinition.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .indicatorName(indicatorName)
                .formula(formula)
                .aggregationType("COUNT")
                .frequencySec(3600)
                .enabled(true)
                .extendedData("{}")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .deleted(false)
                .build();
    }
}

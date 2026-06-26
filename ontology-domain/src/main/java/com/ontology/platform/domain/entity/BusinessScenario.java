package com.ontology.platform.domain.entity;

import lombok.*;

import java.time.Instant;

/**
 * 业务场景实体
 * Business Scenario Entity
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessScenario {

    private String id;
    private String ontologyId;
    private String name;
    private String nameEn;
    private String description;
    private String projectId;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * 创建新的业务场景
     */
    public static BusinessScenario create(String id, String ontologyId, String name,
                                          String nameEn, String description, String projectId) {
        Instant now = Instant.now();
        return BusinessScenario.builder()
                .id(id)
                .ontologyId(ontologyId)
                .name(name)
                .nameEn(nameEn)
                .description(description)
                .projectId(projectId)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}

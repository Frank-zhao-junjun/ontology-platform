package com.ontology.platform.domain.entity;

import com.ontology.platform.common.enums.InvocationMode;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
public class OntologyAction {
    private final String id, contextId, manifestCode, name, nameEn, description, aggregateRootId;
    private final InvocationMode invocationMode;
    private final String parametersJson, publishesEventIdsJson, allowedStateFromJson, businessScenarioIdsJson;
    private final String mcpToolName;
    private final Instant createdAt;

    @Builder
    public OntologyAction(String id, String contextId, String manifestCode, String name, String nameEn,
                          String description, String aggregateRootId, InvocationMode invocationMode,
                          String parametersJson, String publishesEventIdsJson, String allowedStateFromJson,
                          String businessScenarioIdsJson, String mcpToolName, Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.contextId = contextId;
        this.manifestCode = manifestCode;
        this.name = name;
        this.nameEn = nameEn;
        this.description = description;
        this.aggregateRootId = aggregateRootId;
        this.invocationMode = invocationMode != null ? invocationMode : InvocationMode.BOTH;
        this.parametersJson = parametersJson != null ? parametersJson : "[]";
        this.publishesEventIdsJson = publishesEventIdsJson != null ? publishesEventIdsJson : "[]";
        this.allowedStateFromJson = allowedStateFromJson != null ? allowedStateFromJson : "[]";
        this.businessScenarioIdsJson = businessScenarioIdsJson != null ? businessScenarioIdsJson : "[]";
        this.mcpToolName = mcpToolName;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static OntologyAction create(String contextId, String manifestCode, String name, String nameEn,
                                        String description, String aggregateRootId, InvocationMode invocationMode,
                                        String parametersJson, String publishesEventIdsJson,
                                        String allowedStateFromJson, String businessScenarioIdsJson,
                                        String mcpToolName) {
        return OntologyAction.builder().contextId(contextId).manifestCode(manifestCode).name(name).nameEn(nameEn)
                .description(description).aggregateRootId(aggregateRootId).invocationMode(invocationMode)
                .parametersJson(parametersJson).publishesEventIdsJson(publishesEventIdsJson)
                .allowedStateFromJson(allowedStateFromJson).businessScenarioIdsJson(businessScenarioIdsJson)
                .mcpToolName(mcpToolName).build();
    }
}

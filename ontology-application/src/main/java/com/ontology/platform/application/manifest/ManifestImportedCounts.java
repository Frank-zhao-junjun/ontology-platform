package com.ontology.platform.application.manifest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManifestImportedCounts {
    private int boundedContext;
    private int businessScenarios;
    private int objectTypes;
    private int properties;
    private int propertyFieldKeys;
    private int relations;
    private int stateMachines;
    private int actions;
    private int rules;
    private int domainEvents;
    private int roles;
    private int fieldPermissions;
    private int agentPolicies;
    private int dataSources;
}

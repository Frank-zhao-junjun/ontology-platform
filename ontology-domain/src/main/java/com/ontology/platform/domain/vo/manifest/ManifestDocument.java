package com.ontology.platform.domain.vo.manifest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManifestDocument {
    private String apiVersion;
    private String kind;
    private Metadata metadata;
    private Spec spec;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        private String id;
        private String version;
        private String name;
        private String displayName;
        private String description;
        private String boundedContext;
        private List<String> domainTags;
        private String compiledAt;
        private String source;
        private String status;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Spec {
        private Semantic semantic;
        private Behavior behavior;
        private Events events;
        private Governance governance;
        private List<DataSource> dataSources;
        private List<Epc> epc;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Semantic {
        private BoundedContext boundedContext;
        private List<BusinessScenario> businessScenarios;
        private List<ValueObject> valueObjects;
        private List<ObjectType> objectTypes;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BoundedContext {
        private String id; private String name; private String nameEn; private String description;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BusinessScenario {
        private String id; private String name; private String nameEn;
        private String description; private List<String> applicableObjectTypeIds;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValueObject {
        private String id; private String name; private String nameEn;
        private List<PropertyDef> properties;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ObjectType {
        private String id; private String name; private String nameEn;
        private String kind; private List<String> businessScenarioIds;
        private String description; private String aggregateRootId;
        private List<PropertyDef> properties;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PropertyDef {
        private String id; private String name; private String nameEn;
        private String dataType; private Boolean required;
        private List<String> enumValues; private String description;
        private Boolean isPrimary; private Boolean isState;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Behavior {
        private List<ActionDef> actions;
        private List<RuleDef> rules;
        private List<StateMachineDef> stateMachines;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActionDef {
        private String id; private String name; private String nameEn;
        private String aggregateRootId; private String description;
        private Map<String, Object> inputSchema;
        private List<String> preRuleIds; private List<String> postRuleIds;
        private List<String> publishesEventIds;
        private String domain; private String riskLevel;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RuleDef {
        private String id; private String name; private String description; private String expression;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StateMachineDef {
        private String id; private String name; private String aggregateRootId;
        private String entityId; private List<StateDef> states; private List<TransitionDef> transitions;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StateDef {
        private String name; private boolean isInitial; private boolean isFinal;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransitionDef {
        private String from; private String to; private String trigger; private String guard;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Events {
        private List<EventDef> domainEvents;
        private List<CausalityDef> causalities;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventDef {
        private String id; private String name; private String nameEn;
        private String eventType; private String severity; private String aggregateRootId;
        private Map<String, Object> payloadSchema;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CausalityDef {
        private String id; private String causeEventId; private String effectEventId; private String description;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Governance {
        private List<RoleDef> roles; private List<AgentPolicyDef> agentPolicies;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RoleDef {
        private String id; private String name; private String code; private List<String> permissions;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AgentPolicyDef {
        private String id; private String name; private String agentRoleId; private List<String> allowedTools;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataSource {
        private String id; private String name; private String code;
        private String sourceType; private Map<String, Object> connectionConfig; private String credentialRef;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Epc {
        private String id; private String flowName; private List<EpcStepDef> steps;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EpcStepDef {
        private Integer stepOrder; private String triggerEventId;
        private String actionId; private List<String> conditions; private List<String> guards;
    }

    // Convenience accessors (null-safe at all levels)
    @JsonIgnore
    public List<ObjectType> getObjectTypes() {
        if (spec == null || spec.semantic == null || spec.semantic.objectTypes == null) return List.of();
        return spec.semantic.objectTypes;
    }
    @JsonIgnore
    public List<ActionDef> getActions() {
        if (spec == null || spec.behavior == null || spec.behavior.actions == null) return List.of();
        return spec.behavior.actions;
    }
    @JsonIgnore
    public List<EventDef> getEvents() {
        if (spec == null || spec.events == null || spec.events.domainEvents == null) return List.of();
        return spec.events.domainEvents;
    }
    @JsonIgnore
    public List<RuleDef> getRules() {
        if (spec == null || spec.behavior == null || spec.behavior.rules == null) return List.of();
        return spec.behavior.rules;
    }
    @JsonIgnore
    public List<StateMachineDef> getStateMachines() {
        if (spec == null || spec.behavior == null || spec.behavior.stateMachines == null) return List.of();
        return spec.behavior.stateMachines;
    }
}

package com.ontology.platform.domain.dto.imports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * YAML OntologyManifest 顶层结构
 * 对应项目1 YAML 导出的 Kubernetes-style manifest 格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YamlManifest {

    /** apiVersion */
    private String apiVersion;
    /** kind: OntologyManifest */
    private String kind;

    private Metadata metadata;
    private Spec spec;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Spec {
        private Semantic semantic;
        private Behavior behavior;
        private Events events;
        private Governance governance;
        private List<DataSource> dataSources;
    }

    // ==================== Spec.Semantic ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Semantic {
        private BoundedContext boundedContext;
        private List<BusinessScenario> businessScenarios;
        private List<ValueObject> valueObjects;
        private List<ObjectTypeDef> objectTypes;
        private List<StateMachineDef> stateMachines;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoundedContext {
        private String id;
        private String name;
        private String nameEn;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessScenario {
        private String id;
        private String name;
        private String nameEn;
        private String description;
        private List<String> applicableObjectTypeIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValueObject {
        private String id;
        private String name;
        private String nameEn;
        @Builder.Default
        private List<PropertyDef> properties = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyDef {
        private String id;
        private String name;
        private String nameEn;
        private String dataType;
        private Boolean required;
        private Boolean sensitive;
        private String valueObjectRef;
        private List<String> enumValues;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObjectTypeDef {
        private String id;
        private String name;
        private String nameEn;
        private String kind;
        private String description;
        private String aggregateRootId;
        private List<String> businessScenarioIds;
        @Builder.Default
        private List<PropertyDef> properties = new ArrayList<>();
        @Builder.Default
        private List<RelationDef> relations = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationDef {
        private String id;
        private String name;
        private String sourceObjectTypeId;
        private String targetObjectTypeId;
        private String cardinality;
        private String relationKind;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StateMachineDef {
        private String id;
        private String name;
        private String objectTypeId;
        private String statusField;
        @Builder.Default
        private List<StateDef> states = new ArrayList<>();
        @Builder.Default
        private List<TransitionDef> transitions = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StateDef {
        private String name;
        private String code;
        private Boolean isInitial;
        private Boolean isFinal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransitionDef {
        private String name;
        private String from;
        private String to;
        private String trigger;
        private String actionId;
    }

    // ==================== Spec.Behavior ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Behavior {
        private List<ActionDef> actions;
        private List<RuleDef> rules;
        private List<MetricDef> metrics;
        private List<TransactionBoundaryDef> transactionBoundaries;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionDef {
        private String id;
        private String name;
        private String nameEn;
        private String aggregateRootId;
        private String description;
        private List<String> businessScenarioIds;
        @Builder.Default
        private List<ParameterDef> parameters = new ArrayList<>();
        private List<String> preRuleIds;
        private List<String> allowedStateFrom;
        private List<String> publishesEventIds;
        private String mcpToolName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParameterDef {
        private String name;
        private String nameEn;
        private String dataType;
        private Boolean required;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleDef {
        private String id;
        private String name;
        private String type;
        private ExpressionDef expression;
        private String errorMessage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpressionDef {
        private String type;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricDef {
        private String id;
        private String name;
        private String formula;
        private String dataSourceRef;
        private String period;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionBoundaryDef {
        private String id;
        private String actionId;
        private String description;
        @Builder.Default
        private List<OperationDef> operations = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationDef {
        private String type;
        private String field;
        private String value;
    }

    // ==================== Spec.Events ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Events {
        private List<DomainEventDef> domainEvents;
        private List<RouteDef> routes;
        private List<HandlerDef> handlers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DomainEventDef {
        private String id;
        private String name;
        private String nameEn;
        private String aggregateRootId;
        private String triggerActionId;
        private PayloadSchemaDef payloadSchema;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayloadSchemaDef {
        private List<String> required;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteDef {
        private String id;
        private String eventId;
        @Builder.Default
        private List<TargetDef> targets = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetDef {
        private String boundedContext;
        private String system;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HandlerDef {
        private String id;
        private String routeId;
        private String targetBoundedContext;
        private String actionId;
        private List<String> businessScenarioIds;
    }

    // ==================== Spec.Governance ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Governance {
        private List<RoleDef> roles;
        private List<FieldPermissionDef> fieldPermissions;
        private List<AgentPolicyDef> agentPolicies;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleDef {
        private String id;
        private String name;
        @Builder.Default
        private List<PermissionDef> permissions = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionDef {
        private String objectTypeId;
        private List<String> ops;
        private List<String> denyActionIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldPermissionDef {
        private String objectTypeId;
        private String propertyNameEn;
        private List<String> allowedRoleIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentPolicyDef {
        private String id;
        private String manifestVersion;
        private String roleId;
        private List<String> allowedMcpTools;
        private List<String> allowedAggregateRootIds;
        private List<String> allowedActionIds;
        private RateLimitDef rateLimit;
        private Boolean defaultDeny;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLimitDef {
        private Integer maxCallsPerSecond;
    }

    // ==================== Spec.DataSources ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSource {
        private String id;
        private String name;
        private String type;
        private String boundObjectTypeId;
        private ApiDef api;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiDef {
        private String baseUrl;
        private String entitySet;
        private String authSecretRef;
    }
}

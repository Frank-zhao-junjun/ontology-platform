package com.ontology.platform.domain.dto.imports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OntologyExchange 顶层信封结构
 * 对应 TypeScript 的 OntologyExchange 接口 (apiVersion: ontology.platform/v2, kind: OntologyExchange)
 * 1:1 映射项目1 TypeScript ontology.ts 定义
 * Phase 3a v2 交换格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OntologyExchangeDocument {

    /** apiVersion: "ontology.platform/v2" */
    private String apiVersion;

    /** kind: "OntologyExchange" */
    private String kind;

    private Metadata metadata;
    private Spec spec;

    // ==================== Metadata ====================

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
        private String source;
        private String status;
        private String projectId;
        private String exportedAt;
        private String exporterVersion;
    }

    // ==================== Spec ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Spec {
        private OntologyProject project;
        private Extensions extensions;
        private LifecycleSpec lifecycle;
    }

    // ==================== OntologyProject ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OntologyProject {
        private String id;
        private String name;
        private String description;
        private Domain domain;
        private DataModel dataModel;
        private BehaviorModel behaviorModel;
        private RuleModel ruleModel;
        private EventModel eventModel;
        private GovernanceModel governanceModel;
        private DataSourcesModel dataSourcesModel;
        private OrganizationModel organizationModel;
        private MetricsModel metricsModel;
        private ProcessModel processModel;
        private AgentSemanticLayer agentSemanticLayer;
        private EpcModel epcModel;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Domain {
        private String id;
        private String name;
        private String nameEn;
        private String description;
        private String icon;
        private String color;
    }

    // ==================== DataModel ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataModel {
        private String id;
        private String name;
        private String version;
        private String domain;
        @Builder.Default
        private List<ProjectRef> projects = new ArrayList<>();
        @Builder.Default
        private List<BusinessScenario> businessScenarios = new ArrayList<>();
        @Builder.Default
        private List<Entity> entities = new ArrayList<>();
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProjectRef {
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
        private String projectId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entity {
        private String id;
        private String name;
        private String nameEn;
        private String projectId;
        private String businessScenarioId;
        private String description;
        private String entityRole; // "aggregate_root" or "child_entity"
        private String parentAggregateId;
        @Builder.Default
        private List<Attribute> attributes = new ArrayList<>();
        @Builder.Default
        private List<Relation> relations = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attribute {
        private String id;
        private String name;
        private String nameEn;
        private String dataType;
        private Boolean required;
        private Boolean unique;
        private String enumRef;
        private String description;
        private Integer length;
        private Integer precision;
        private Integer scale;
        private String referenceKind;
        private String referencedEntityId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Relation {
        private String id;
        private String name;
        private String type; // "one_to_one", "one_to_many", "many_to_many"
        private String targetEntity;
        private String foreignKey;
        private String description;
        private Boolean cascade;
    }

    // ==================== BehaviorModel ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BehaviorModel {
        private String id;
        private String name;
        private String version;
        private String domain;
        @Builder.Default
        private List<StateMachine> stateMachines = new ArrayList<>();
        @Builder.Default
        private List<Action> actions = new ArrayList<>();
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StateMachine {
        private String id;
        private String name;
        private String entity;
        private String statusField;
        @Builder.Default
        private List<State> states = new ArrayList<>();
        @Builder.Default
        private List<Transition> transitions = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class State {
        private String id;
        private String name;
        private String description;
        private Boolean isInitial;
        private Boolean isFinal;
        @Builder.Default
        private List<String> availableActions = new ArrayList<>();
        private String semanticTag;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Transition {
        private String id;
        private String name;
        private String from;
        private String to;
        private String trigger; // "manual", "automatic", "scheduled"
        @Builder.Default
        private List<String> preConditions = new ArrayList<>();
        private String guardCondition;
        private String publishEventId;
        private Boolean requiresApproval;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Action {
        private String id;
        private String name;
        private String nameEn;
        private String description;
        private String targetEntityId;
        private String actionType;
        @Builder.Default
        private List<Parameter> parameters = new ArrayList<>();
        @Builder.Default
        private List<String> preConditions = new ArrayList<>();
        @Builder.Default
        private List<String> triggerPhrases = new ArrayList<>();
        private String executionType;
        private List<String> requiredRoles;
        private String idempotencyKeyTemplate;
        private Boolean requiresConfirmation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameter {
        private String id;
        private String name;
        private String nameEn;
        private String dataType;
        private Boolean required;
    }

    // ==================== RuleModel ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleModel {
        private String id;
        private String name;
        private String version;
        private String domain;
        @Builder.Default
        private List<Rule> rules = new ArrayList<>();
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rule {
        private String id;
        private String name;
        private String type;
        private String entity;
        private String field;
        private RuleCondition condition;
        private String errorMessage;
        private String severity;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleCondition {
        private String type;
        private String expression;
        private List<String> fields;
        private String refEntity;
        private String pattern;
        private String min;
        private String max;
    }

    // ==================== EventModel ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventModel {
        private String id;
        private String name;
        private String version;
        private String domain;
        @Builder.Default
        private List<EventDefinition> events = new ArrayList<>();
        @Builder.Default
        private List<Object> subscriptions = new ArrayList<>();
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventDefinition {
        private String id;
        private String name;
        private String nameEn;
        private String entity;
        private String trigger;
        private Boolean isDomainEvent;
        private String description;
        @Builder.Default
        private List<EventPayloadField> payload = new ArrayList<>();
        @Builder.Default
        private List<String> payloadFields = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventPayloadField {
        private String field;
    }

    // ==================== GovernanceModel ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GovernanceModel {
        private String id;
        @Builder.Default
        private List<GovernanceRole> roles = new ArrayList<>();
        @Builder.Default
        private List<FieldPermission> fieldPermissions = new ArrayList<>();
        @Builder.Default
        private List<AgentPolicy> agentPolicies = new ArrayList<>();
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GovernanceRole {
        private String id;
        private String name;
        @Builder.Default
        private List<Permission> permissions = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Permission {
        private String objectTypeId;
        @Builder.Default
        private List<String> ops = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldPermission {
        private String objectTypeId;
        private String propertyNameEn;
        @Builder.Default
        private List<String> allowedRoleIds = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AgentPolicy {
        private String id;
        private String manifestsVersion;
        private String roleId;
        @Builder.Default
        private List<String> allowedMcpTools = new ArrayList<>();
        @Builder.Default
        private List<String> allowedAggregateRootIds = new ArrayList<>();
        private Boolean defaultDeny;
    }

    // ==================== DataSourcesModel ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSourcesModel {
        @Builder.Default
        private List<DataSource> sources = new ArrayList<>();
    }

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

    // ==================== OrganizationModel ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganizationModel {
        @Builder.Default private List<Department> departments = new ArrayList<>();
        @Builder.Default private List<Position> positions = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Department {
        private String id;
        private String name;
        private String nameEn;
        private String description;
        private String parentDepartmentId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Position {
        private String id;
        private String name;
        private String nameEn;
        private String description;
        private String departmentId;
        @Builder.Default private List<String> responsibilities = new ArrayList<>();
    }

    // ==================== MetricsModel ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricsModel {
        @Builder.Default private List<BusinessMetric> metrics = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessMetric {
        private String id;
        private String name;
        private String nameEn;
        private String description;
        private String formula;
        private String dataSourceRef;
        private String period;
        private String targetEntity;
    }

    // ==================== ProcessModel ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessModel {
        private String id;
        private String name;
        private String version;
        private String domain;
        @Builder.Default private List<Orchestration> orchestrations = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Orchestration {
        private String id;
        private String name;
        private String description;
        @Builder.Default private List<String> entryPoints = new ArrayList<>();
        @Builder.Default private List<ProcessStep> steps = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessStep {
        private String id;
        private String name;
        private String type;
        private String description;
    }

    // ==================== AgentSemanticLayer ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AgentSemanticLayer {
        @Builder.Default private List<Intent> intents = new ArrayList<>();
        @Builder.Default private List<BusinessTerm> businessTerms = new ArrayList<>();
        @Builder.Default private List<SemanticAgentPolicy> agentPolicies = new ArrayList<>();
        @Builder.Default private List<SemanticRelation> semanticRelations = new ArrayList<>();
        @Builder.Default private List<SemanticErrorRecovery> errorRecoveries = new ArrayList<>();
        @Builder.Default private List<SemanticFieldMapping> fieldMappings = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Intent {
        private String id;
        private String name;
        private String description;
        private String category;
        private String targetEntityId;
        @Builder.Default private List<String> triggerPhrases = new ArrayList<>();
        private String actionId;
        private SlotFilling slotFilling;
        private Integer priority;
        private Boolean requiresConfirmation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SlotFilling {
        @Builder.Default private List<IntentSlot> slots = new ArrayList<>();
        @Builder.Default private List<String> requiredSlots = new ArrayList<>();
        @Builder.Default private List<String> fillOrder = new ArrayList<>();
        private Boolean allowBatchFill;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessTerm {
        private String id;
        private String name;
        private String nameEn;
        private String definition;
        @Builder.Default private List<String> synonyms = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IntentSlot {
        private String id;
        private String paramName;
        private String displayName;
        private String name;
        private String prompt;
        @Builder.Default private String slotType = "string";
        private Boolean required;
        private Boolean inferableFromContext;
        @Builder.Default private List<String> examples = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SemanticRelation {
        private String id;
        private String sourceTermId;
        private String targetTermId;
        private String relationType;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SemanticAgentPolicy {
        private String id;
        private String roleId;
        @Builder.Default private List<String> allowedMcpTools = new ArrayList<>();
        @Builder.Default private List<String> allowedAggregateRootIds = new ArrayList<>();
        private Boolean defaultDeny;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SemanticErrorRecovery {
        private String id;
        private String actionId;
        private String errorPattern;
        private String recoveryStrategy;
        private Integer maxRetries;
        private String fallbackActionId;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SemanticFieldMapping {
        private String id;
        private String entityId;
        private String fieldNameEn;
        private String businessTermId;
        private String mappingType;
        private String transformRule;
    }

    // ==================== Lifecycle (Phase 3c) ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LifecycleSpec {
        @Builder.Default
        private Map<String, EntityLifecycleEntry> byEntityId = new LinkedHashMap<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EntityLifecycleEntry {
        private String entityId;
        private String entityNameEn;
        private String statusField;
        private Object stateMachine;
        private Map<String, Object> actionsByState;
        private Map<String, Object> rulesByState;
        private Map<String, Object> eventsByState;
        private Map<String, Object> rolesByState;
        private List<Object> auditTrail;
        private Map<String, Object> stats;
    }

    // ==================== EpcModel (Phase 3d: EPC Chain) ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EpcModel {
        @Builder.Default private List<EpcChain> chains = new ArrayList<>();
        @Builder.Default private List<EpcProfile> profiles = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EpcChain {
        private String id;
        private String name;
        private String aggregateRootId;
        private String description;
        private String chainType;
        @Builder.Default private List<EpcNode> nodes = new ArrayList<>();
        @Builder.Default private List<EpcEdge> edges = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EpcNode {
        private String id;
        private String nodeType;
        private String name;
        private String description;
        private String refType;
        private String refId;
        private Integer sortOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EpcEdge {
        private String id;
        private String sourceNodeId;
        private String targetNodeId;
        private String edgeType;
        private String label;
        private String conditionExpr;
        private Integer sortOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EpcProfile {
        private String id;
        private String chainId;
        private String profileData; // JSON string
        private String profileVersion;
    }

    // ==================== Extensions ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Extensions {
        @Builder.Default private List<MetadataDef> metadataList = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetadataDef {
        private String id;
        private String name;
        private String nameEn;
        private String description;
        private String domain;
        private String type;
    }
}

package com.ontology.platform.infrastructure.imports;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/**
 * Project1 原生 JSON → OntologyExchangeDocument (v2) 转换器。
 *
 * <p>支持两种 Project1 导出格式:</p>
 * <ol>
 *   <li><b>原始格式</b>: {@code {version, project, entities, stateMachines, rules, metrics,
 *       dataSources, businessChain, governance}}</li>
 *   <li><b>v1 Manifest 格式</b>: {@code {apiVersion:"ontology.platform/v1", kind:"OntologyManifest",
 *       metadata, spec:{semantic:{objectTypes,stateMachines}, behavior:{actions,rules},
 *       events, governance, dataSources, epc}}}</li>
 * </ol>
 *
 * <p>输出统一的 v2 {@link OntologyExchangeDocument}，可直接传入
 * {@link com.ontology.platform.application.service.exchange.ExchangeImportService}。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Project1JsonToExchangeConverter {

    private final ObjectMapper objectMapper;

    /**
     * 将 Project1 JSON 字符串转换为 OntologyExchangeDocument。
     */
    public OntologyExchangeDocument convert(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            if (isV1ManifestFormat(root)) {
                return convertFromV1Manifest(root);
            } else {
                return convertFromRawFormat(root);
            }
        } catch (Exception e) {
            log.error("Failed to convert Project1 JSON to exchange document: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检测是否为 v1 Manifest 格式 (有 apiVersion + kind)。
     */
    private boolean isV1ManifestFormat(JsonNode root) {
        return root.has("apiVersion") && root.has("kind") && root.has("spec");
    }

    // ═══════════════════════════════════════════════════════════════
    // 格式 1: 原始格式 {version, project, entities, ...}
    // ═══════════════════════════════════════════════════════════════

    private OntologyExchangeDocument convertFromRawFormat(JsonNode root) {
        // Require at least "project" field — too minimal to convert
        if (!root.has("project") || root.get("project") == null) {
            log.warn("Project1 JSON missing required 'project' field, cannot convert");
            return null;
        }
        String projectId = pathStr(root, "project", "id");
        String projectName = pathStr(root, "project", "name");
        String version = pathStr(root, "version");
        String description = pathStr(root, "project", "description");

        return OntologyExchangeDocument.builder()
                .apiVersion("ontology.platform/v2")
                .kind("OntologyExchange")
                .metadata(Metadata.builder()
                        .id(projectId != null ? projectId : UUID.randomUUID().toString())
                        .version(version != null ? version : "1.0.0")
                        .name(projectName != null ? projectName : "")
                        .displayName(projectName)
                        .description(description)
                        .source("ontology-designer")
                        .status("draft")
                        .exportedAt(Instant.now().toString())
                        .exporterVersion("Project1JsonToExchangeConverter")
                        .build())
                .spec(Spec.builder()
                        .project(buildOntologyProject(root, projectId, projectName, description))
                        .build())
                .build();
    }

    private OntologyProject buildOntologyProject(JsonNode root, String projectId, String projectName,
                                                  String description) {
        return OntologyProject.builder()
                .id(projectId)
                .name(projectName)
                .description(description)
                .domain(Domain.builder()
                        .id(projectId != null ? projectId + "-domain" : null)
                        .name(projectName)
                        .description(description)
                        .build())
                .dataModel(buildDataModel(root))
                .behaviorModel(buildBehaviorModel(root))
                .ruleModel(buildRuleModel(root))
                .eventModel(buildEventModel(root))
                .governanceModel(buildGovernanceModel(root))
                .dataSourcesModel(buildDataSourcesModel(root))
                .metricsModel(buildMetricsModel(root))
                .processModel(buildProcessModel(root))
                .createdAt(Instant.now().toString())
                .updatedAt(Instant.now().toString())
                .build();
    }

    private DataModel buildDataModel(JsonNode root) {
        List<Entity> entities = new ArrayList<>();
        JsonNode entitiesNode = root.get("entities");
        if (entitiesNode != null && entitiesNode.isArray()) {
            for (JsonNode entityNode : entitiesNode) {
                entities.add(buildEntity(entityNode));
            }
        }
        return DataModel.builder()
                .entities(entities)
                .build();
    }

    private Entity buildEntity(JsonNode node) {
        String entityId = pathStr(node, "id");
        String entityName = pathStr(node, "name");
        return Entity.builder()
                .id(entityId)
                .name(entityName)
                .nameEn(pathStr(node, "nameEn"))
                .description(pathStr(node, "description"))
                .entityRole(pathStr(node, "kind") != null ? pathStr(node, "kind") : "entity")
                .projectId(pathStr(node, "projectId"))
                .businessScenarioId(pathStr(node, "businessScenarioId"))
                .parentAggregateId(pathStr(node, "parentAggregateId"))
                .attributes(buildAttributes(node.get("attributes")))
                .relations(buildRelations(node.get("relations"), entityId))
                .build();
    }

    private List<Attribute> buildAttributes(JsonNode attrsNode) {
        List<Attribute> attrs = new ArrayList<>();
        if (attrsNode == null || !attrsNode.isArray()) return attrs;
        for (JsonNode attr : attrsNode) {
            attrs.add(Attribute.builder()
                    .id(pathStr(attr, "id"))
                    .name(pathStr(attr, "name"))
                    .nameEn(pathStr(attr, "nameEn"))
                    .dataType(pathStr(attr, "dataType") != null ? pathStr(attr, "dataType") : pathStr(attr, "type"))
                    .required(attr.has("required") ? attr.get("required").asBoolean() : null)
                    .unique(attr.has("unique") ? attr.get("unique").asBoolean() : null)
                    .description(pathStr(attr, "description"))
                    .length(attr.has("length") ? attr.get("length").asInt() : null)
                    .precision(attr.has("precision") ? attr.get("precision").asInt() : null)
                    .scale(attr.has("scale") ? attr.get("scale").asInt() : null)
                    .referenceKind(pathStr(attr, "referenceKind"))
                    .referencedEntityId(pathStr(attr, "referencedEntityId"))
                    .build());
        }
        return attrs;
    }

    private List<Relation> buildRelations(JsonNode relsNode, String sourceEntityId) {
        List<Relation> rels = new ArrayList<>();
        if (relsNode == null || !relsNode.isArray()) return rels;
        for (JsonNode rel : relsNode) {
            rels.add(Relation.builder()
                    .id(pathStr(rel, "id"))
                    .name(pathStr(rel, "name") != null ? pathStr(rel, "name") : pathStr(rel, "type"))
                    .type(pathStr(rel, "type") != null ? pathStr(rel, "type") : "one_to_many")
                    .targetEntity(pathStr(rel, "target") != null ? pathStr(rel, "target") : pathStr(rel, "targetEntity"))
                    .foreignKey(pathStr(rel, "foreignKey"))
                    .description(pathStr(rel, "description"))
                    .cascade(rel.has("cascade") ? rel.get("cascade").asBoolean() : null)
                    .build());
        }
        return rels;
    }

    // ── BehaviorModel ──

    private BehaviorModel buildBehaviorModel(JsonNode root) {
        return BehaviorModel.builder()
                .stateMachines(buildStateMachines(root))
                .actions(buildActions(root))
                .build();
    }

    private List<StateMachine> buildStateMachines(JsonNode root) {
        List<StateMachine> sms = new ArrayList<>();
        JsonNode smNode = root.get("stateMachines");
        if (smNode == null || !smNode.isArray()) return sms;
        for (JsonNode sm : smNode) {
            sms.add(StateMachine.builder()
                    .id(pathStr(sm, "id"))
                    .name(pathStr(sm, "name"))
                    .entity(pathStr(sm, "objectTypeId") != null ? pathStr(sm, "objectTypeId") : pathStr(sm, "entity"))
                    .states(buildStates(sm.get("states")))
                    .transitions(buildTransitions(sm.get("transitions")))
                    .build());
        }
        return sms;
    }

    private List<State> buildStates(JsonNode statesNode) {
        List<State> states = new ArrayList<>();
        if (statesNode == null || !statesNode.isArray()) return states;
        for (JsonNode s : statesNode) {
            states.add(State.builder()
                    .id(pathStr(s, "id") != null ? pathStr(s, "id") : pathStr(s, "name"))
                    .name(pathStr(s, "name"))
                    .description(pathStr(s, "description"))
                    .isInitial(s.has("isInitial") ? s.get("isInitial").asBoolean() : null)
                    .isFinal(s.has("isFinal") ? s.get("isFinal").asBoolean() : null)
                    .build());
        }
        return states;
    }

    private List<Transition> buildTransitions(JsonNode transNode) {
        List<Transition> transitions = new ArrayList<>();
        if (transNode == null || !transNode.isArray()) return transitions;
        for (JsonNode t : transNode) {
            transitions.add(Transition.builder()
                    .id(pathStr(t, "id") != null ? pathStr(t, "id") : pathStr(t, "trigger"))
                    .from(pathStr(t, "from"))
                    .to(pathStr(t, "to"))
                    .trigger(pathStr(t, "trigger"))
                    .guardCondition(pathStr(t, "guardCondition"))
                    .description(pathStr(t, "description"))
                    .build());
        }
        return transitions;
    }

    private List<Action> buildActions(JsonNode root) {
        List<Action> actions = new ArrayList<>();
        JsonNode actionsNode = root.get("actions");
        if (actionsNode == null || !actionsNode.isArray()) return actions;
        for (JsonNode a : actionsNode) {
            actions.add(Action.builder()
                    .id(pathStr(a, "id"))
                    .name(pathStr(a, "name"))
                    .nameEn(pathStr(a, "nameEn"))
                    .description(pathStr(a, "description"))
                    .targetEntityId(pathStr(a, "targetEntityId"))
                    .actionType(pathStr(a, "actionType"))
                    .executionType(pathStr(a, "executionType"))
                    .requiresConfirmation(a.has("requiresConfirmation") ? a.get("requiresConfirmation").asBoolean() : null)
                    .build());
        }
        return actions;
    }

    private List<Parameter> buildActionParameters(JsonNode paramsNode) {
        List<Parameter> params = new ArrayList<>();
        if (paramsNode == null || !paramsNode.isArray()) return params;
        for (JsonNode p : paramsNode) {
            params.add(Parameter.builder()
                    .id(pathStr(p, "id"))
                    .name(pathStr(p, "name"))
                    .dataType(pathStr(p, "dataType") != null ? pathStr(p, "dataType") : pathStr(p, "type"))
                    .required(p.has("required") ? p.get("required").asBoolean() : null)
                    .build());
        }
        return params;
    }

    // ── RuleModel ──

    private RuleModel buildRuleModel(JsonNode root) {
        List<Rule> rules = new ArrayList<>();
        JsonNode rulesNode = root.get("rules");
        if (rulesNode == null || !rulesNode.isArray()) return RuleModel.builder().rules(rules).build();
        for (JsonNode r : rulesNode) {
            rules.add(Rule.builder()
                    .id(pathStr(r, "id"))
                    .name(pathStr(r, "name"))
                    .type(pathStr(r, "type"))
                    .entity(pathStr(r, "entity"))
                    .field(pathStr(r, "field"))
                    .errorMessage(pathStr(r, "errorMessage"))
                    .severity(pathStr(r, "severity"))
                    .description(pathStr(r, "description"))
                    .condition(buildRuleCondition(r.get("condition")))
                    .build());
        }
        return RuleModel.builder().rules(rules).build();
    }

    private RuleCondition buildRuleCondition(JsonNode condNode) {
        if (condNode == null) return null;
        return RuleCondition.builder()
                .type(pathStr(condNode, "type"))
                .expression(pathStr(condNode, "expression"))
                .pattern(pathStr(condNode, "pattern"))
                .min(pathStr(condNode, "min"))
                .max(pathStr(condNode, "max"))
                .refEntity(pathStr(condNode, "refEntity"))
                .build();
    }

    // ── EventModel ──

    private EventModel buildEventModel(JsonNode root) {
        List<EventDefinition> events = new ArrayList<>();
        JsonNode eventsNode = root.get("events");
        if (eventsNode == null || !eventsNode.isArray()) return EventModel.builder().events(events).build();
        for (JsonNode evt : eventsNode) {
            events.add(EventDefinition.builder()
                    .id(pathStr(evt, "id"))
                    .name(pathStr(evt, "name"))
                    .nameEn(pathStr(evt, "nameEn"))
                    .entity(pathStr(evt, "entity"))
                    .trigger(pathStr(evt, "trigger"))
                    .description(pathStr(evt, "description"))
                    .build());
        }
        return EventModel.builder().events(events).build();
    }

    // ── GovernanceModel ──

    private GovernanceModel buildGovernanceModel(JsonNode root) {
        JsonNode govNode = root.get("governance");
        if (govNode == null) return GovernanceModel.builder().build();

        List<GovernanceRole> roles = new ArrayList<>();
        JsonNode rolesNode = govNode.get("roles");
        if (rolesNode != null && rolesNode.isArray()) {
            for (JsonNode role : rolesNode) {
                roles.add(GovernanceRole.builder()
                        .id(pathStr(role, "id"))
                        .name(pathStr(role, "name"))
                        .build());
            }
        }
        return GovernanceModel.builder().roles(roles).build();
    }

    // ── DataSourcesModel ──

    private DataSourcesModel buildDataSourcesModel(JsonNode root) {
        List<DataSource> sources = new ArrayList<>();
        JsonNode dsNode = root.get("dataSources");
        if (dsNode == null || !dsNode.isArray()) return DataSourcesModel.builder().sources(sources).build();
        for (JsonNode ds : dsNode) {
            sources.add(DataSource.builder()
                    .id(pathStr(ds, "id"))
                    .name(pathStr(ds, "name"))
                    .type(pathStr(ds, "type"))
                    .boundObjectTypeId(pathStr(ds, "boundObjectTypeId"))
                    .build());
        }
        return DataSourcesModel.builder().sources(sources).build();
    }

    // ── MetricsModel ──

    private MetricsModel buildMetricsModel(JsonNode root) {
        List<BusinessMetric> metrics = new ArrayList<>();
        JsonNode mNode = root.get("metrics");
        if (mNode == null || !mNode.isArray()) return MetricsModel.builder().metrics(metrics).build();
        for (JsonNode m : mNode) {
            metrics.add(BusinessMetric.builder()
                    .id(pathStr(m, "id"))
                    .name(pathStr(m, "name"))
                    .nameEn(pathStr(m, "nameEn"))
                    .description(pathStr(m, "description"))
                    .formula(pathStr(m, "formula"))
                    .dataSourceRef(pathStr(m, "dataSourceRef"))
                    .period(pathStr(m, "period"))
                    .targetEntity(pathStr(m, "targetEntity"))
                    .build());
        }
        return MetricsModel.builder().metrics(metrics).build();
    }

    // ── ProcessModel (from businessChain) ──

    private ProcessModel buildProcessModel(JsonNode root) {
        JsonNode bcNode = root.get("businessChain");
        if (bcNode == null) return ProcessModel.builder().build();

        List<Orchestration> orchestrations = new ArrayList<>();
        // businessChain may have valueDomains, capabilities, scenarios, epcProcesses
        JsonNode scenariosNode = bcNode.get("scenarios");
        if (scenariosNode != null && scenariosNode.isArray()) {
            for (JsonNode scenario : scenariosNode) {
                List<ProcessStep> steps = new ArrayList<>();
                JsonNode stepsNode = scenario.get("steps");
                if (stepsNode != null && stepsNode.isArray()) {
                    for (int i = 0; i < stepsNode.size(); i++) {
                        JsonNode step = stepsNode.get(i);
                        steps.add(ProcessStep.builder()
                                .id(pathStr(step, "id") != null ? pathStr(step, "id") : "step-" + i)
                                .name(pathStr(step, "name"))
                                .type(pathStr(step, "type"))
                                .description(pathStr(step, "description"))
                                .build());
                    }
                }
                orchestrations.add(Orchestration.builder()
                        .id(pathStr(scenario, "id"))
                        .name(pathStr(scenario, "name"))
                        .description(pathStr(scenario, "description"))
                        .steps(steps)
                        .build());
            }
        }
        return ProcessModel.builder().orchestrations(orchestrations).build();
    }

    // ═══════════════════════════════════════════════════════════════
    // 格式 2: v1 Manifest 格式 {apiVersion, kind, spec:{semantic, ...}}
    // ═══════════════════════════════════════════════════════════════

    private OntologyExchangeDocument convertFromV1Manifest(JsonNode root) {
        JsonNode metadata = root.get("metadata");
        JsonNode spec = root.get("spec");

        String metadataId = pathStr(metadata, "id");
        String metadataVersion = pathStr(metadata, "version");
        String metadataName = pathStr(metadata, "name");
        String displayName = pathStr(metadata, "displayName") != null
                ? pathStr(metadata, "displayName") : metadataName;
        String description = pathStr(metadata, "description");
        String source = pathStr(metadata, "source") != null ? pathStr(metadata, "source") : "ontology-designer";

        return OntologyExchangeDocument.builder()
                .apiVersion("ontology.platform/v2")
                .kind("OntologyExchange")
                .metadata(Metadata.builder()
                        .id(metadataId != null ? metadataId : UUID.randomUUID().toString())
                        .version(metadataVersion != null ? metadataVersion : "1.0.0")
                        .name(metadataName != null ? metadataName : "")
                        .displayName(displayName)
                        .description(description)
                        .source(source)
                        .status(pathStr(metadata, "status") != null ? pathStr(metadata, "status") : "draft")
                        .exportedAt(pathStr(metadata, "compiledAt"))
                        .exporterVersion("Project1JsonToExchangeConverter")
                        .build())
                .spec(Spec.builder()
                        .project(buildProjectFromV1Spec(spec, metadataId, metadataName))
                        .build())
                .build();
    }

    private OntologyProject buildProjectFromV1Spec(JsonNode spec, String projectId, String projectName) {
        JsonNode semantic = spec != null ? spec.get("semantic") : null;
        JsonNode behavior = spec != null ? spec.get("behavior") : null;

        return OntologyProject.builder()
                .id(projectId)
                .name(projectName)
                .domain(Domain.builder().id(projectId != null ? projectId + "-domain" : null)
                        .name(projectName).build())
                .dataModel(buildDataModelFromV1Semantic(semantic))
                .behaviorModel(buildBehaviorModelFromV1(semantic, behavior))
                .ruleModel(buildRuleModelFromV1(behavior))
                .eventModel(buildEventModelFromV1(spec))
                .governanceModel(buildGovernanceModelFromV1(spec))
                .dataSourcesModel(buildDataSourcesModelFromV1(spec))
                .build();
    }

    private DataModel buildDataModelFromV1Semantic(JsonNode semantic) {
        if (semantic == null) return DataModel.builder().build();
        List<Entity> entities = new ArrayList<>();
        JsonNode objectTypes = semantic.get("objectTypes");
        if (objectTypes != null && objectTypes.isArray()) {
            for (JsonNode ot : objectTypes) {
                entities.add(buildEntity(ot)); // reuses raw format's buildEntity
            }
        }
        return DataModel.builder().entities(entities).build();
    }

    private BehaviorModel buildBehaviorModelFromV1(JsonNode semantic, JsonNode behavior) {
        List<StateMachine> sms = new ArrayList<>();
        // v1 format puts stateMachines in both semantic and behavior
        List<Action> actions = new ArrayList<>();

        if (semantic != null) {
            JsonNode smNode = semantic.get("stateMachines");
            if (smNode != null && smNode.isArray()) {
                for (JsonNode sm : smNode) sms.add(buildStateMachineFromV1(sm));
            }
        }
        if (behavior != null) {
            JsonNode smNode2 = behavior.get("stateMachines");
            if (smNode2 != null && smNode2.isArray()) {
                for (JsonNode sm : smNode2) sms.add(buildStateMachineFromV1(sm));
            }
            JsonNode actionsNode = behavior.get("actions");
            if (actionsNode != null && actionsNode.isArray()) {
                for (JsonNode a : actionsNode) actions.add(buildActionFromV1(a));
            }
        }
        return BehaviorModel.builder().stateMachines(sms).actions(actions).build();
    }

    private StateMachine buildStateMachineFromV1(JsonNode sm) {
        return StateMachine.builder()
                .id(pathStr(sm, "id"))
                .name(pathStr(sm, "name"))
                .entity(pathStr(sm, "objectTypeId") != null ? pathStr(sm, "objectTypeId") : pathStr(sm, "entityId"))
                .statusField(pathStr(sm, "statusField"))
                .states(buildStates(sm.get("states")))
                .transitions(buildTransitions(sm.get("transitions")))
                .build();
    }

    private Action buildActionFromV1(JsonNode a) {
        return Action.builder()
                .id(pathStr(a, "id"))
                .name(pathStr(a, "name"))
                .nameEn(pathStr(a, "nameEn"))
                .description(pathStr(a, "description"))
                .targetEntityId(pathStr(a, "targetEntityId"))
                .actionType(pathStr(a, "actionType"))
                .executionType(pathStr(a, "executionType"))
                .requiresConfirmation(a.has("requiresConfirmation") ? a.get("requiresConfirmation").asBoolean() : null)
                .parameters(buildActionParameters(a.get("parameters")))
                .build();
    }

    private RuleModel buildRuleModelFromV1(JsonNode behavior) {
        List<Rule> rules = new ArrayList<>();
        if (behavior == null) return RuleModel.builder().rules(rules).build();
        JsonNode rulesNode = behavior.get("rules");
        if (rulesNode != null && rulesNode.isArray()) {
            for (JsonNode r : rulesNode) {
                rules.add(Rule.builder()
                        .id(pathStr(r, "id")).name(pathStr(r, "name"))
                        .type(pathStr(r, "type")).entity(pathStr(r, "entity"))
                        .field(pathStr(r, "field")).errorMessage(pathStr(r, "errorMessage"))
                        .severity(pathStr(r, "severity")).description(pathStr(r, "description"))
                        .condition(buildRuleCondition(r.get("condition")))
                        .build());
            }
        }
        return RuleModel.builder().rules(rules).build();
    }

    private EventModel buildEventModelFromV1(JsonNode spec) {
        if (spec == null) return EventModel.builder().build();
        JsonNode eventsNode = spec.get("events");
        if (eventsNode == null || !eventsNode.isArray()) return EventModel.builder().build();

        List<EventDefinition> events = new ArrayList<>();
        for (JsonNode evt : eventsNode) {
            events.add(EventDefinition.builder()
                    .id(pathStr(evt, "id")).name(pathStr(evt, "name"))
                    .entity(pathStr(evt, "entity")).trigger(pathStr(evt, "trigger"))
                    .description(pathStr(evt, "description")).build());
        }
        return EventModel.builder().events(events).build();
    }

    private GovernanceModel buildGovernanceModelFromV1(JsonNode spec) {
        if (spec == null) return GovernanceModel.builder().build();
        JsonNode govNode = spec.get("governance");
        if (govNode == null) return GovernanceModel.builder().build();

        List<GovernanceRole> roles = new ArrayList<>();
        JsonNode rolesNode = govNode.get("roles");
        if (rolesNode != null && rolesNode.isArray()) {
            for (JsonNode role : rolesNode) {
                roles.add(GovernanceRole.builder()
                        .id(pathStr(role, "id")).name(pathStr(role, "name")).build());
            }
        }
        return GovernanceModel.builder().roles(roles).build();
    }

    private DataSourcesModel buildDataSourcesModelFromV1(JsonNode spec) {
        if (spec == null) return DataSourcesModel.builder().build();
        JsonNode dsNode = spec.get("dataSources");
        if (dsNode == null || !dsNode.isArray()) return DataSourcesModel.builder().build();

        List<DataSource> sources = new ArrayList<>();
        for (JsonNode ds : dsNode) {
            sources.add(DataSource.builder()
                    .id(pathStr(ds, "id")).name(pathStr(ds, "name"))
                    .type(pathStr(ds, "type") != null ? pathStr(ds, "type") : pathStr(ds, "sourceType"))
                    .build());
        }
        return DataSourcesModel.builder().sources(sources).build();
    }

    // ═══════════════════════════════════════════════════════════════
    // 通用工具方法
    // ═══════════════════════════════════════════════════════════════

    static String pathStr(JsonNode parent, String... path) {
        JsonNode current = parent;
        for (String segment : path) {
            if (current == null || current.isMissingNode()) return null;
            current = current.get(segment);
        }
        if (current == null || current.isNull()) return null;
        String text = current.asText();
        return (text != null && !text.isBlank()) ? text : null;
    }

    static List<JsonNode> nonNullArray(JsonNode node) {
        if (node == null || !node.isArray()) return Collections.emptyList();
        List<JsonNode> result = new ArrayList<>();
        for (JsonNode item : node) {
            if (item != null && !item.isNull()) result.add(item);
        }
        return result;
    }
}

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
                .organizationModel(buildOrganizationModel(root))
                .agentSemanticLayer(buildAgentSemanticLayer(root))
                .epcModel(buildEpcModel(root))
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
        // Normalize entityRole: "entity" → "aggregate_root" for Project1 compatibility
        String rawRole = pathStr(node, "entityRole");
        if (rawRole == null) {
            rawRole = pathStr(node, "kind");
        }
        String normalizedRole = normalizeEntityRole(rawRole);
        return Entity.builder()
                .id(entityId)
                .name(entityName)
                .nameEn(pathStr(node, "nameEn"))
                .description(pathStr(node, "description"))
                .entityRole(normalizedRole)
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

    // ── OrganizationModel (raw format) ──

    private OrganizationModel buildOrganizationModel(JsonNode root) {
        JsonNode orgNode = root.get("organization");
        if (orgNode == null) return OrganizationModel.builder().build();

        List<Department> departments = new ArrayList<>();
        JsonNode deptsNode = orgNode.get("departments");
        if (deptsNode != null && deptsNode.isArray()) {
            for (JsonNode d : deptsNode) {
                departments.add(Department.builder()
                        .id(pathStr(d, "id"))
                        .name(pathStr(d, "name"))
                        .nameEn(pathStr(d, "nameEn"))
                        .description(pathStr(d, "description"))
                        .parentDepartmentId(pathStr(d, "parentDepartmentId"))
                        .build());
            }
        }

        List<Position> positions = new ArrayList<>();
        JsonNode posNode = orgNode.get("positions");
        if (posNode != null && posNode.isArray()) {
            for (JsonNode p : posNode) {
                List<String> responsibilities = new ArrayList<>();
                JsonNode respNode = p.get("responsibilities");
                if (respNode != null && respNode.isArray()) {
                    for (JsonNode r : respNode) responsibilities.add(r.asText());
                }
                positions.add(Position.builder()
                        .id(pathStr(p, "id"))
                        .name(pathStr(p, "name"))
                        .nameEn(pathStr(p, "nameEn"))
                        .description(pathStr(p, "description"))
                        .departmentId(pathStr(p, "departmentId"))
                        .responsibilities(responsibilities)
                        .build());
            }
        }

        return OrganizationModel.builder()
                .departments(departments)
                .positions(positions)
                .build();
    }

    // ── AgentSemanticLayer (raw format) ──

    private AgentSemanticLayer buildAgentSemanticLayer(JsonNode root) {
        JsonNode aslNode = root.get("agentSemanticLayer");
        if (aslNode == null) return AgentSemanticLayer.builder().build();

        List<Intent> intents = new ArrayList<>();
        JsonNode intentsNode = aslNode.get("intents");
        if (intentsNode != null && intentsNode.isArray()) {
            for (JsonNode intent : intentsNode) {
                intents.add(Intent.builder()
                        .id(pathStr(intent, "id"))
                        .name(pathStr(intent, "name"))
                        .description(pathStr(intent, "description"))
                        .category(pathStr(intent, "category"))
                        .targetEntityId(pathStr(intent, "targetEntityId"))
                        .actionId(pathStr(intent, "actionId"))
                        .priority(intent.has("priority") ? intent.get("priority").asInt() : null)
                        .requiresConfirmation(intent.has("requiresConfirmation") ? intent.get("requiresConfirmation").asBoolean() : null)
                        .build());
            }
        }

        List<BusinessTerm> businessTerms = new ArrayList<>();
        JsonNode termsNode = aslNode.get("businessTerms");
        if (termsNode != null && termsNode.isArray()) {
            for (JsonNode t : termsNode) {
                businessTerms.add(BusinessTerm.builder()
                        .id(pathStr(t, "id"))
                        .name(pathStr(t, "name"))
                        .nameEn(pathStr(t, "nameEn"))
                        .definition(pathStr(t, "definition"))
                        .build());
            }
        }

        List<SemanticRelation> semanticRelations = new ArrayList<>();
        JsonNode relsNode = aslNode.get("semanticRelations");
        if (relsNode != null && relsNode.isArray()) {
            for (JsonNode r : relsNode) {
                semanticRelations.add(SemanticRelation.builder()
                        .id(pathStr(r, "id"))
                        .sourceTermId(pathStr(r, "sourceTermId"))
                        .targetTermId(pathStr(r, "targetTermId"))
                        .relationType(pathStr(r, "relationType"))
                        .description(pathStr(r, "description"))
                        .build());
            }
        }

        List<SemanticAgentPolicy> agentPolicies = new ArrayList<>();
        JsonNode policiesNode = aslNode.get("agentPolicies");
        if (policiesNode != null && policiesNode.isArray()) {
            for (JsonNode policy : policiesNode) {
                agentPolicies.add(SemanticAgentPolicy.builder()
                        .id(pathStr(policy, "id"))
                        .roleId(pathStr(policy, "roleId"))
                        .defaultDeny(policy.has("defaultDeny") ? policy.get("defaultDeny").asBoolean() : null)
                        .build());
            }
        }

        List<SemanticErrorRecovery> errorRecoveries = new ArrayList<>();
        JsonNode errNode = aslNode.get("errorRecoveries");
        if (errNode != null && errNode.isArray()) {
            for (JsonNode er : errNode) {
                errorRecoveries.add(SemanticErrorRecovery.builder()
                        .id(pathStr(er, "id"))
                        .actionId(pathStr(er, "actionId"))
                        .errorPattern(pathStr(er, "errorPattern"))
                        .recoveryStrategy(pathStr(er, "recoveryStrategy"))
                        .maxRetries(er.has("maxRetries") ? er.get("maxRetries").asInt() : null)
                        .fallbackActionId(pathStr(er, "fallbackActionId"))
                        .description(pathStr(er, "description"))
                        .build());
            }
        }

        List<SemanticFieldMapping> fieldMappings = new ArrayList<>();
        JsonNode fmNode = aslNode.get("fieldMappings");
        if (fmNode != null && fmNode.isArray()) {
            for (JsonNode fm : fmNode) {
                fieldMappings.add(SemanticFieldMapping.builder()
                        .id(pathStr(fm, "id"))
                        .entityId(pathStr(fm, "entityId"))
                        .fieldNameEn(pathStr(fm, "fieldNameEn"))
                        .businessTermId(pathStr(fm, "businessTermId"))
                        .mappingType(pathStr(fm, "mappingType"))
                        .transformRule(pathStr(fm, "transformRule"))
                        .build());
            }
        }

        return AgentSemanticLayer.builder()
                .intents(intents)
                .businessTerms(businessTerms)
                .semanticRelations(semanticRelations)
                .agentPolicies(agentPolicies)
                .errorRecoveries(errorRecoveries)
                .fieldMappings(fieldMappings)
                .build();
    }

    // ── EpcModel (raw format) ──

    private EpcModel buildEpcModel(JsonNode root) {
        JsonNode epcNode = root.get("epc");
        if (epcNode == null) return EpcModel.builder().build();

        List<EpcChain> chains = new ArrayList<>();
        JsonNode chainsNode = epcNode.get("chains");
        if (chainsNode != null && chainsNode.isArray()) {
            for (JsonNode chain : chainsNode) {
                List<EpcNode> nodes = new ArrayList<>();
                JsonNode nodesNode = chain.get("nodes");
                if (nodesNode != null && nodesNode.isArray()) {
                    for (JsonNode n : nodesNode) {
                        nodes.add(EpcNode.builder()
                                .id(pathStr(n, "id"))
                                .nodeType(pathStr(n, "nodeType"))
                                .name(pathStr(n, "name"))
                                .description(pathStr(n, "description"))
                                .refType(pathStr(n, "refType"))
                                .refId(pathStr(n, "refId"))
                                .sortOrder(n.has("sortOrder") ? n.get("sortOrder").asInt() : null)
                                .build());
                    }
                }
                List<EpcEdge> edges = new ArrayList<>();
                JsonNode edgesNode = chain.get("edges");
                if (edgesNode != null && edgesNode.isArray()) {
                    for (JsonNode e : edgesNode) {
                        edges.add(EpcEdge.builder()
                                .id(pathStr(e, "id"))
                                .sourceNodeId(pathStr(e, "sourceNodeId"))
                                .targetNodeId(pathStr(e, "targetNodeId"))
                                .edgeType(pathStr(e, "edgeType"))
                                .label(pathStr(e, "label"))
                                .conditionExpr(pathStr(e, "conditionExpr"))
                                .sortOrder(e.has("sortOrder") ? e.get("sortOrder").asInt() : null)
                                .build());
                    }
                }
                chains.add(EpcChain.builder()
                        .id(pathStr(chain, "id"))
                        .name(pathStr(chain, "name"))
                        .aggregateRootId(pathStr(chain, "aggregateRootId"))
                        .description(pathStr(chain, "description"))
                        .chainType(pathStr(chain, "chainType"))
                        .nodes(nodes)
                        .edges(edges)
                        .build());
            }
        }

        List<EpcProfile> profiles = new ArrayList<>();
        JsonNode profilesNode = epcNode.get("profiles");
        if (profilesNode != null && profilesNode.isArray()) {
            for (JsonNode p : profilesNode) {
                profiles.add(EpcProfile.builder()
                        .id(pathStr(p, "id"))
                        .chainId(pathStr(p, "chainId"))
                        .profileVersion(pathStr(p, "profileVersion"))
                        .build());
            }
        }

        return EpcModel.builder()
                .chains(chains)
                .profiles(profiles)
                .build();
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
                .metricsModel(buildMetricsModelFromV1(spec))
                .processModel(buildProcessModelFromV1(spec))
                .organizationModel(buildOrganizationModelFromV1(spec))
                .agentSemanticLayer(buildAgentSemanticLayerFromV1(spec))
                .epcModel(buildEpcModelFromV1(spec))
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

    private MetricsModel buildMetricsModelFromV1(JsonNode spec) {
        if (spec == null) return MetricsModel.builder().build();
        JsonNode metricsNode = spec.get("metrics");
        if (metricsNode == null || !metricsNode.isArray()) return MetricsModel.builder().build();

        List<BusinessMetric> metrics = new ArrayList<>();
        for (JsonNode m : metricsNode) {
            metrics.add(BusinessMetric.builder()
                    .id(pathStr(m, "id")).name(pathStr(m, "name"))
                    .nameEn(pathStr(m, "nameEn")).description(pathStr(m, "description"))
                    .formula(pathStr(m, "formula")).dataSourceRef(pathStr(m, "dataSourceRef"))
                    .period(pathStr(m, "period")).targetEntity(pathStr(m, "targetEntity"))
                    .build());
        }
        return MetricsModel.builder().metrics(metrics).build();
    }

    private ProcessModel buildProcessModelFromV1(JsonNode spec) {
        if (spec == null) return ProcessModel.builder().build();
        JsonNode processNode = spec.get("process");
        if (processNode == null) return ProcessModel.builder().build();

        List<Orchestration> orchestrations = new ArrayList<>();
        JsonNode orchNode = processNode.get("orchestrations");
        if (orchNode != null && orchNode.isArray()) {
            for (JsonNode o : orchNode) {
                List<ProcessStep> steps = new ArrayList<>();
                JsonNode stepsNode = o.get("steps");
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
                        .id(pathStr(o, "id")).name(pathStr(o, "name"))
                        .description(pathStr(o, "description")).steps(steps)
                        .build());
            }
        }
        return ProcessModel.builder()
                .id(pathStr(processNode, "id")).name(pathStr(processNode, "name"))
                .version(pathStr(processNode, "version")).domain(pathStr(processNode, "domain"))
                .orchestrations(orchestrations)
                .build();
    }

    private OrganizationModel buildOrganizationModelFromV1(JsonNode spec) {
        if (spec == null) return OrganizationModel.builder().build();
        JsonNode orgNode = spec.get("organization");
        if (orgNode == null) return OrganizationModel.builder().build();

        // Reuse raw format converter
        return buildOrganizationModelFromOrgNode(orgNode);
    }

    private OrganizationModel buildOrganizationModelFromOrgNode(JsonNode orgNode) {
        List<Department> departments = new ArrayList<>();
        JsonNode deptsNode = orgNode.get("departments");
        if (deptsNode != null && deptsNode.isArray()) {
            for (JsonNode d : deptsNode) {
                departments.add(Department.builder()
                        .id(pathStr(d, "id")).name(pathStr(d, "name"))
                        .nameEn(pathStr(d, "nameEn")).description(pathStr(d, "description"))
                        .parentDepartmentId(pathStr(d, "parentDepartmentId")).build());
            }
        }
        List<Position> positions = new ArrayList<>();
        JsonNode posNode = orgNode.get("positions");
        if (posNode != null && posNode.isArray()) {
            for (JsonNode p : posNode) {
                List<String> responsibilities = new ArrayList<>();
                JsonNode respNode = p.get("responsibilities");
                if (respNode != null && respNode.isArray()) {
                    for (JsonNode r : respNode) responsibilities.add(r.asText());
                }
                positions.add(Position.builder()
                        .id(pathStr(p, "id")).name(pathStr(p, "name"))
                        .nameEn(pathStr(p, "nameEn")).description(pathStr(p, "description"))
                        .departmentId(pathStr(p, "departmentId"))
                        .responsibilities(responsibilities).build());
            }
        }
        return OrganizationModel.builder().departments(departments).positions(positions).build();
    }

    private AgentSemanticLayer buildAgentSemanticLayerFromV1(JsonNode spec) {
        if (spec == null) return AgentSemanticLayer.builder().build();
        JsonNode aslNode = spec.get("agentSemanticLayer");
        if (aslNode == null) return AgentSemanticLayer.builder().build();

        // We already have buildAgentSemanticLayer for raw, but that reads from root
        // For v1, the node is directly spec.agentSemanticLayer — convert inline
        List<Intent> intents = new ArrayList<>();
        JsonNode intentsNode = aslNode.get("intents");
        if (intentsNode != null && intentsNode.isArray()) {
            for (JsonNode intent : intentsNode) {
                intents.add(Intent.builder()
                        .id(pathStr(intent, "id")).name(pathStr(intent, "name"))
                        .description(pathStr(intent, "description")).category(pathStr(intent, "category"))
                        .targetEntityId(pathStr(intent, "targetEntityId")).actionId(pathStr(intent, "actionId"))
                        .priority(intent.has("priority") ? intent.get("priority").asInt() : null)
                        .requiresConfirmation(intent.has("requiresConfirmation") ? intent.get("requiresConfirmation").asBoolean() : null)
                        .build());
            }
        }
        List<BusinessTerm> businessTerms = new ArrayList<>();
        JsonNode termsNode = aslNode.get("businessTerms");
        if (termsNode != null && termsNode.isArray()) {
            for (JsonNode t : termsNode) {
                businessTerms.add(BusinessTerm.builder()
                        .id(pathStr(t, "id")).name(pathStr(t, "name"))
                        .nameEn(pathStr(t, "nameEn")).definition(pathStr(t, "definition")).build());
            }
        }
        List<SemanticRelation> semanticRelations = new ArrayList<>();
        JsonNode relsNode = aslNode.get("semanticRelations");
        if (relsNode != null && relsNode.isArray()) {
            for (JsonNode r : relsNode) {
                semanticRelations.add(SemanticRelation.builder()
                        .id(pathStr(r, "id")).sourceTermId(pathStr(r, "sourceTermId"))
                        .targetTermId(pathStr(r, "targetTermId")).relationType(pathStr(r, "relationType"))
                        .description(pathStr(r, "description")).build());
            }
        }
        List<SemanticAgentPolicy> agentPolicies = new ArrayList<>();
        JsonNode policiesNode = aslNode.get("agentPolicies");
        if (policiesNode != null && policiesNode.isArray()) {
            for (JsonNode policy : policiesNode) {
                agentPolicies.add(SemanticAgentPolicy.builder()
                        .id(pathStr(policy, "id")).roleId(pathStr(policy, "roleId"))
                        .defaultDeny(policy.has("defaultDeny") ? policy.get("defaultDeny").asBoolean() : null)
                        .build());
            }
        }
        return AgentSemanticLayer.builder()
                .intents(intents).businessTerms(businessTerms)
                .semanticRelations(semanticRelations).agentPolicies(agentPolicies)
                .build();
    }

    private EpcModel buildEpcModelFromV1(JsonNode spec) {
        if (spec == null) return EpcModel.builder().build();
        JsonNode epcNode = spec.get("epc");
        if (epcNode == null) return EpcModel.builder().build();

        List<EpcChain> chains = new ArrayList<>();
        JsonNode chainsNode = epcNode.get("chains");
        if (chainsNode != null && chainsNode.isArray()) {
            for (JsonNode chain : chainsNode) {
                List<EpcNode> nodes = new ArrayList<>();
                JsonNode nodesNode = chain.get("nodes");
                if (nodesNode != null && nodesNode.isArray()) {
                    for (JsonNode n : nodesNode) {
                        nodes.add(EpcNode.builder()
                                .id(pathStr(n, "id")).nodeType(pathStr(n, "nodeType"))
                                .name(pathStr(n, "name")).description(pathStr(n, "description"))
                                .refType(pathStr(n, "refType")).refId(pathStr(n, "refId"))
                                .sortOrder(n.has("sortOrder") ? n.get("sortOrder").asInt() : null)
                                .build());
                    }
                }
                List<EpcEdge> edges = new ArrayList<>();
                JsonNode edgesNode = chain.get("edges");
                if (edgesNode != null && edgesNode.isArray()) {
                    for (JsonNode e : edgesNode) {
                        edges.add(EpcEdge.builder()
                                .id(pathStr(e, "id")).sourceNodeId(pathStr(e, "sourceNodeId"))
                                .targetNodeId(pathStr(e, "targetNodeId")).edgeType(pathStr(e, "edgeType"))
                                .label(pathStr(e, "label")).conditionExpr(pathStr(e, "conditionExpr"))
                                .sortOrder(e.has("sortOrder") ? e.get("sortOrder").asInt() : null)
                                .build());
                    }
                }
                chains.add(EpcChain.builder()
                        .id(pathStr(chain, "id")).name(pathStr(chain, "name"))
                        .aggregateRootId(pathStr(chain, "aggregateRootId"))
                        .description(pathStr(chain, "description")).chainType(pathStr(chain, "chainType"))
                        .nodes(nodes).edges(edges).build());
            }
        }

        List<EpcProfile> profiles = new ArrayList<>();
        JsonNode profilesNode = epcNode.get("profiles");
        if (profilesNode != null && profilesNode.isArray()) {
            for (JsonNode p : profilesNode) {
                profiles.add(EpcProfile.builder()
                        .id(pathStr(p, "id"))
                        .chainId(pathStr(p, "chainId"))
                        .profileVersion(pathStr(p, "profileVersion"))
                        .build());
            }
        }

        return EpcModel.builder().chains(chains).profiles(profiles).build();
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

    /**
     * Normalize entityRole value for compatibility with Project1 formats.
     * "entity" or unknown → "aggregate_root"
     * "child_entity" → "child_entity"
     * "aggregate_root" → "aggregate_root"
     */
    static String normalizeEntityRole(String role) {
        if (role == null) return "aggregate_root";
        return switch (role.trim().toLowerCase()) {
            case "child_entity" -> "child_entity";
            default -> "aggregate_root";
        };
    }
}

package com.ontology.platform.infrastructure.imports;

import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument.BusinessScenario;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument.Metadata;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument.ProjectRef;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument.Spec;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument.*;
import com.ontology.platform.domain.dto.imports.YamlManifest;
import com.ontology.platform.domain.dto.imports.YamlManifest.BoundedContext;
import com.ontology.platform.domain.dto.imports.YamlManifest.ObjectTypeDef;
import com.ontology.platform.domain.dto.imports.YamlManifest.Semantic;
import com.ontology.platform.domain.dto.imports.YamlManifest.ValueObject;
import com.ontology.platform.domain.dto.imports.YamlManifest.PropertyDef;
import com.ontology.platform.domain.dto.imports.YamlManifest.RelationDef;
import com.ontology.platform.domain.dto.imports.YamlManifest.StateMachineDef;
import com.ontology.platform.domain.dto.imports.YamlManifest.StateDef;
import com.ontology.platform.domain.dto.imports.YamlManifest.TransitionDef;
import com.ontology.platform.domain.dto.imports.YamlManifest.ActionDef;
import com.ontology.platform.domain.dto.imports.YamlManifest.ParameterDef;
import com.ontology.platform.domain.dto.imports.YamlManifest.RuleDef;
import com.ontology.platform.domain.dto.imports.YamlManifest.DomainEventDef;
import com.ontology.platform.domain.dto.imports.YamlManifest.BoundedContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * YAML Manifest (v1) → OntologyExchangeDocument (v2) upcaster.
 *
 * <p>Converts the parsed Phase 2 YAML manifest format into the Phase 3a
 * v2 exchange format, bridging the two import pipelines.</p>
 */
@Component
public class ManifestUpcasterV1ToV2 {

    /**
     * Upcast a v1 {@link YamlManifest} into a v2 {@link OntologyExchangeDocument}.
     *
     * @param v1 the parsed v1 manifest (may be {@code null})
     * @return the v2 document, or {@code null} when the input is {@code null}
     */
    public OntologyExchangeDocument upcast(YamlManifest v1) {
        if (v1 == null) {
            return null;
        }

        // resolve project id
        String projectId = resolveProjectId(v1);

        // --- Metadata ---
        Metadata metadata = buildMetadata(v1, projectId);

        // --- Spec.Project ---
        OntologyProject project = buildProject(v1, projectId);

        Spec spec = Spec.builder()
                .project(project)
                .build();

        return OntologyExchangeDocument.builder()
                .apiVersion("ontology.platform/v2")
                .kind("OntologyExchange")
                .metadata(metadata)
                .spec(spec)
                .build();
    }

    // ========================================================================
    // Metadata
    // ========================================================================

    private Metadata buildMetadata(YamlManifest v1, String projectId) {
        Metadata.MetadataBuilder builder = Metadata.builder();
        YamlManifest.Metadata v1meta = v1.getMetadata();
        if (v1meta != null) {
            builder.id(v1meta.getId())
                    .version(v1meta.getVersion())
                    .name(v1meta.getName())
                    .displayName(v1meta.getDisplayName())
                    .description(v1meta.getDescription())
                    .source(v1meta.getSource())
                    .status(v1meta.getStatus());
        }
        builder.projectId(projectId);
        builder.exportedAt(now());
        builder.exporterVersion("Hermes-Agent-Upcaster");
        return builder.build();
    }

    // ========================================================================
    // Project
    // ========================================================================

    private OntologyProject buildProject(YamlManifest v1, String projectId) {
        YamlManifest.Metadata v1meta = v1.getMetadata();

        String projectName = v1meta != null && v1meta.getDisplayName() != null
                ? v1meta.getDisplayName()
                : (v1meta != null ? v1meta.getName() : projectId);

        String projectDesc = v1meta != null ? v1meta.getDescription() : null;

        Domain domain = buildDomain(v1);
        DataModel dataModel = buildDataModel(v1, projectId, projectName);
        BehaviorModel behaviorModel = buildBehaviorModel(v1, projectId, projectName);
        RuleModel ruleModel = buildRuleModel(v1, projectId, projectName, v1.getSpec());
        EventModel eventModel = buildEventModel(v1, projectId, projectName);
        GovernanceModel governanceModel = buildGovernanceModel(v1);
        DataSourcesModel dataSourcesModel = buildDataSourcesModel(v1);

        String now = now();
        return OntologyProject.builder()
                .id(projectId)
                .name(projectName)
                .description(projectDesc)
                .domain(domain)
                .dataModel(dataModel)
                .behaviorModel(behaviorModel)
                .ruleModel(ruleModel)
                .eventModel(eventModel)
                .governanceModel(governanceModel)
                .dataSourcesModel(dataSourcesModel)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // ========================================================================
    // Domain
    // ========================================================================

    private Domain buildDomain(YamlManifest v1) {
        BoundedContext bc = null;
        if (v1.getSpec() != null && v1.getSpec().getSemantic() != null) {
            bc = v1.getSpec().getSemantic().getBoundedContext();
        }
        if (bc != null) {
            return Domain.builder()
                    .id(bc.getId())
                    .name(bc.getName())
                    .nameEn(bc.getNameEn())
                    .description(bc.getDescription())
                    .build();
        }
        // default domain
        return Domain.builder()
                .id("default-domain")
                .name("默认域")
                .build();
    }

    // ========================================================================
    // DataModel
    // ========================================================================

    private DataModel buildDataModel(YamlManifest v1, String projectId, String projectName) {
        DataModel.DataModelBuilder builder = DataModel.builder();

        builder.id("dm-" + projectId);
        builder.name(projectName + "数据模型");
        if (v1.getMetadata() != null) {
            builder.version(v1.getMetadata().getVersion());
        }
        builder.domain(resolveDomainName(v1));

        // projects ref
        builder.projects(Collections.singletonList(
                ProjectRef.builder()
                        .id(projectId)
                        .name(projectName)
                        .description(projectName)
                        .build()
        ));

        // business scenarios
        List<BusinessScenario> scenarios = buildBusinessScenarios(v1, projectId);
        if (!scenarios.isEmpty()) {
            builder.businessScenarios(scenarios);
        }

        // entities: objectTypes + valueObjects
        List<Entity> entities = new ArrayList<>();
        if (v1.getSpec() != null && v1.getSpec().getSemantic() != null) {
            Semantic sem = v1.getSpec().getSemantic();

            // objectTypes -> aggregate roots / entities
            if (sem.getObjectTypes() != null) {
                for (ObjectTypeDef ot : sem.getObjectTypes()) {
                    if (ot.getId() == null || ot.getId().isBlank()) continue;
                    entities.add(buildEntityFromObjectType(ot, projectId, sem));
                }
            }

            // valueObjects -> child entities
            if (sem.getValueObjects() != null) {
                for (ValueObject vo : sem.getValueObjects()) {
                    if (vo.getId() == null || vo.getId().isBlank()) continue;
                    entities.add(buildEntityFromValueObject(vo, projectId));
                }
            }
        }
        builder.entities(entities);

        String now = now();
        builder.createdAt(now);
        builder.updatedAt(now);

        return builder.build();
    }

    private List<BusinessScenario> buildBusinessScenarios(YamlManifest v1, String projectId) {
        if (v1.getSpec() == null || v1.getSpec().getSemantic() == null
                || v1.getSpec().getSemantic().getBusinessScenarios() == null) {
            return Collections.emptyList();
        }
        return v1.getSpec().getSemantic().getBusinessScenarios().stream()
                .map(bs -> BusinessScenario.builder()
                        .id(bs.getId())
                        .name(bs.getName())
                        .nameEn(bs.getNameEn())
                        .description(bs.getDescription())
                        .projectId(projectId)
                        .build())
                .filter(bs -> bs.getId() != null && !bs.getId().isBlank())
                .collect(Collectors.toList());
    }

    private Entity buildEntityFromObjectType(ObjectTypeDef ot, String projectId, Semantic sem) {
        String entityRole = mapEntityRole(ot.getKind());
        String parentAggregateId = null;
        if ("aggregate_root".equals(ot.getKind())) {
            parentAggregateId = null; // aggregate roots have no parent
        } else if (ot.getAggregateRootId() != null) {
            parentAggregateId = ot.getAggregateRootId();
        }

        // resolve first businessScenarioId
        String bsId = (ot.getBusinessScenarioIds() != null && !ot.getBusinessScenarioIds().isEmpty())
                ? ot.getBusinessScenarioIds().get(0)
                : null;

        List<Attribute> attributes = buildAttributes(ot.getProperties());
        List<Relation> relations = buildRelations(ot.getRelations());

        return Entity.builder()
                .id(ot.getId())
                .name(ot.getName())
                .nameEn(ot.getNameEn())
                .projectId(projectId)
                .businessScenarioId(bsId)
                .description(ot.getDescription())
                .entityRole(entityRole)
                .parentAggregateId(parentAggregateId)
                .attributes(attributes)
                .relations(relations)
                .build();
    }

    private Entity buildEntityFromValueObject(ValueObject vo, String projectId) {
        List<Attribute> attributes = buildAttributes(vo.getProperties());

        return Entity.builder()
                .id(vo.getId())
                .name(vo.getName())
                .nameEn(vo.getNameEn())
                .projectId(projectId)
                .description("值对象")
                .entityRole("child_entity")
                .attributes(attributes)
                .build();
    }

    private List<Attribute> buildAttributes(List<PropertyDef> props) {
        if (props == null) return Collections.emptyList();
        return props.stream()
                .map(p -> Attribute.builder()
                        .id(p.getId() != null ? p.getId() : UUID.randomUUID().toString())
                        .name(p.getName())
                        .nameEn(p.getNameEn())
                        .dataType(p.getDataType())
                        .required(p.getRequired())
                        .description(p.getName())
                        .build())
                .collect(Collectors.toList());
    }

    private List<Relation> buildRelations(List<RelationDef> rels) {
        if (rels == null) return Collections.emptyList();
        return rels.stream()
                .map(r -> Relation.builder()
                        .id(r.getId() != null ? r.getId() : UUID.randomUUID().toString())
                        .name(r.getName())
                        .type(mapCardinality(r.getCardinality()))
                        .targetEntity(r.getTargetObjectTypeId())
                        .build())
                .collect(Collectors.toList());
    }

    // ========================================================================
    // BehaviorModel
    // ========================================================================

    private BehaviorModel buildBehaviorModel(YamlManifest v1, String projectId, String projectName) {
        BehaviorModel.BehaviorModelBuilder builder = BehaviorModel.builder();
        builder.id("bm-" + projectId);
        builder.name(projectName + "行为模型");
        if (v1.getMetadata() != null) {
            builder.version(v1.getMetadata().getVersion());
        }
        builder.domain(resolveDomainName(v1));

        // state machines
        List<StateMachine> stateMachines = buildStateMachines(v1);
        builder.stateMachines(stateMachines);

        // actions
        List<Action> actions = buildActions(v1);
        builder.actions(actions);

        String now = now();
        builder.createdAt(now);
        builder.updatedAt(now);

        return builder.build();
    }

    private List<StateMachine> buildStateMachines(YamlManifest v1) {
        if (v1.getSpec() == null || v1.getSpec().getSemantic() == null
                || v1.getSpec().getSemantic().getStateMachines() == null) {
            return Collections.emptyList();
        }
        return v1.getSpec().getSemantic().getStateMachines().stream()
                .map(this::buildStateMachine)
                .filter(sm -> sm.getId() != null && !sm.getId().isBlank())
                .collect(Collectors.toList());
    }

    private StateMachine buildStateMachine(StateMachineDef def) {
        List<State> states = new ArrayList<>();
        if (def.getStates() != null) {
            for (StateDef sd : def.getStates()) {
                String stateId = sd.getCode() != null ? sd.getCode() : UUID.randomUUID().toString();
                states.add(State.builder()
                        .id(stateId)
                        .name(sd.getName())
                        .description(sd.getName())
                        .isInitial(sd.getIsInitial())
                        .isFinal(sd.getIsFinal())
                        .build());
            }
        }

        List<Transition> transitions = new ArrayList<>();
        if (def.getTransitions() != null) {
            for (TransitionDef td : def.getTransitions()) {
                String transId = td.getName() != null ? td.getName() : UUID.randomUUID().toString();
                transitions.add(Transition.builder()
                        .id(transId)
                        .name(td.getName())
                        .from(td.getFrom())
                        .to(td.getTo())
                        .trigger(td.getTrigger())
                        .build());
            }
        }

        return StateMachine.builder()
                .id(def.getId())
                .name(def.getName())
                .entity(def.getObjectTypeId())
                .statusField(def.getStatusField())
                .states(states)
                .transitions(transitions)
                .build();
    }

    private List<Action> buildActions(YamlManifest v1) {
        if (v1.getSpec() == null || v1.getSpec().getBehavior() == null
                || v1.getSpec().getBehavior().getActions() == null) {
            return Collections.emptyList();
        }
        return v1.getSpec().getBehavior().getActions().stream()
                .map(this::buildAction)
                .filter(a -> a.getId() != null && !a.getId().isBlank())
                .collect(Collectors.toList());
    }

    private Action buildAction(ActionDef def) {
        List<Parameter> params = new ArrayList<>();
        if (def.getParameters() != null) {
            for (ParameterDef pd : def.getParameters()) {
                params.add(Parameter.builder()
                        .id(pd.getName() != null ? pd.getName() : UUID.randomUUID().toString())
                        .name(pd.getName())
                        .nameEn(pd.getNameEn())
                        .dataType(pd.getDataType())
                        .required(pd.getRequired())
                        .build());
            }
        }

        return Action.builder()
                .id(def.getId())
                .name(def.getName())
                .nameEn(def.getNameEn())
                .description(def.getDescription())
                .targetEntityId(def.getAggregateRootId())
                .actionType("custom")
                .parameters(params)
                .preConditions(def.getPreRuleIds() != null
                        ? def.getPreRuleIds()
                        : List.of())
                .build();
    }

    // ========================================================================
    // RuleModel
    // ========================================================================

    private RuleModel buildRuleModel(YamlManifest v1, String projectId, String projectName, YamlManifest.Spec spec) {
        RuleModel.RuleModelBuilder builder = RuleModel.builder();
        builder.id("rm-" + projectId);
        builder.name(projectName + "规则模型");
        if (v1.getMetadata() != null) {
            builder.version(v1.getMetadata().getVersion());
        }
        builder.domain(resolveDomainName(v1));

        // resolve the first object type id for "entity" default
        String firstEntityId = resolveFirstObjectTypeId(spec);

        List<Rule> rules = new ArrayList<>();
        if (spec != null && spec.getBehavior() != null && spec.getBehavior().getRules() != null) {
            for (RuleDef rd : spec.getBehavior().getRules()) {
                if (rd.getId() == null || rd.getId().isBlank()) continue;
                rules.add(buildRule(rd, firstEntityId));
            }
        }
        builder.rules(rules);

        String now = now();
        builder.createdAt(now);
        builder.updatedAt(now);

        return builder.build();
    }

    private Rule buildRule(RuleDef def, String defaultEntity) {
        RuleCondition condition = null;
        if (def.getExpression() != null) {
            condition = RuleCondition.builder()
                    .type(def.getExpression().getType())
                    .expression(def.getExpression().getDescription())
                    .build();
        }

        return Rule.builder()
                .id(def.getId())
                .name(def.getName())
                .type(def.getType())
                .entity(defaultEntity)
                .condition(condition)
                .errorMessage(def.getErrorMessage())
                .severity("error")
                .build();
    }

    // ========================================================================
    // EventModel
    // ========================================================================

    private EventModel buildEventModel(YamlManifest v1, String projectId, String projectName) {
        EventModel.EventModelBuilder builder = EventModel.builder();
        builder.id("em-" + projectId);
        builder.name(projectName + "事件模型");
        if (v1.getMetadata() != null) {
            builder.version(v1.getMetadata().getVersion());
        }
        builder.domain(resolveDomainName(v1));

        List<EventDefinition> events = new ArrayList<>();
        if (v1.getSpec() != null && v1.getSpec().getEvents() != null
                && v1.getSpec().getEvents().getDomainEvents() != null) {
            for (DomainEventDef ed : v1.getSpec().getEvents().getDomainEvents()) {
                if (ed.getId() == null || ed.getId().isBlank()) continue;
                events.add(EventDefinition.builder()
                        .id(ed.getId())
                        .name(ed.getName())
                        .nameEn(ed.getNameEn())
                        .entity(ed.getAggregateRootId())
                        .isDomainEvent(true)
                        .build());
            }
        }
        builder.events(events);

        String now = now();
        builder.createdAt(now);
        builder.updatedAt(now);

        return builder.build();
    }

    // ========================================================================
    // GovernanceModel
    // ========================================================================

    private GovernanceModel buildGovernanceModel(YamlManifest v1) {
        GovernanceModel.GovernanceModelBuilder builder = GovernanceModel.builder();

        if (v1.getSpec() != null && v1.getSpec().getGovernance() != null) {
            YamlManifest.Governance gov = v1.getSpec().getGovernance();

            // roles
            if (gov.getRoles() != null) {
                builder.roles(gov.getRoles().stream()
                        .map(r -> GovernanceRole.builder()
                                .id(r.getId())
                                .name(r.getName())
                                .permissions(r.getPermissions() != null
                                        ? r.getPermissions().stream()
                                        .map(p -> Permission.builder()
                                                .objectTypeId(p.getObjectTypeId())
                                                .ops(p.getOps() != null ? p.getOps() : new ArrayList<>())
                                                .build())
                                        .collect(Collectors.toList())
                                        : new ArrayList<>())
                                .build())
                        .collect(Collectors.toList()));
            }

            // fieldPermissions
            if (gov.getFieldPermissions() != null) {
                builder.fieldPermissions(gov.getFieldPermissions().stream()
                        .map(fp -> FieldPermission.builder()
                                .objectTypeId(fp.getObjectTypeId())
                                .propertyNameEn(fp.getPropertyNameEn())
                                .allowedRoleIds(fp.getAllowedRoleIds() != null
                                        ? fp.getAllowedRoleIds()
                                        : new ArrayList<>())
                                .build())
                        .collect(Collectors.toList()));
            }

            // agentPolicies
            if (gov.getAgentPolicies() != null) {
                builder.agentPolicies(gov.getAgentPolicies().stream()
                        .map(ap -> AgentPolicy.builder()
                                .id(ap.getId())
                                .roleId(ap.getRoleId())
                                .allowedMcpTools(ap.getAllowedMcpTools() != null
                                        ? ap.getAllowedMcpTools()
                                        : new ArrayList<>())
                                .allowedAggregateRootIds(ap.getAllowedAggregateRootIds() != null
                                        ? ap.getAllowedAggregateRootIds()
                                        : new ArrayList<>())
                                .build())
                        .collect(Collectors.toList()));
            }
        }

        return builder.build();
    }

    // ========================================================================
    // DataSourcesModel
    // ========================================================================

    private DataSourcesModel buildDataSourcesModel(YamlManifest v1) {
        DataSourcesModel.DataSourcesModelBuilder builder = DataSourcesModel.builder();

        if (v1.getSpec() != null && v1.getSpec().getDataSources() != null) {
            builder.sources(v1.getSpec().getDataSources().stream()
                    .map(ds -> DataSource.builder()
                            .id(ds.getId())
                            .name(ds.getName())
                            .type(ds.getType())
                            .boundObjectTypeId(ds.getBoundObjectTypeId())
                            .api(ds.getApi() != null
                                    ? ApiDef.builder()
                                    .baseUrl(ds.getApi().getBaseUrl())
                                    .entitySet(ds.getApi().getEntitySet())
                                    .authSecretRef(ds.getApi().getAuthSecretRef())
                                    .build()
                                    : null)
                            .build())
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private String resolveProjectId(YamlManifest v1) {
        if (v1.getSpec() != null && v1.getSpec().getSemantic() != null
                && v1.getSpec().getSemantic().getBoundedContext() != null
                && v1.getSpec().getSemantic().getBoundedContext().getId() != null) {
            return v1.getSpec().getSemantic().getBoundedContext().getId();
        }
        if (v1.getMetadata() != null && v1.getMetadata().getId() != null) {
            return v1.getMetadata().getId();
        }
        return UUID.randomUUID().toString();
    }

    private String resolveDomainName(YamlManifest v1) {
        if (v1.getSpec() != null && v1.getSpec().getSemantic() != null
                && v1.getSpec().getSemantic().getBoundedContext() != null
                && v1.getSpec().getSemantic().getBoundedContext().getName() != null) {
            return v1.getSpec().getSemantic().getBoundedContext().getName();
        }
        return "默认域";
    }

    private String resolveFirstObjectTypeId(YamlManifest.Spec spec) {
        if (spec != null && spec.getSemantic() != null
                && spec.getSemantic().getObjectTypes() != null
                && !spec.getSemantic().getObjectTypes().isEmpty()) {
            return spec.getSemantic().getObjectTypes().get(0).getId();
        }
        return null;
    }

    /**
     * Map v1 entity kind to v2 entityRole.
     */
    private String mapEntityRole(String kind) {
        if ("aggregate_root".equals(kind)) {
            return "aggregate_root";
        }
        return "child_entity";
    }

    /**
     * Map v1 cardinality string to v2 relation type.
     */
    private String mapCardinality(String cardinality) {
        if (cardinality == null) return null;
        switch (cardinality) {
            case "N:1": return "many_to_one";
            case "1:N": return "one_to_many";
            case "1:1": return "one_to_one";
            case "N:N": return "many_to_many";
            default: return cardinality;
        }
    }

    private String now() {
        return Instant.now().toString();
    }
}

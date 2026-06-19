package com.ontology.platform.infrastructure.imports;

import com.ontology.platform.domain.dto.imports.YamlManifest;
import com.ontology.platform.domain.dto.imports.YamlManifest.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.stream.Collectors;

/**
 * YAML OntologyManifest 解析器
 *
 * <p>使用 SnakeYAML 将项目1导出的 YAML OntologyManifest 解析为 {@link YamlManifest} DTO。</p>
 */
@Component
@RequiredArgsConstructor
public class YamlManifestParser {

    private final Yaml yaml = new Yaml();

    /**
     * 解析 YAML 字符串为 YamlManifest DTO
     *
     * @param yamlContent 项目1导出的 YAML 内容
     * @return 解析后的 YamlManifest
     * @throws IllegalArgumentException 格式非法时抛出
     */
    @SuppressWarnings("unchecked")
    public YamlManifest parse(String yamlContent) {
        if (yamlContent == null || yamlContent.isBlank()) {
            throw new IllegalArgumentException("YAML 内容为空");
        }

        Map<String, Object> raw;
        try {
            raw = yaml.load(yamlContent);
        } catch (Exception e) {
            throw new IllegalArgumentException("YAML 解析失败: " + e.getMessage(), e);
        }

        if (raw == null || raw.isEmpty()) {
            throw new IllegalArgumentException("YAML 内容为空或格式无效");
        }

        YamlManifest.YamlManifestBuilder builder = YamlManifest.builder();

        builder.apiVersion(str(raw.get("apiVersion")));
        builder.kind(str(raw.get("kind")));
        builder.metadata(parseMetadata(map(raw.get("metadata"))));
        builder.spec(parseSpec(map(raw.get("spec"))));

        return builder.build();
    }

    // ==================== Metadata ====================

    private Metadata parseMetadata(Map<String, Object> m) {
        if (m == null) return null;
        return Metadata.builder()
                .id(str(m.get("id")))
                .version(str(m.get("version")))
                .name(str(m.get("name")))
                .displayName(str(m.get("displayName")))
                .description(str(m.get("description")))
                .boundedContext(str(m.get("boundedContext")))
                .domainTags(strList(m.get("domainTags")))
                .compiledAt(str(m.get("compiledAt")))
                .source(str(m.get("source")))
                .status(str(m.get("status")))
                .build();
    }

    // ==================== Spec ====================

    private Spec parseSpec(Map<String, Object> s) {
        if (s == null) return null;
        return Spec.builder()
                .semantic(parseSemantic(map(s.get("semantic"))))
                .behavior(parseBehavior(map(s.get("behavior"))))
                .events(parseEvents(map(s.get("events"))))
                .governance(parseGovernance(map(s.get("governance"))))
                .dataSources(parseDataSources(s.get("dataSources")))
                .build();
    }

    // ==================== Semantic ====================

    @SuppressWarnings("unchecked")
    private Semantic parseSemantic(Map<String, Object> sem) {
        if (sem == null) return null;
        return Semantic.builder()
                .boundedContext(parseBoundedContext(map(sem.get("boundedContext"))))
                .businessScenarios(parseList(sem.get("businessScenarios"), this::parseBusinessScenario))
                .valueObjects(parseList(sem.get("valueObjects"), this::parseValueObject))
                .objectTypes(parseList(sem.get("objectTypes"), this::parseObjectTypeDef))
                .stateMachines(parseList(sem.get("stateMachines"), this::parseStateMachineDef))
                .build();
    }

    private BoundedContext parseBoundedContext(Map<String, Object> m) {
        if (m == null) return null;
        return BoundedContext.builder()
                .id(str(m.get("id")))
                .name(str(m.get("name")))
                .nameEn(str(m.get("nameEn")))
                .description(str(m.get("description")))
                .build();
    }

    private BusinessScenario parseBusinessScenario(Map<String, Object> m) {
        if (m == null) return null;
        return BusinessScenario.builder()
                .id(str(m.get("id")))
                .name(str(m.get("name")))
                .nameEn(str(m.get("nameEn")))
                .description(str(m.get("description")))
                .applicableObjectTypeIds(strList(m.get("applicableObjectTypeIds")))
                .build();
    }

    private ValueObject parseValueObject(Map<String, Object> m) {
        if (m == null) return null;
        return ValueObject.builder()
                .id(str(m.get("id")))
                .name(str(m.get("name")))
                .nameEn(str(m.get("nameEn")))
                .properties(parseList(m.get("properties"), this::parsePropertyDef))
                .build();
    }

    private ObjectTypeDef parseObjectTypeDef(Map<String, Object> m) {
        if (m == null) return null;
        return ObjectTypeDef.builder()
                .id(str(m.get("id")))
                .name(str(m.get("name")))
                .nameEn(str(m.get("nameEn")))
                .kind(str(m.get("kind")))
                .description(str(m.get("description")))
                .aggregateRootId(str(m.get("aggregateRootId")))
                .businessScenarioIds(strList(m.get("businessScenarioIds")))
                .properties(parseList(m.get("properties"), this::parsePropertyDef))
                .relations(parseList(m.get("relations"), this::parseRelationDef))
                .build();
    }

    private PropertyDef parsePropertyDef(Map<String, Object> m) {
        if (m == null) return null;
        return PropertyDef.builder()
                .id(str(m.get("id")))
                .name(str(m.get("name")))
                .nameEn(str(m.get("nameEn")))
                .dataType(str(m.get("dataType")))
                .required(bool(m.get("required")))
                .sensitive(bool(m.get("sensitive")))
                .valueObjectRef(str(m.get("valueObjectRef")))
                .enumValues(strList(m.get("enumValues")))
                .build();
    }

    private RelationDef parseRelationDef(Map<String, Object> m) {
        if (m == null) return null;
        return RelationDef.builder()
                .id(str(m.get("id")))
                .name(str(m.get("name")))
                .sourceObjectTypeId(str(m.get("sourceObjectTypeId")))
                .targetObjectTypeId(str(m.get("targetObjectTypeId")))
                .cardinality(str(m.get("cardinality")))
                .relationKind(str(m.get("relationKind")))
                .build();
    }

    private StateMachineDef parseStateMachineDef(Map<String, Object> m) {
        if (m == null) return null;
        return StateMachineDef.builder()
                .id(str(m.get("id")))
                .name(str(m.get("name")))
                .objectTypeId(str(m.get("objectTypeId")))
                .statusField(str(m.get("statusField")))
                .states(parseList(m.get("states"), this::parseStateDef))
                .transitions(parseList(m.get("transitions"), this::parseTransitionDef))
                .build();
    }

    private StateDef parseStateDef(Map<String, Object> m) {
        if (m == null) return null;
        return StateDef.builder()
                .name(str(m.get("name")))
                .code(str(m.get("code")))
                .isInitial(bool(m.get("isInitial")))
                .isFinal(bool(m.get("isFinal")))
                .build();
    }

    private TransitionDef parseTransitionDef(Map<String, Object> m) {
        if (m == null) return null;
        return TransitionDef.builder()
                .name(str(m.get("name")))
                .from(str(m.get("from")))
                .to(str(m.get("to")))
                .trigger(str(m.get("trigger")))
                .actionId(str(m.get("actionId")))
                .build();
    }

    // ==================== Behavior ====================

    private Behavior parseBehavior(Map<String, Object> b) {
        if (b == null) return null;
        return Behavior.builder()
                .actions(parseList(b.get("actions"), this::parseActionDef))
                .rules(parseList(b.get("rules"), this::parseRuleDef))
                .metrics(parseList(b.get("metrics"), this::parseMetricDef))
                .transactionBoundaries(parseList(b.get("transactionBoundaries"), this::parseTransactionBoundaryDef))
                .build();
    }

    private ActionDef parseActionDef(Map<String, Object> m) {
        if (m == null) return null;
        return ActionDef.builder()
                .id(str(m.get("id")))
                .name(str(m.get("name")))
                .nameEn(str(m.get("nameEn")))
                .aggregateRootId(str(m.get("aggregateRootId")))
                .description(str(m.get("description")))
                .businessScenarioIds(strList(m.get("businessScenarioIds")))
                .parameters(parseList(m.get("parameters"), this::parseParameterDef))
                .preRuleIds(strList(m.get("preRuleIds")))
                .allowedStateFrom(strList(m.get("allowedStateFrom")))
                .publishesEventIds(strList(m.get("publishesEventIds")))
                .mcpToolName(str(m.get("mcpToolName")))
                .build();
    }

    private ParameterDef parseParameterDef(Map<String, Object> m) {
        if (m == null) return null;
        return ParameterDef.builder()
                .name(str(m.get("name")))
                .nameEn(str(m.get("nameEn")))
                .dataType(str(m.get("dataType")))
                .required(bool(m.get("required")))
                .build();
    }

    private RuleDef parseRuleDef(Map<String, Object> m) {
        if (m == null) return null;
        return RuleDef.builder()
                .id(str(m.get("id")))
                .name(str(m.get("name")))
                .type(str(m.get("type")))
                .expression(parseExpressionDef(map(m.get("expression"))))
                .errorMessage(str(m.get("errorMessage")))
                .build();
    }

    private ExpressionDef parseExpressionDef(Map<String, Object> m) {
        if (m == null) return null;
        return ExpressionDef.builder()
                .type(str(m.get("type")))
                .description(str(m.get("description")))
                .build();
    }

    private MetricDef parseMetricDef(Map<String, Object> m) {
        if (m == null) return null;
        return MetricDef.builder()
                .id(str(m.get("id")))
                .name(str(m.get("name")))
                .formula(str(m.get("formula")))
                .dataSourceRef(str(m.get("dataSourceRef")))
                .period(str(m.get("period")))
                .build();
    }

    private TransactionBoundaryDef parseTransactionBoundaryDef(Map<String, Object> m) {
        if (m == null) return null;
        return TransactionBoundaryDef.builder()
                .id(str(m.get("id")))
                .actionId(str(m.get("actionId")))
                .description(str(m.get("description")))
                .operations(parseList(m.get("operations"), this::parseOperationDef))
                .build();
    }

    private OperationDef parseOperationDef(Map<String, Object> m) {
        if (m == null) return null;
        return OperationDef.builder()
                .type(str(m.get("type")))
                .field(str(m.get("field")))
                .value(str(m.get("value")))
                .build();
    }

    // ==================== Events ====================

    private Events parseEvents(Map<String, Object> e) {
        if (e == null) return null;
        return Events.builder()
                .domainEvents(parseList(e.get("domainEvents"), this::parseDomainEventDef))
                .routes(parseList(e.get("routes"), this::parseRouteDef))
                .handlers(parseList(e.get("handlers"), this::parseHandlerDef))
                .build();
    }

    private DomainEventDef parseDomainEventDef(Map<String, Object> m) {
        if (m == null) return null;
        return DomainEventDef.builder()
                .id(str(m.get("id")))
                .name(str(m.get("name")))
                .nameEn(str(m.get("nameEn")))
                .aggregateRootId(str(m.get("aggregateRootId")))
                .triggerActionId(str(m.get("triggerActionId")))
                .payloadSchema(parsePayloadSchemaDef(map(m.get("payloadSchema"))))
                .build();
    }

    private PayloadSchemaDef parsePayloadSchemaDef(Map<String, Object> m) {
        if (m == null) return null;
        return PayloadSchemaDef.builder()
                .required(strList(m.get("required")))
                .build();
    }

    private RouteDef parseRouteDef(Map<String, Object> m) {
        if (m == null) return null;
        return RouteDef.builder()
                .id(str(m.get("id")))
                .eventId(str(m.get("eventId")))
                .targets(parseList(m.get("targets"), this::parseTargetDef))
                .build();
    }

    private TargetDef parseTargetDef(Map<String, Object> m) {
        if (m == null) return null;
        return TargetDef.builder()
                .boundedContext(str(m.get("boundedContext")))
                .system(str(m.get("system")))
                .build();
    }

    private HandlerDef parseHandlerDef(Map<String, Object> m) {
        if (m == null) return null;
        return HandlerDef.builder()
                .id(str(m.get("id")))
                .routeId(str(m.get("routeId")))
                .targetBoundedContext(str(m.get("targetBoundedContext")))
                .actionId(str(m.get("actionId")))
                .businessScenarioIds(strList(m.get("businessScenarioIds")))
                .build();
    }

    // ==================== Governance ====================

    private Governance parseGovernance(Map<String, Object> g) {
        if (g == null) return null;
        return Governance.builder()
                .roles(parseList(g.get("roles"), this::parseRoleDef))
                .fieldPermissions(parseList(g.get("fieldPermissions"), this::parseFieldPermissionDef))
                .agentPolicies(parseList(g.get("agentPolicies"), this::parseAgentPolicyDef))
                .build();
    }

    private RoleDef parseRoleDef(Map<String, Object> m) {
        if (m == null) return null;
        return RoleDef.builder()
                .id(str(m.get("id")))
                .name(str(m.get("name")))
                .permissions(parseList(m.get("permissions"), this::parsePermissionDef))
                .build();
    }

    private PermissionDef parsePermissionDef(Map<String, Object> m) {
        if (m == null) return null;
        return PermissionDef.builder()
                .objectTypeId(str(m.get("objectTypeId")))
                .ops(strList(m.get("ops")))
                .denyActionIds(strList(m.get("denyActionIds")))
                .build();
    }

    private FieldPermissionDef parseFieldPermissionDef(Map<String, Object> m) {
        if (m == null) return null;
        return FieldPermissionDef.builder()
                .objectTypeId(str(m.get("objectTypeId")))
                .propertyNameEn(str(m.get("propertyNameEn")))
                .allowedRoleIds(strList(m.get("allowedRoleIds")))
                .build();
    }

    private AgentPolicyDef parseAgentPolicyDef(Map<String, Object> m) {
        if (m == null) return null;
        return AgentPolicyDef.builder()
                .id(str(m.get("id")))
                .manifestVersion(str(m.get("manifestVersion")))
                .roleId(str(m.get("roleId")))
                .allowedMcpTools(strList(m.get("allowedMcpTools")))
                .allowedAggregateRootIds(strList(m.get("allowedAggregateRootIds")))
                .allowedActionIds(strList(m.get("allowedActionIds")))
                .rateLimit(parseRateLimitDef(map(m.get("rateLimit"))))
                .defaultDeny(bool(m.get("defaultDeny")))
                .build();
    }

    private RateLimitDef parseRateLimitDef(Map<String, Object> m) {
        if (m == null) return null;
        return RateLimitDef.builder()
                .maxCallsPerSecond(intObj(m.get("maxCallsPerSecond")))
                .build();
    }

    // ==================== Data Sources ====================

    private List<DataSource> parseDataSources(Object raw) {
        if (!(raw instanceof List)) return null;
        return ((List<Map<String, Object>>) raw).stream()
                .map(this::parseDataSource)
                .collect(Collectors.toList());
    }

    private DataSource parseDataSource(Map<String, Object> m) {
        if (m == null) return null;
        return DataSource.builder()
                .id(str(m.get("id")))
                .name(str(m.get("name")))
                .type(str(m.get("type")))
                .boundObjectTypeId(str(m.get("boundObjectTypeId")))
                .api(parseApiDef(map(m.get("api"))))
                .build();
    }

    private ApiDef parseApiDef(Map<String, Object> m) {
        if (m == null) return null;
        return ApiDef.builder()
                .baseUrl(str(m.get("baseUrl")))
                .entitySet(str(m.get("entitySet")))
                .authSecretRef(str(m.get("authSecretRef")))
                .build();
    }

    // ==================== 辅助方法 ====================

    @SuppressWarnings("unchecked")
    private <T> List<T> parseList(Object raw, java.util.function.Function<Map<String, Object>, T> mapper) {
        if (!(raw instanceof List)) return new ArrayList<>();
        return ((List<Map<String, Object>>) raw).stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object obj) {
        return obj instanceof Map ? (Map<String, Object>) obj : null;
    }

    private static String str(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    @SuppressWarnings("unchecked")
    private static List<String> strList(Object obj) {
        if (obj instanceof List) {
            return ((List<Object>) obj).stream()
                    .map(o -> o != null ? o.toString() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return null;
    }

    private static Boolean bool(Object obj) {
        if (obj instanceof Boolean) return (Boolean) obj;
        if (obj instanceof String) return Boolean.parseBoolean((String) obj);
        return null;
    }

    private static Integer intObj(Object obj) {
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();
        if (obj instanceof String) {
            try { return Integer.parseInt((String) obj); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }
}

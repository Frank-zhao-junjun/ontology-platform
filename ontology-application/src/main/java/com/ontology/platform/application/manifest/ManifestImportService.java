package com.ontology.platform.application.manifest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.ontology.platform.application.service.BehaviorService;
import com.ontology.platform.application.service.BoundedContextService;
import com.ontology.platform.application.service.DataSourceService;
import com.ontology.platform.application.service.GovernanceService;
import com.ontology.platform.application.service.ModelingService;
import com.ontology.platform.common.enums.DomainTag;
import com.ontology.platform.common.enums.InvocationMode;
import com.ontology.platform.domain.entity.AggregateRoot;
import com.ontology.platform.domain.entity.BoundedContext;
import com.ontology.platform.domain.entity.ObjectTypeV2;
import com.ontology.platform.domain.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * US-A01 Round 2: persist validated Manifest into platform semantic/governance/data-source tables.
 */
@Service
@RequiredArgsConstructor
public class ManifestImportService {
    private static final Set<String> OPS_READ = Set.of("READ");
    private static final Set<String> OPS_WRITE = Set.of("WRITE");
    private static final Set<String> OPS_DELETE = Set.of("DELETE");
    private static final Set<String> OPS_EXECUTE = Set.of("EXECUTE");

    private final ManifestImportDryRunService dryRunService;
    private final ManifestYamlParser yamlParser;
    private final BoundedContextService boundedContextService;
    private final ModelingService modelingService;
    private final GovernanceService governanceService;
    private final DataSourceService dataSourceService;
    private final BehaviorService behaviorService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public ManifestImportResult importYaml(String yaml) throws IOException {
        return importDocument(yamlParser.parseYaml(yaml));
    }

    @Transactional
    public ManifestImportResult importJson(String json) throws IOException {
        return importDocument(yamlParser.parseJson(json));
    }

    public ManifestImportResult importDocument(JsonNode root) throws IOException {
        ManifestDryRunResult dry = dryRunService.dryRun(root);
        ManifestImportResult result = toImportResult(dry);
        if (!dry.isValid()) {
            return result;
        }

        JsonNode metadata = root.get("metadata");
        JsonNode spec = root.get("spec");
        JsonNode semantic = spec.get("semantic");
        JsonNode governance = spec.get("governance");

        Map<String, String> objectTypeIdByManifest = new HashMap<>();
        Map<String, String> aggregateRootIdByManifest = new HashMap<>();
        Map<String, String> roleIdByManifest = new HashMap<>();
        Map<String, String> dataSourceIdByManifest = new HashMap<>();
        Map<String, String> ruleIdByManifest = new HashMap<>();
        Map<String, String> actionIdByManifest = new HashMap<>();

        BoundedContext ctx = createContext(metadata, semantic);
        result.setContextId(ctx.getId());
        result.setDraftId(ctx.getId());

        importAggregateRootsAndObjectTypes(semantic, ctx.getId(), objectTypeIdByManifest, aggregateRootIdByManifest);
        importRelationships(semantic, ctx.getId(), objectTypeIdByManifest);
        JsonNode behavior = spec.get("behavior");
        JsonNode events = spec.get("events");
        importRules(behavior, ctx.getId(), ruleIdByManifest);
        importActions(behavior, ctx.getId(), aggregateRootIdByManifest, ruleIdByManifest, actionIdByManifest);
        importDomainEvents(events, ctx.getId(), aggregateRootIdByManifest, actionIdByManifest);
        importGovernance(governance, ctx.getId(), objectTypeIdByManifest, aggregateRootIdByManifest, roleIdByManifest, result);
        importDataSources(spec, ctx.getId(), objectTypeIdByManifest, dataSourceIdByManifest, result);

        result.getWarnings().add("IMPORT_PERSISTED: semantic + behavior + events + governance + dataSources written");
        result.getWarnings().add("DRAFT_ID: draft context id; call POST /v1/contexts/{id}/approve to publish manifest snapshot");
        return result;
    }

    private BoundedContext createContext(JsonNode metadata, JsonNode semantic) {
        JsonNode bc = semantic.get("boundedContext");
        String name = text(bc, "name");
        String code = slugCode(text(bc, "nameEn"), text(bc, "id"));
        String description = text(bc, "description");
        String ontologyId = text(metadata, "id");
        DomainTag tag = resolveDomainTag(metadata);
        return boundedContextService.create(name, code, description, tag, "manifest-import", ontologyId);
    }

    private void importAggregateRootsAndObjectTypes(JsonNode semantic, String contextId,
                                                      Map<String, String> objectTypeIdByManifest,
                                                      Map<String, String> aggregateRootIdByManifest) throws IOException {
        List<JsonNode> objectTypes = new ArrayList<>(array(semantic, "objectTypes"));
        objectTypes.sort((a, b) -> kindOrder(text(a, "kind")) - kindOrder(text(b, "kind")));
        for (JsonNode ot : objectTypes) {
            String manifestId = text(ot, "id");
            String kind = text(ot, "kind");
            String name = text(ot, "name");
            String code = slugCode(text(ot, "nameEn"), manifestId);
            String description = text(ot, "description");

            String platformAggregateRootId = null;
            if ("aggregate_root".equals(kind)) {
                AggregateRoot ar = modelingService.createAggregateRoot(contextId, name, code, description);
                platformAggregateRootId = ar.getId();
                aggregateRootIdByManifest.put(manifestId, ar.getId());
            } else if ("entity".equals(kind)) {
                String manifestArId = text(ot, "aggregateRootId");
                platformAggregateRootId = aggregateRootIdByManifest.get(manifestArId);
            }

            String objectKind = mapObjectKind(kind);
            ObjectTypeV2 created = modelingService.createObjectType(contextId, name, code, objectKind, platformAggregateRootId);
            objectTypeIdByManifest.put(manifestId, created.getId());

            if (!array(ot, "properties").isEmpty()) {
                modelingService.updateAttributes(created.getId(), serializeProperties(ot));
            }
        }
    }

    private void importRules(JsonNode behavior, String contextId, Map<String, String> ruleIdByManifest) throws IOException {
        if (behavior == null) {
            return;
        }
        for (JsonNode ruleNode : array(behavior, "rules")) {
            String manifestCode = text(ruleNode, "id");
            String expressionJson = ruleNode.has("expression")
                    ? objectMapper.writeValueAsString(ruleNode.get("expression")) : "{}";
            String failureSchema = ruleNode.has("failurePayloadSchema")
                    ? objectMapper.writeValueAsString(ruleNode.get("failurePayloadSchema")) : null;
            var rule = behaviorService.createRule(contextId, manifestCode, text(ruleNode, "name"),
                    text(ruleNode, "type"), expressionJson, text(ruleNode, "errorMessage"), failureSchema);
            ruleIdByManifest.put(manifestCode, rule.getId());
        }
    }

    private void importActions(JsonNode behavior, String contextId, Map<String, String> aggregateRootIdByManifest,
                               Map<String, String> ruleIdByManifest,
                               Map<String, String> actionIdByManifest) throws IOException {
        if (behavior == null) {
            return;
        }
        for (JsonNode actionNode : array(behavior, "actions")) {
            String manifestCode = text(actionNode, "id");
            String arManifest = text(actionNode, "aggregateRootId");
            String platformArId = aggregateRootIdByManifest.get(arManifest);
            if (platformArId == null) {
                continue;
            }
            var action = behaviorService.createAction(contextId, manifestCode,
                    text(actionNode, "name"), text(actionNode, "nameEn"), text(actionNode, "description"),
                    platformArId, InvocationMode.BOTH,
                    writeJsonArray(actionNode.get("parameters")),
                    writeJsonStringList(actionNode.get("publishesEventIds")),
                    writeJsonStringList(actionNode.get("allowedStateFrom")),
                    writeJsonStringList(actionNode.get("businessScenarioIds")),
                    text(actionNode, "mcpToolName"));
            actionIdByManifest.put(manifestCode, action.getId());

            List<String> platformRuleIds = new ArrayList<>();
            for (JsonNode ruleRef : array(actionNode, "preRuleIds")) {
                String rid = ruleIdByManifest.get(ruleRef.asText());
                if (rid != null) {
                    platformRuleIds.add(rid);
                }
            }
            behaviorService.linkRulesToAction(action.getId(), platformRuleIds);
        }
    }

    private void importDomainEvents(JsonNode events, String contextId,
                                    Map<String, String> aggregateRootIdByManifest,
                                    Map<String, String> actionIdByManifest) throws IOException {
        if (events == null) {
            return;
        }
        for (JsonNode evt : array(events, "domainEvents")) {
            String manifestCode = text(evt, "id");
            String arManifest = text(evt, "aggregateRootId");
            String platformArId = aggregateRootIdByManifest.get(arManifest);
            if (platformArId == null) {
                continue;
            }
            String triggerManifest = text(evt, "triggerActionId");
            String triggerPlatformId = triggerManifest != null ? actionIdByManifest.get(triggerManifest) : null;
            String payload = evt.has("payloadSchema")
                    ? objectMapper.writeValueAsString(evt.get("payloadSchema")) : "{}";
            behaviorService.createDomainEvent(contextId, manifestCode,
                    text(evt, "name"), text(evt, "nameEn"), platformArId, triggerPlatformId, payload);
        }
    }

    private String writeJsonArray(JsonNode node) throws IOException {
        if (node == null || !node.isArray()) {
            return "[]";
        }
        return objectMapper.writeValueAsString(node);
    }

    private String writeJsonStringList(JsonNode node) throws IOException {
        if (node == null || !node.isArray()) {
            return "[]";
        }
        return objectMapper.writeValueAsString(node);
    }

    private void importRelationships(JsonNode semantic, String contextId, Map<String, String> objectTypeIdByManifest) {
        for (JsonNode ot : array(semantic, "objectTypes")) {
            for (JsonNode rel : array(ot, "relations")) {
                String sourceManifest = text(rel, "sourceObjectTypeId");
                if (sourceManifest == null) {
                    sourceManifest = text(ot, "id");
                }
                String targetManifest = text(rel, "targetObjectTypeId");
                String sourceId = objectTypeIdByManifest.get(sourceManifest);
                String targetId = objectTypeIdByManifest.get(targetManifest);
                if (sourceId == null || targetId == null) {
                    continue;
                }
                modelingService.createRelationship(contextId, sourceId, targetId,
                        text(rel, "name"),
                        slugCode(text(rel, "id"), text(rel, "name")),
                        textOrDefault(rel, "cardinality", "1:N"),
                        mapRelationKind(text(rel, "relationKind")));
            }
        }
    }

    private void importGovernance(JsonNode governance, String contextId,
                                  Map<String, String> objectTypeIdByManifest,
                                  Map<String, String> aggregateRootIdByManifest,
                                  Map<String, String> roleIdByManifest,
                                  ManifestImportResult result) {
        if (governance == null) {
            return;
        }
        for (JsonNode roleNode : array(governance, "roles")) {
            String manifestRoleId = text(roleNode, "id");
            Role role = governanceService.createRole(contextId,
                    text(roleNode, "name"),
                    slugCode(manifestRoleId, manifestRoleId),
                    "");
            roleIdByManifest.put(manifestRoleId, role.getId());

            for (JsonNode perm : array(roleNode, "permissions")) {
                String otManifest = text(perm, "objectTypeId");
                String otPlatform = objectTypeIdByManifest.get(otManifest);
                if (otPlatform == null) {
                    continue;
                }
                List<String> ops = opsList(perm);
                governanceService.addObjectPermission(role.getId(), otPlatform,
                        ops.contains("READ"), ops.contains("WRITE"),
                        ops.contains("DELETE"), ops.contains("EXECUTE"));
            }
        }

        for (JsonNode fp : array(governance, "fieldPermissions")) {
            String otPlatform = objectTypeIdByManifest.get(text(fp, "objectTypeId"));
            String fieldName = text(fp, "propertyNameEn");
            if (otPlatform == null || fieldName == null) {
                continue;
            }
            for (JsonNode roleRef : array(fp, "allowedRoleIds")) {
                String platformRoleId = roleIdByManifest.get(roleRef.asText());
                if (platformRoleId != null) {
                    governanceService.addFieldPermission(platformRoleId, otPlatform, fieldName, true, false);
                }
            }
        }

        for (JsonNode policy : array(governance, "agentPolicies")) {
            String roleManifest = text(policy, "roleId");
            String platformRoleId = roleIdByManifest.get(roleManifest);
            List<String> tools = stringList(policy, "allowedMcpTools");
            if (tools.isEmpty()) {
                tools = stringList(policy, "allowedTools");
            }
            List<String> aggRoots = new ArrayList<>();
            for (String manifestAr : stringList(policy, "allowedAggregateRootIds")) {
                String platformOt = objectTypeIdByManifest.get(manifestAr);
                if (platformOt != null) {
                    aggRoots.add(platformOt);
                }
            }
            int maxOps = 10;
            if (policy.has("rateLimit") && policy.get("rateLimit").has("maxCallsPerSecond")) {
                maxOps = policy.get("rateLimit").get("maxCallsPerSecond").asInt(10);
            }
            governanceService.createSandbox(
                    text(policy, "id"),
                    text(policy, "manifestVersion"),
                    platformRoleId,
                    tools,
                    aggRoots,
                    stringList(policy, "allowedActionIds"),
                    maxOps);
        }
    }

    private void importDataSources(JsonNode spec, String contextId,
                                   Map<String, String> objectTypeIdByManifest,
                                   Map<String, String> dataSourceIdByManifest,
                                   ManifestImportResult result) {
        for (JsonNode ds : array(spec, "dataSources")) {
            String manifestId = text(ds, "id");
            String code = slugCode(manifestId, manifestId);
            String sourceType = text(ds, "type");
            if (sourceType != null) {
                sourceType = sourceType.toUpperCase(Locale.ROOT);
            }
            String credentialRef = null;
            String connectionConfig = "{}";
            if (ds.has("api")) {
                JsonNode api = ds.get("api");
                credentialRef = text(api, "authSecretRef");
                try {
                    connectionConfig = objectMapper.writeValueAsString(api);
                } catch (IOException e) {
                    connectionConfig = api.toString();
                }
            }
            var created = dataSourceService.createDataSource(
                    text(ds, "name"), code, sourceType, connectionConfig, credentialRef);
            dataSourceIdByManifest.put(manifestId, created.getId());

            String boundOt = text(ds, "boundObjectTypeId");
            String platformOt = objectTypeIdByManifest.get(boundOt);
            if (platformOt != null) {
                dataSourceService.createDataAccessMethod(contextId, platformOt, created.getId(),
                        "API_CALL", connectionConfig, 300);
            }
        }
    }

    private String serializeProperties(JsonNode objectType) throws IOException {
        List<Map<String, Object>> props = new ArrayList<>();
        for (JsonNode p : array(objectType, "properties")) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("manifestPropertyId", text(p, "id"));
            row.put("name", text(p, "name"));
            row.put("nameEn", text(p, "nameEn"));
            row.put("dataType", text(p, "dataType"));
            if (p.has("required")) {
                row.put("required", p.get("required").asBoolean());
            }
            if (p.has("enumValues")) {
                row.put("enumValues", p.get("enumValues"));
            }
            if (p.has("valueObjectRef")) {
                row.put("valueObjectRef", text(p, "valueObjectRef"));
            }
            if (p.has("sensitive")) {
                row.put("sensitive", p.get("sensitive").asBoolean());
            }
            props.add(row);
        }
        return objectMapper.writeValueAsString(props);
    }

    private static ManifestImportResult toImportResult(ManifestDryRunResult dry) {
        ManifestImportResult r = new ManifestImportResult();
        r.setValid(dry.isValid());
        r.setDraftId(dry.getDraftId());
        r.setImportedCounts(dry.getImportedCounts());
        r.setWarnings(new ArrayList<>(dry.getWarnings()));
        r.setErrors(new ArrayList<>(dry.getErrors()));
        return r;
    }

    private static String mapObjectKind(String manifestKind) {
        if ("aggregate_root".equals(manifestKind)) {
            return "AGGREGATE_ROOT";
        }
        if ("value_object".equals(manifestKind)) {
            return "VALUE_OBJECT";
        }
        return "ENTITY";
    }

    private static String mapRelationKind(String kind) {
        if (kind == null) {
            return "REFERENCE";
        }
        return switch (kind.toLowerCase(Locale.ROOT)) {
            case "composition" -> "COMPOSITION";
            case "aggregation" -> "AGGREGATION";
            default -> "REFERENCE";
        };
    }

    private static DomainTag resolveDomainTag(JsonNode metadata) {
        if (metadata.has("domainTags") && metadata.get("domainTags").isArray()
                && !metadata.get("domainTags").isEmpty()) {
            String tag = metadata.get("domainTags").get(0).asText();
            if ("生产制造".equals(tag)) {
                return DomainTag.MANUFACTURING;
            }
            try {
                return DomainTag.fromCode(tag.toLowerCase(Locale.ROOT).replace(' ', '_'));
            } catch (IllegalArgumentException ignored) {
                // fall through
            }
        }
        return DomainTag.MANUFACTURING;
    }

    private static String slugCode(String preferred, String fallback) {
        String raw = preferred != null && !preferred.isBlank() ? preferred : fallback;
        if (raw == null) {
            return "unknown";
        }
        return raw.replaceAll("[^a-zA-Z0-9]+", "_").toLowerCase(Locale.ROOT).replaceAll("^_|_$", "");
    }

    private static List<String> opsList(JsonNode perm) {
        List<String> ops = new ArrayList<>();
        JsonNode node = perm.get("ops");
        if (node instanceof ArrayNode) {
            node.forEach(n -> ops.add(n.asText()));
        }
        return ops;
    }

    private static List<String> stringList(JsonNode node, String field) {
        List<String> out = new ArrayList<>();
        for (JsonNode n : array(node, field)) {
            out.add(n.asText());
        }
        return out;
    }

    private static List<JsonNode> array(JsonNode parent, String name) {
        List<JsonNode> out = new ArrayList<>();
        if (parent == null || !parent.has(name)) {
            return out;
        }
        JsonNode node = parent.get(name);
        if (node instanceof ArrayNode) {
            node.forEach(out::add);
        }
        return out;
    }

    private static String text(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        return node.get(field).asText();
    }

    private static String textOrDefault(JsonNode node, String field, String def) {
        String v = text(node, field);
        return v != null ? v : def;
    }

    private static int kindOrder(String kind) {
        if ("aggregate_root".equals(kind)) {
            return 0;
        }
        if ("entity".equals(kind)) {
            return 1;
        }
        return 2;
    }
}

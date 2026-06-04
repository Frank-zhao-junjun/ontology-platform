package com.ontology.platform.application.manifest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * US-A01 Round 1: read-only Manifest import dry-run (validate + count, no persistence).
 */
@Service
@RequiredArgsConstructor
public class ManifestImportDryRunService {
    private static final String SUPPORTED_API_VERSION = "ontology.platform/v1";
    private static final String EXPECTED_KIND = "OntologyManifest";
    private static final Pattern SEMVER = Pattern.compile("^\\d+\\.\\d+\\.\\d+(-[\\w.]+)?(\\+[\\w.]+)?$");
    private static final Pattern PAST_TENSE_NAME_EN = Pattern.compile("(?i).+(ed|Created|Released|Closed|Updated|Completed)$");
    private static final Set<String> FORBIDDEN_CREDENTIAL_KEYS = Set.of(
            "password", "apikey", "api_key", "token", "clientsecret", "client_secret");

    private final ManifestYamlParser yamlParser;

    public ManifestDryRunResult dryRunYaml(String yaml) throws IOException {
        return dryRun(yamlParser.parseYaml(yaml));
    }

    public ManifestDryRunResult dryRunJson(String json) throws IOException {
        return dryRun(yamlParser.parseJson(json));
    }

    public ManifestDryRunResult dryRun(JsonNode root) {
        List<ManifestImportError> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        validateEnvelope(root, errors);
        if (!errors.isEmpty()) {
            return fail(errors, warnings);
        }

        JsonNode metadata = root.get("metadata");
        JsonNode spec = root.get("spec");
        JsonNode semantic = spec != null ? spec.get("semantic") : null;

        validateV02(metadata, errors);
        validateV10(root, errors);
        validateV11(root, errors);

        Set<String> objectTypeIds = collectObjectTypeIds(semantic);
        int aggregateRoots = countAggregateRoots(semantic);

        validateV03(aggregateRoots, errors);
        validateV04(semantic, objectTypeIds, errors);
        validateBusinessScenarios(semantic, objectTypeIds, errors);
        validateStateMachines(semantic, objectTypeIds, errors, warnings);

        JsonNode behavior = spec.get("behavior");
        JsonNode events = spec.get("events");
        Set<String> ruleIds = collectIds(behavior, "rules");
        Set<String> eventIds = collectIds(events, "domainEvents");

        validateV05(behavior, objectTypeIds, errors);
        validateV06(behavior, ruleIds, errors);
        validateV07(behavior, eventIds, errors);
        validateV08(events, warnings);
        validateV09(semantic, errors);

        ManifestImportedCounts counts = buildCounts(root, semantic, spec, behavior, events);
        if (counts.getPropertyFieldKeys() == 0 && counts.getProperties() > 0) {
            warnings.add("PARSE: no properties[].nameEn found; platform field keys may be empty");
        }

        if (!errors.isEmpty()) {
            return fail(errors, warnings);
        }

        String ontologyId = text(metadata, "id");
        String version = text(metadata, "version");
        String draftId = "dry-run:" + ontologyId + ":" + version;

        warnings.add("DRY_RUN: no database write; draftId is synthetic only");
        if (spec.has("governance")) {
            warnings.add("IMPORT_SCOPE: governance parsed for counts only (roles/fieldPermissions/agentPolicies not persisted in Round 1)");
        }
        if (behavior != null && behavior.has("metrics")) {
            warnings.add("IMPORT_SCOPE: behavior.metrics counted as metadata only (US-B05 not implemented)");
        }

        return ManifestDryRunResult.builder()
                .valid(true)
                .draftId(draftId)
                .importedCounts(counts)
                .warnings(warnings)
                .errors(List.of())
                .build();
    }

    private void validateEnvelope(JsonNode root, List<ManifestImportError> errors) {
        String apiVersion = text(root, "apiVersion");
        if (!SUPPORTED_API_VERSION.equals(apiVersion)) {
            errors.add(err("V01", "OntologyManifest", null, "apiVersion",
                    "Unsupported apiVersion: " + apiVersion + "; expected " + SUPPORTED_API_VERSION));
        }
        String kind = text(root, "kind");
        if (!EXPECTED_KIND.equals(kind)) {
            errors.add(err("STRUCTURE", "OntologyManifest", null, "kind",
                    "Expected kind OntologyManifest, got: " + kind));
        }
        if (root.get("metadata") == null || !root.get("metadata").isObject()) {
            errors.add(err("STRUCTURE", "OntologyManifest", null, "metadata", "metadata is required"));
        }
        if (root.get("spec") == null || !root.get("spec").isObject()) {
            errors.add(err("STRUCTURE", "OntologyManifest", null, "spec", "spec is required"));
        }
    }

    private void validateV02(JsonNode metadata, List<ManifestImportError> errors) {
        String version = text(metadata, "version");
        if (version == null || !SEMVER.matcher(version).matches()) {
            errors.add(err("V02", "metadata", text(metadata, "id"), "version",
                    "metadata.version must be semver, got: " + version));
        }
    }

    private void validateV03(int aggregateRoots, List<ManifestImportError> errors) {
        if (aggregateRoots < 1) {
            errors.add(err("V03", "semantic", null, "objectTypes",
                    "At least one objectTypes[] entry with kind=aggregate_root is required"));
        }
    }

    private void validateV04(JsonNode semantic, Set<String> objectTypeIds, List<ManifestImportError> errors) {
        for (JsonNode ot : array(semantic, "objectTypes")) {
            if (!"entity".equals(text(ot, "kind"))) {
                continue;
            }
            String id = text(ot, "id");
            String arId = text(ot, "aggregateRootId");
            if (arId == null || arId.isBlank()) {
                errors.add(err("V04", "objectType", id, "aggregateRootId",
                        "entity requires aggregateRootId"));
            } else if (!objectTypeIds.contains(arId)) {
                errors.add(err("V04", "objectType", id, "aggregateRootId",
                        "aggregateRootId not found: " + arId));
            }
        }
    }

    private void validateBusinessScenarios(JsonNode semantic, Set<String> objectTypeIds,
                                           List<ManifestImportError> errors) {
        for (JsonNode scenario : array(semantic, "businessScenarios")) {
            String scenarioId = text(scenario, "id");
            for (JsonNode ref : array(scenario, "applicableObjectTypeIds")) {
                String otId = ref.asText();
                if (!objectTypeIds.contains(otId)) {
                    errors.add(err("REF", "businessScenario", scenarioId, "applicableObjectTypeIds",
                            "Unknown objectTypeId: " + otId));
                }
            }
        }
    }

    private void validateStateMachines(JsonNode semantic, Set<String> objectTypeIds,
                                       List<ManifestImportError> errors, List<String> warnings) {
        for (JsonNode sm : array(semantic, "stateMachines")) {
            String smId = text(sm, "id");
            String otId = text(sm, "objectTypeId");
            if (otId == null || !objectTypeIds.contains(otId)) {
                errors.add(err("REF", "stateMachine", smId, "objectTypeId",
                        "Unknown objectTypeId: " + otId));
            }
            String statusField = text(sm, "statusField");
            if (statusField != null) {
                warnings.add("FIELD_KEY: stateMachine " + smId + " uses statusField=" + statusField
                        + " (platform maps manifest properties[].nameEn for fields)");
            }
        }
    }

    private void validateV05(JsonNode behavior, Set<String> objectTypeIds, List<ManifestImportError> errors) {
        if (behavior == null) {
            return;
        }
        for (JsonNode action : array(behavior, "actions")) {
            String id = text(action, "id");
            String arId = text(action, "aggregateRootId");
            if (arId == null || !objectTypeIds.contains(arId)) {
                errors.add(err("V05", "action", id, "aggregateRootId",
                        "aggregateRootId not found: " + arId));
            }
        }
    }

    private void validateV06(JsonNode behavior, Set<String> ruleIds, List<ManifestImportError> errors) {
        if (behavior == null) {
            return;
        }
        for (JsonNode action : array(behavior, "actions")) {
            String actionId = text(action, "id");
            for (JsonNode ruleRef : array(action, "preRuleIds")) {
                String ruleId = ruleRef.asText();
                if (!ruleIds.contains(ruleId)) {
                    errors.add(err("V06", "action", actionId, "preRuleIds",
                            "Unknown rule id: " + ruleId));
                }
            }
        }
    }

    private void validateV07(JsonNode behavior, Set<String> eventIds, List<ManifestImportError> errors) {
        if (behavior == null) {
            return;
        }
        for (JsonNode action : array(behavior, "actions")) {
            String actionId = text(action, "id");
            for (JsonNode evtRef : array(action, "publishesEventIds")) {
                String evtId = evtRef.asText();
                if (!eventIds.contains(evtId)) {
                    errors.add(err("V07", "action", actionId, "publishesEventIds",
                            "Unknown domainEvent id: " + evtId));
                }
            }
        }
    }

    private void validateV08(JsonNode events, List<String> warnings) {
        if (events == null) {
            return;
        }
        for (JsonNode evt : array(events, "domainEvents")) {
            String nameEn = text(evt, "nameEn");
            if (nameEn != null && !PAST_TENSE_NAME_EN.matcher(nameEn).matches()) {
                warnings.add("V08: domainEvent " + text(evt, "id") + " nameEn '" + nameEn
                        + "' is not past-tense (warning only)");
            }
        }
    }

    private void validateV09(JsonNode semantic, List<ManifestImportError> errors) {
        for (JsonNode sm : array(semantic, "stateMachines")) {
            String smId = text(sm, "id");
            int initialCount = 0;
            for (JsonNode state : array(sm, "states")) {
                if (state.has("isInitial") && state.get("isInitial").asBoolean(false)) {
                    initialCount++;
                }
            }
            if (initialCount != 1) {
                errors.add(err("V09", "stateMachine", smId, "states",
                        "Exactly one state must have isInitial=true, found: " + initialCount));
            }
        }
    }

    private void validateV10(JsonNode root, List<ManifestImportError> errors) {
        scanCredentials(root, "", errors);
    }

    private void scanCredentials(JsonNode node, String path, List<ManifestImportError> errors) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> e = fields.next();
                String key = e.getKey();
                String keyLower = key.toLowerCase(Locale.ROOT);
                if (FORBIDDEN_CREDENTIAL_KEYS.contains(keyLower)
                        && !keyLower.endsWith("secretref")
                        && !key.endsWith("SecretRef")) {
                    errors.add(err("V10", "OntologyManifest", null, key,
                            "Plaintext credential field forbidden at " + path + key));
                }
                scanCredentials(e.getValue(), path + key + ".", errors);
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                scanCredentials(child, path, errors);
            }
        }
    }

    private void validateV11(JsonNode root, List<ManifestImportError> errors) {
        Set<String> seen = new HashSet<>();
        collectDuplicateIds(root, seen, errors);
    }

    private void collectDuplicateIds(JsonNode node, Set<String> seen, List<ManifestImportError> errors) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            if (node.has("id") && node.get("id").isTextual()) {
                String id = node.get("id").asText();
                if (!seen.add(id)) {
                    errors.add(err("V11", "element", id, "id", "Duplicate id in manifest: " + id));
                }
            }
            node.fields().forEachRemaining(e -> collectDuplicateIds(e.getValue(), seen, errors));
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                collectDuplicateIds(child, seen, errors);
            }
        }
    }

    private ManifestImportedCounts buildCounts(JsonNode root, JsonNode semantic, JsonNode spec,
                                               JsonNode behavior, JsonNode events) {
        int properties = 0;
        Set<String> fieldKeys = new LinkedHashSet<>();

        for (JsonNode ot : array(semantic, "objectTypes")) {
            properties += countProperties(ot, fieldKeys);
        }
        for (JsonNode vo : array(semantic, "valueObjects")) {
            properties += countProperties(vo, fieldKeys);
        }

        JsonNode governance = spec.get("governance");
        return ManifestImportedCounts.builder()
                .boundedContext(semantic != null && semantic.has("boundedContext") ? 1 : 0)
                .businessScenarios(array(semantic, "businessScenarios").size())
                .objectTypes(array(semantic, "objectTypes").size())
                .properties(properties)
                .propertyFieldKeys(fieldKeys.size())
                .relations(countAllRelations(semantic))
                .stateMachines(array(semantic, "stateMachines").size())
                .actions(behavior != null ? array(behavior, "actions").size() : 0)
                .rules(behavior != null ? array(behavior, "rules").size() : 0)
                .domainEvents(events != null ? array(events, "domainEvents").size() : 0)
                .roles(governance != null ? array(governance, "roles").size() : 0)
                .fieldPermissions(governance != null ? array(governance, "fieldPermissions").size() : 0)
                .agentPolicies(governance != null ? array(governance, "agentPolicies").size() : 0)
                .dataSources(array(spec, "dataSources").size())
                .build();
    }

    private int countProperties(JsonNode container, Set<String> fieldKeys) {
        int n = 0;
        for (JsonNode prop : array(container, "properties")) {
            n++;
            String nameEn = text(prop, "nameEn");
            if (nameEn != null && !nameEn.isBlank()) {
                fieldKeys.add(nameEn);
            }
        }
        return n;
    }

    private int countAllRelations(JsonNode semantic) {
        int n = 0;
        for (JsonNode ot : array(semantic, "objectTypes")) {
            n += array(ot, "relations").size();
        }
        return n;
    }

    private Set<String> collectObjectTypeIds(JsonNode semantic) {
        Set<String> ids = new HashSet<>();
        for (JsonNode ot : array(semantic, "objectTypes")) {
            String id = text(ot, "id");
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private int countAggregateRoots(JsonNode semantic) {
        int n = 0;
        for (JsonNode ot : array(semantic, "objectTypes")) {
            if ("aggregate_root".equals(text(ot, "kind"))) {
                n++;
            }
        }
        return n;
    }

    private Set<String> collectIds(JsonNode parent, String arrayName) {
        Set<String> ids = new HashSet<>();
        if (parent == null) {
            return ids;
        }
        for (JsonNode item : array(parent, arrayName)) {
            String id = text(item, "id");
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private List<JsonNode> array(JsonNode parent, String name) {
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

    private String text(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        return node.get(field).asText();
    }

    private ManifestImportError err(String code, String elementType, String id, String field, String message) {
        return ManifestImportError.builder()
                .code(code)
                .elementType(elementType)
                .id(id)
                .field(field)
                .message(message)
                .build();
    }

    private ManifestDryRunResult fail(List<ManifestImportError> errors, List<String> warnings) {
        return ManifestDryRunResult.builder()
                .valid(false)
                .draftId(null)
                .importedCounts(new ManifestImportedCounts())
                .warnings(warnings)
                .errors(errors)
                .build();
    }
}

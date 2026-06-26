package com.ontology.platform.infrastructure.validation;

import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument.*;
import com.ontology.platform.domain.service.validation.ValidationContext;
import com.ontology.platform.domain.service.validation.ValidationIssue;
import com.ontology.platform.domain.service.validation.ValidationPlugin;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Structural Validator — V01-V11 rules adapted from v1 {@code ManifestValidator}
 * to the v2 {@code OntologyExchangeDocument}.
 *
 * <p>Validates document-level structure: apiVersion, semver, aggregate roots,
 * cross-references, event tense, credentials exposure, and ID uniqueness.</p>
 */
@Component
public class StructuralValidator implements ValidationPlugin {

    private static final Pattern SEMVER = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");
    private static final Pattern CREDENTIAL_PATTERN = Pattern.compile(
            "(?i)(password|apikey|secret|token|access_key|private_key)");

    @Override
    public String pluginCode() { return "STR"; }

    @Override
    public String pluginName() { return "Structural Validator (V01-V11)"; }

    @Override
    public List<ValidationIssue> validate(ValidationContext ctx) {
        List<ValidationIssue> issues = new ArrayList<>();
        OntologyExchangeDocument doc = ctx.getDocument();
        if (doc == null) {
            issues.add(issue("STR-00", "error", "document", null, null,
                    "Document is null"));
            return issues;
        }

        v01_apiVersion(doc, issues);
        if (doc.getMetadata() != null) {
            v02_semver(doc.getMetadata(), issues);
        }
        if (doc.getSpec() != null && doc.getSpec().getProject() != null) {
            OntologyProject project = doc.getSpec().getProject();
            v03_aggregateRoots(project, issues);
            v04_entityRefs(project, issues);
            v05_actionRefs(project, issues);
            v06_ruleRefs(project, issues);
            v07_eventRefs(project, issues);
            v08_eventNameTense(project, issues);
            v09_stateMachineInit(project, issues);
            v10_noCredentials(project, issues);
            v11_uniqueIds(project, issues);
        }

        return issues;
    }

    // ═══════════════════════════════════════════════════════════════
    // V01: apiVersion must be ontology.platform/v2
    // ═══════════════════════════════════════════════════════════════

    private void v01_apiVersion(OntologyExchangeDocument doc, List<ValidationIssue> issues) {
        String v = doc.getApiVersion();
        if (v == null || !"ontology.platform/v2".equals(v)) {
            issues.add(issue("STR-01", "error", "document", null, "apiVersion",
                    "Unsupported apiVersion: " + v + ", expected 'ontology.platform/v2'"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // V02: metadata.version must be semver
    // ═══════════════════════════════════════════════════════════════

    private void v02_semver(Metadata metadata, List<ValidationIssue> issues) {
        String version = metadata.getVersion();
        if (version == null || !SEMVER.matcher(version).matches()) {
            issues.add(issue("STR-02", "error", "metadata", null, "version",
                    "metadata.version must match semver pattern (e.g. 1.0.0), got: " + version));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // V03: At least one aggregate_root entity
    // ═══════════════════════════════════════════════════════════════

    private void v03_aggregateRoots(OntologyProject project, List<ValidationIssue> issues) {
        DataModel dm = project.getDataModel();
        if (dm == null || dm.getEntities() == null || dm.getEntities().isEmpty()) {
            issues.add(issue("STR-03", "error", "dataModel", null, null,
                    "Data model must contain at least one entity with entityRole='aggregate_root'"));
            return;
        }
        long rootCount = dm.getEntities().stream()
                .filter(e -> "aggregate_root".equals(e.getEntityRole()))
                .count();
        if (rootCount == 0) {
            issues.add(issue("STR-03", "error", "dataModel", null, null,
                    "At least one entity must have entityRole='aggregate_root'"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // V04: child_entity parentAggregateId must reference existing entity
    // ═══════════════════════════════════════════════════════════════

    private void v04_entityRefs(OntologyProject project, List<ValidationIssue> issues) {
        DataModel dm = project.getDataModel();
        if (dm == null || dm.getEntities() == null) return;

        Set<String> entityIds = dm.getEntities().stream()
                .map(Entity::getId).filter(Objects::nonNull).collect(Collectors.toSet());

        for (Entity e : dm.getEntities()) {
            if ("child_entity".equals(e.getEntityRole())) {
                String parentId = e.getParentAggregateId();
                if (parentId == null || parentId.isBlank()) {
                    issues.add(issue("STR-04", "error", "entity", e.getId(),
                            "parentAggregateId",
                            "child_entity must have parentAggregateId set"));
                } else if (!entityIds.contains(parentId)) {
                    issues.add(issue("STR-04", "error", "entity", e.getId(),
                            "parentAggregateId",
                            "parentAggregateId '" + parentId + "' does not reference an existing entity"));
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // V05: Action targetEntityId must reference existing entity
    // ═══════════════════════════════════════════════════════════════

    private void v05_actionRefs(OntologyProject project, List<ValidationIssue> issues) {
        DataModel dm = project.getDataModel();
        BehaviorModel bm = project.getBehaviorModel();
        if (dm == null || bm == null || bm.getActions() == null) return;

        Set<String> entityIds = dm.getEntities().stream()
                .map(Entity::getId).filter(Objects::nonNull).collect(Collectors.toSet());

        for (Action action : bm.getActions()) {
            String targetId = action.getTargetEntityId();
            if (targetId != null && !targetId.isBlank() && !entityIds.contains(targetId)) {
                issues.add(issue("STR-05", "error", "action", action.getId(),
                        "targetEntityId",
                        "Action targetEntityId '" + targetId + "' does not reference an existing entity"));
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // V06: Action preConditions must reference existing rules
    // ═══════════════════════════════════════════════════════════════

    private void v06_ruleRefs(OntologyProject project, List<ValidationIssue> issues) {
        BehaviorModel bm = project.getBehaviorModel();
        RuleModel rm = project.getRuleModel();
        if (bm == null || bm.getActions() == null || rm == null || rm.getRules() == null) return;

        Set<String> ruleIds = rm.getRules().stream()
                .map(Rule::getId).filter(Objects::nonNull).collect(Collectors.toSet());

        for (Action action : bm.getActions()) {
            if (action.getPreConditions() != null) {
                for (String ruleId : action.getPreConditions()) {
                    if (ruleId != null && !ruleId.isBlank() && !ruleIds.contains(ruleId)) {
                        issues.add(issue("STR-06", "error", "action", action.getId(),
                                "preConditions",
                                "Referenced rule '" + ruleId + "' does not exist"));
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // V07: Event references from actions/states must exist
    // ═══════════════════════════════════════════════════════════════

    private void v07_eventRefs(OntologyProject project, List<ValidationIssue> issues) {
        BehaviorModel bm = project.getBehaviorModel();
        EventModel em = project.getEventModel();
        if (bm == null || em == null || em.getEvents() == null) return;

        Set<String> eventIds = em.getEvents().stream()
                .map(EventDefinition::getId).filter(Objects::nonNull).collect(Collectors.toSet());

        // Check transitions publishEventId refs
        if (bm.getStateMachines() != null) {
            for (StateMachine sm : bm.getStateMachines()) {
                if (sm.getTransitions() != null) {
                    for (Transition t : sm.getTransitions()) {
                        String eventId = t.getPublishEventId();
                        if (eventId != null && !eventId.isBlank() && !eventIds.contains(eventId)) {
                            issues.add(issue("STR-07", "warning", "transition", t.getId(),
                                    "publishEventId",
                                    "Referenced event '" + eventId + "' does not exist in eventModel"));
                        }
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // V08: Event nameEn should end with past-tense suffix (ed/d)
    // ═══════════════════════════════════════════════════════════════

    private void v08_eventNameTense(OntologyProject project, List<ValidationIssue> issues) {
        EventModel em = project.getEventModel();
        if (em == null || em.getEvents() == null) return;

        for (EventDefinition event : em.getEvents()) {
            String nameEn = event.getNameEn();
            if (nameEn != null && !nameEn.isBlank()) {
                String lower = nameEn.toLowerCase();
                if (!lower.endsWith("ed") && !lower.endsWith("d")) {
                    issues.add(issue("STR-08", "warning", "event", event.getId(),
                            "nameEn",
                            "Event nameEn '" + nameEn + "' should use past tense (end with -ed/-d)"));
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // V09: Each state machine must have exactly one initial state
    // ═══════════════════════════════════════════════════════════════

    private void v09_stateMachineInit(OntologyProject project, List<ValidationIssue> issues) {
        BehaviorModel bm = project.getBehaviorModel();
        if (bm == null || bm.getStateMachines() == null) return;

        for (StateMachine sm : bm.getStateMachines()) {
            if (sm.getStates() == null || sm.getStates().isEmpty()) continue;

            long initCount = sm.getStates().stream()
                    .filter(s -> Boolean.TRUE.equals(s.getIsInitial()))
                    .count();
            if (initCount == 0) {
                issues.add(issue("STR-09", "error", "stateMachine", sm.getId(),
                        "states",
                        "StateMachine must have exactly one state with isInitial=true, found 0"));
            } else if (initCount > 1) {
                issues.add(issue("STR-09", "error", "stateMachine", sm.getId(),
                        "states",
                        "StateMachine must have exactly one state with isInitial=true, found " + initCount));
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // V10: DataSources must not expose plaintext credentials in config
    // ═══════════════════════════════════════════════════════════════

    private void v10_noCredentials(OntologyProject project, List<ValidationIssue> issues) {
        DataSourcesModel dsm = project.getDataSourcesModel();
        if (dsm == null || dsm.getSources() == null) return;

        for (DataSource ds : dsm.getSources()) {
            String name = ds.getName();
            // Check common credential field names in the DataSource itself
            if (name != null && CREDENTIAL_PATTERN.matcher(name).find()) {
                issues.add(issue("STR-10", "warning", "dataSource", ds.getId(),
                        "name",
                        "DataSource name may contain credential-like pattern"));
            }
            // Check ApiDef.authSecretRef for credential patterns
            OntologyExchangeDocument.ApiDef api = ds.getApi();
            if (api != null && api.getAuthSecretRef() != null) {
                issues.add(issue("STR-10", "warning", "dataSource", ds.getId(),
                        "api.authSecretRef",
                        "DataSource uses authSecretRef; ensure no plaintext credentials in manifest"));
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // V11: All IDs across sections must be unique
    // ═══════════════════════════════════════════════════════════════

    private void v11_uniqueIds(OntologyProject project, List<ValidationIssue> issues) {
        Map<String, List<String>> idSources = new LinkedHashMap<>();

        collectIds(idSources, "entity", entityIds(project));
        collectIds(idSources, "attribute", attributeIds(project));
        collectIds(idSources, "action", actionIds(project));
        collectIds(idSources, "rule", ruleIds(project));
        collectIds(idSources, "event", eventIds(project));
        collectIds(idSources, "state", stateIds(project));
        collectIds(idSources, "transition", transitionIds(project));
        collectIds(idSources, "role", roleIds(project));
        collectIds(idSources, "dataSource", dataSourceIds(project));

        // Check for duplicate IDs across all sections
        Map<String, String> seen = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : idSources.entrySet()) {
            String source = entry.getKey();
            for (String id : entry.getValue()) {
                if (id == null || id.isBlank()) continue;
                if (seen.containsKey(id)) {
                    issues.add(issue("STR-11", "error", source, id, "id",
                            "Duplicate ID '" + id + "' also found in " + seen.get(id)));
                } else {
                    seen.put(id, source);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Helper: build ValidationIssue
    // ═══════════════════════════════════════════════════════════════

    private ValidationIssue issue(String code, String severity, String elementType,
                                   String elementId, String field, String message) {
        return ValidationIssue.builder()
                .code(code).severity(severity).elementType(elementType)
                .elementId(elementId).field(field).message(message)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // ID collectors for V11
    // ═══════════════════════════════════════════════════════════════

    private void collectIds(Map<String, List<String>> map, String key, List<String> ids) {
        if (ids != null && !ids.isEmpty()) map.put(key, ids);
    }

    private List<String> entityIds(OntologyProject p) {
        var dm = p.getDataModel();
        if (dm == null || dm.getEntities() == null) return List.of();
        return dm.getEntities().stream().map(Entity::getId).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<String> attributeIds(OntologyProject p) {
        var dm = p.getDataModel();
        if (dm == null || dm.getEntities() == null) return List.of();
        return dm.getEntities().stream()
                .flatMap(e -> e.getAttributes() != null ? e.getAttributes().stream() : java.util.stream.Stream.empty())
                .map(Attribute::getId).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<String> actionIds(OntologyProject p) {
        var bm = p.getBehaviorModel();
        if (bm == null || bm.getActions() == null) return List.of();
        return bm.getActions().stream().map(Action::getId).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<String> ruleIds(OntologyProject p) {
        var rm = p.getRuleModel();
        if (rm == null || rm.getRules() == null) return List.of();
        return rm.getRules().stream().map(Rule::getId).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<String> eventIds(OntologyProject p) {
        var em = p.getEventModel();
        if (em == null || em.getEvents() == null) return List.of();
        return em.getEvents().stream().map(EventDefinition::getId).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<String> stateIds(OntologyProject p) {
        var bm = p.getBehaviorModel();
        if (bm == null || bm.getStateMachines() == null) return List.of();
        return bm.getStateMachines().stream()
                .flatMap(sm -> sm.getStates() != null ? sm.getStates().stream() : java.util.stream.Stream.empty())
                .map(State::getId).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<String> transitionIds(OntologyProject p) {
        var bm = p.getBehaviorModel();
        if (bm == null || bm.getStateMachines() == null) return List.of();
        return bm.getStateMachines().stream()
                .flatMap(sm -> sm.getTransitions() != null ? sm.getTransitions().stream() : java.util.stream.Stream.empty())
                .map(Transition::getId).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<String> roleIds(OntologyProject p) {
        var gm = p.getGovernanceModel();
        if (gm == null || gm.getRoles() == null) return List.of();
        return gm.getRoles().stream().map(GovernanceRole::getId).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<String> dataSourceIds(OntologyProject p) {
        var dsm = p.getDataSourcesModel();
        if (dsm == null || dsm.getSources() == null) return List.of();
        return dsm.getSources().stream().map(DataSource::getId).filter(Objects::nonNull).collect(Collectors.toList());
    }
}

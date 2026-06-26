package com.ontology.platform.infrastructure.validation;

import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument.*;
import com.ontology.platform.domain.service.validation.ValidationContext;
import com.ontology.platform.domain.service.validation.ValidationIssue;
import com.ontology.platform.domain.service.validation.ValidationPlugin;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Manifest structure validator — V01-V11 rules adapted for v2 OntologyExchangeDocument.
 *
 * <p>Replaces the legacy {@code ManifestValidator} (which operates on v1 ManifestDocument)
 * with rules that directly validate the v2 exchange format. Registered as a Spring
 * {@link Component} so {@code ExchangeValidationService} auto-discovers it.</p>
 */
@Component
public class ManifestValidatorPlugin implements ValidationPlugin {

    private static final Pattern SEMVER = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");

    @Override
    public String pluginCode() { return "V"; }

    @Override
    public String pluginName() { return "Manifest Structure Validator (V01-V11)"; }

    @Override
    public List<ValidationIssue> validate(ValidationContext ctx) {
        List<ValidationIssue> issues = new ArrayList<>();
        OntologyExchangeDocument doc = ctx.getDocument();
        if (doc == null) {
            issues.add(ValidationIssue.builder()
                    .code("V00").severity("error").elementType("document")
                    .message("Exchange document is null").build());
            return issues;
        }

        v01_apiVersion(doc, issues);
        v02_semver(doc, issues);
        v03_aggregateRoots(doc, issues);
        v04_entityParentRefs(doc, issues);
        v05_actionRefs(doc, issues);
        v07_eventRefs(doc, issues);
        v08_eventNameTense(doc, issues);
        v09_stateMachineInit(doc, issues);
        v10_noCredentials(doc, issues);
        v11_uniqueIds(doc, issues);
        return issues;
    }

    // ── V01: apiVersion must be ontology.platform/v2 ──

    private void v01_apiVersion(OntologyExchangeDocument doc, List<ValidationIssue> issues) {
        String v = doc.getApiVersion();
        if (v == null || !"ontology.platform/v2".equals(v)) {
            issues.add(ValidationIssue.builder()
                    .code("V01").severity("error").elementType("apiVersion")
                    .message("Unsupported apiVersion: " + v + ", expected ontology.platform/v2").build());
        }
    }

    // ── V02: metadata.version must be semver ──

    private void v02_semver(OntologyExchangeDocument doc, List<ValidationIssue> issues) {
        Metadata meta = doc.getMetadata();
        if (meta == null || meta.getVersion() == null || !SEMVER.matcher(meta.getVersion()).matches()) {
            issues.add(ValidationIssue.builder()
                    .code("V02").severity("error").elementType("metadata")
                    .elementId(meta != null ? meta.getId() : null).field("version")
                    .message("Invalid semver: " + (meta != null ? meta.getVersion() : "null")).build());
        }
    }

    // ── V03: At least 1 aggregate_root entity ──

    private void v03_aggregateRoots(OntologyExchangeDocument doc, List<ValidationIssue> issues) {
        List<Entity> entities = getEntities(doc);
        long count = entities.stream()
                .filter(e -> "aggregate_root".equals(e.getEntityRole())).count();
        if (count == 0) {
            issues.add(ValidationIssue.builder()
                    .code("V03").severity("error").elementType("dataModel").field("entities")
                    .message("At least 1 aggregate_root entity required").build());
        }
    }

    // ── V04: child_entity must reference valid parentAggregateId ──

    private void v04_entityParentRefs(OntologyExchangeDocument doc, List<ValidationIssue> issues) {
        List<Entity> entities = getEntities(doc);
        Set<String> entityIds = new HashSet<>();
        for (Entity e : entities) {
            if (e.getId() != null) entityIds.add(e.getId());
        }
        for (Entity e : entities) {
            String ref = e.getParentAggregateId();
            if (ref != null && !entityIds.contains(ref)) {
                issues.add(ValidationIssue.builder()
                        .code("V04").severity("error").elementType("entity")
                        .elementId(e.getId()).field("parentAggregateId")
                        .message("parentAggregateId ref not found: " + ref).build());
            }
        }
    }

    // ── V05: Action targetEntityId must reference valid entity ──

    private void v05_actionRefs(OntologyExchangeDocument doc, List<ValidationIssue> issues) {
        List<Entity> entities = getEntities(doc);
        Set<String> entityIds = new HashSet<>();
        for (Entity e : entities) {
            if (e.getId() != null) entityIds.add(e.getId());
        }
        List<Action> actions = getActions(doc);
        for (Action a : actions) {
            if (a.getTargetEntityId() != null && !entityIds.contains(a.getTargetEntityId())) {
                issues.add(ValidationIssue.builder()
                        .code("V05").severity("error").elementType("action")
                        .elementId(a.getId()).field("targetEntityId")
                        .message("targetEntityId ref not found: " + a.getTargetEntityId()).build());
            }
        }
    }

    // ── V07: Event refs from EPC nodes must be valid (adapted: cross-model ref check) ──

    private void v07_eventRefs(OntologyExchangeDocument doc, List<ValidationIssue> issues) {
        // In v2, EPC nodes reference events via refId. Check event refs in EPC model.
        EpcModel epc = getEpcModel(doc);
        if (epc == null || epc.getChains() == null) return;

        Set<String> eventIds = getEventIds(doc);
        for (EpcChain chain : epc.getChains()) {
            if (chain.getNodes() == null) continue;
            for (EpcNode node : chain.getNodes()) {
                if ("EVENT".equals(node.getNodeType()) && node.getRefId() != null
                        && !eventIds.contains(node.getRefId())) {
                    issues.add(ValidationIssue.builder()
                            .code("V07").severity("error").elementType("epcNode")
                            .elementId(node.getId()).field("refId")
                            .message("EPC node references unknown event: " + node.getRefId()).build());
                }
            }
        }
    }

    // ── V08: Event nameEn should be past tense ──

    private void v08_eventNameTense(OntologyExchangeDocument doc, List<ValidationIssue> issues) {
        List<EventDefinition> events = getEvents(doc);
        for (EventDefinition e : events) {
            String name = e.getNameEn();
            if (name != null && !name.endsWith("ed") && !name.endsWith("d")) {
                issues.add(ValidationIssue.builder()
                        .code("V08").severity("warning").elementType("event")
                        .elementId(e.getId()).field("nameEn")
                        .message("Event name may not be past tense: " + name).build());
            }
        }
    }

    // ── V09: Each stateMachine must have exactly 1 initial state ──

    private void v09_stateMachineInit(OntologyExchangeDocument doc, List<ValidationIssue> issues) {
        List<StateMachine> sms = getStateMachines(doc);
        for (StateMachine sm : sms) {
            if (sm.getStates() == null) continue;
            long init = sm.getStates().stream().filter(s -> Boolean.TRUE.equals(s.getIsInitial())).count();
            if (init != 1) {
                issues.add(ValidationIssue.builder()
                        .code("V09").severity("error").elementType("stateMachine")
                        .elementId(sm.getId()).field("states")
                        .message("Exactly 1 isInitial state required, found " + init).build());
            }
        }
    }

    // ── V10: No plaintext credentials in dataSource config ──

    private void v10_noCredentials(OntologyExchangeDocument doc, List<ValidationIssue> issues) {
        List<DataSource> sources = getDataSources(doc);
        for (DataSource ds : sources) {
            // v2 DataSource has api.authSecretRef (reference-based auth), not connectionConfig
            // V10 is a warning: prefer authSecretRef over inline credentials
            if (ds.getApi() != null && ds.getApi().getAuthSecretRef() == null) {
                issues.add(ValidationIssue.builder()
                        .code("V10").severity("warning").elementType("dataSource")
                        .elementId(ds.getId()).field("api.authSecretRef")
                        .message("DataSource should use authSecretRef (no inline credentials)").build());
            }
        }
    }

    // ── V11: No duplicate IDs across core elements ──

    private void v11_uniqueIds(OntologyExchangeDocument doc, List<ValidationIssue> issues) {
        Set<String> seen = new HashSet<>();
        for (Entity e : getEntities(doc)) {
            if (!seen.add(e.getId()))
                issues.add(ValidationIssue.builder().code("V11").severity("error")
                        .elementType("entity").elementId(e.getId()).field("id")
                        .message("Duplicate id").build());
        }
        for (Action a : getActions(doc)) {
            if (!seen.add(a.getId()))
                issues.add(ValidationIssue.builder().code("V11").severity("error")
                        .elementType("action").elementId(a.getId()).field("id")
                        .message("Duplicate id").build());
        }
        for (EventDefinition ev : getEvents(doc)) {
            if (!seen.add(ev.getId()))
                issues.add(ValidationIssue.builder().code("V11").severity("error")
                        .elementType("event").elementId(ev.getId()).field("id")
                        .message("Duplicate id").build());
        }
        for (Rule r : getRules(doc)) {
            if (!seen.add(r.getId()))
                issues.add(ValidationIssue.builder().code("V11").severity("error")
                        .elementType("rule").elementId(r.getId()).field("id")
                        .message("Duplicate id").build());
        }
        for (StateMachine sm : getStateMachines(doc)) {
            if (!seen.add(sm.getId()))
                issues.add(ValidationIssue.builder().code("V11").severity("error")
                        .elementType("stateMachine").elementId(sm.getId()).field("id")
                        .message("Duplicate id").build());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Helper accessors (null-safe)
    // ═══════════════════════════════════════════════════════════════

    private OntologyProject project(OntologyExchangeDocument doc) {
        return doc.getSpec() != null ? doc.getSpec().getProject() : null;
    }

    private List<Entity> getEntities(OntologyExchangeDocument doc) {
        OntologyProject p = project(doc);
        if (p == null || p.getDataModel() == null || p.getDataModel().getEntities() == null)
            return List.of();
        return p.getDataModel().getEntities();
    }

    private List<Action> getActions(OntologyExchangeDocument doc) {
        OntologyProject p = project(doc);
        if (p == null || p.getBehaviorModel() == null || p.getBehaviorModel().getActions() == null)
            return List.of();
        return p.getBehaviorModel().getActions();
    }

    private List<Rule> getRules(OntologyExchangeDocument doc) {
        OntologyProject p = project(doc);
        if (p == null || p.getRuleModel() == null || p.getRuleModel().getRules() == null)
            return List.of();
        return p.getRuleModel().getRules();
    }

    private List<EventDefinition> getEvents(OntologyExchangeDocument doc) {
        OntologyProject p = project(doc);
        if (p == null || p.getEventModel() == null || p.getEventModel().getEvents() == null)
            return List.of();
        return p.getEventModel().getEvents();
    }

    private Set<String> getEventIds(OntologyExchangeDocument doc) {
        Set<String> ids = new HashSet<>();
        for (EventDefinition e : getEvents(doc)) {
            if (e.getId() != null) ids.add(e.getId());
        }
        return ids;
    }

    private List<StateMachine> getStateMachines(OntologyExchangeDocument doc) {
        OntologyProject p = project(doc);
        if (p == null || p.getBehaviorModel() == null || p.getBehaviorModel().getStateMachines() == null)
            return List.of();
        return p.getBehaviorModel().getStateMachines();
    }

    private List<DataSource> getDataSources(OntologyExchangeDocument doc) {
        OntologyProject p = project(doc);
        if (p == null || p.getDataSourcesModel() == null || p.getDataSourcesModel().getSources() == null)
            return List.of();
        return p.getDataSourcesModel().getSources();
    }

    private EpcModel getEpcModel(OntologyExchangeDocument doc) {
        OntologyProject p = project(doc);
        return p != null ? p.getEpcModel() : null;
    }
}

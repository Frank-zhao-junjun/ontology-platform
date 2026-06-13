package com.ontology.platform.application.service.manifest;

import com.ontology.platform.domain.vo.manifest.*;
import java.util.*;
import java.util.regex.Pattern;

public class ManifestValidator {

    public ManifestValidationResult validate(ManifestDocument doc) {
        ManifestValidationResult result = ManifestValidationResult.success();
        if (doc == null) { result.addError(ValidationError.of("V00", "ROOT", null, null, "Manifest is null")); return result; }
        v01_apiVersion(doc, result);
        v02_semver(doc, result);
        v03_aggregateRoots(doc, result);
        v04_entityRefs(doc, result);
        v05_actionRefs(doc, result);
        v06_ruleRefs(doc, result);
        v07_eventRefs(doc, result);
        v08_eventNameTense(doc, result);
        v09_stateMachineInit(doc, result);
        v10_noCredentials(doc, result);
        v11_uniqueIds(doc, result);
        return result;
    }

    private void v01_apiVersion(ManifestDocument doc, ManifestValidationResult r) {
        String v = doc.getApiVersion();
        if (v == null || !"ontology.platform/v1".equals(v))
            r.addError(ValidationError.of("V01", "apiVersion", null, "apiVersion",
                "unsupported apiVersion: " + v + ", expected ontology.platform/v1"));
    }

    private static final Pattern SEMVER = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");
    private void v02_semver(ManifestDocument doc, ManifestValidationResult r) {
        ManifestDocument.Metadata m = doc.getMetadata();
        if (m == null || m.getVersion() == null || !SEMVER.matcher(m.getVersion()).matches())
            r.addError(ValidationError.of("V02", "metadata", m!=null?m.getId():null, "version",
                "invalid semver: " + (m!=null?m.getVersion():"null")));
    }

    private void v03_aggregateRoots(ManifestDocument doc, ManifestValidationResult r) {
        long count = doc.getObjectTypes().stream().filter(t -> "aggregate_root".equals(t.getKind())).count();
        if (count == 0)
            r.addError(ValidationError.of("V03", "semantic", null, "objectTypes", "at least 1 aggregate_root required"));
    }

    private void v04_entityRefs(ManifestDocument doc, ManifestValidationResult r) {
        Set<String> ids = objectTypeIds(doc);
        for (ManifestDocument.ObjectType t : doc.getObjectTypes()) {
            String ref = t.getAggregateRootId();
            if (ref != null && !ids.contains(ref))
                r.addError(ValidationError.of("V04", "objectType", t.getId(), "aggregateRootId", "ref not found: " + ref));
        }
    }

    private void v05_actionRefs(ManifestDocument doc, ManifestValidationResult r) {
        Set<String> ids = objectTypeIds(doc);
        for (ManifestDocument.ActionDef a : doc.getActions()) {
            if (a.getAggregateRootId() != null && !ids.contains(a.getAggregateRootId()))
                r.addError(ValidationError.of("V05", "action", a.getId(), "aggregateRootId", "ref not found: " + a.getAggregateRootId()));
        }
    }

    private void v06_ruleRefs(ManifestDocument doc, ManifestValidationResult r) {
        Set<String> ruleIds = new HashSet<>();
        doc.getRules().forEach(x -> ruleIds.add(x.getId()));
        for (ManifestDocument.ActionDef a : doc.getActions())
            for (String rid : nvl(a.getPreRuleIds()))
                if (!ruleIds.contains(rid))
                    r.addError(ValidationError.of("V06", "action", a.getId(), "preRuleIds", "rule not found: " + rid));
    }

    private void v07_eventRefs(ManifestDocument doc, ManifestValidationResult r) {
        Set<String> eventIds = new HashSet<>();
        doc.getEvents().forEach(e -> eventIds.add(e.getId()));
        for (ManifestDocument.ActionDef a : doc.getActions())
            for (String eid : nvl(a.getPublishesEventIds()))
                if (!eventIds.contains(eid))
                    r.addError(ValidationError.of("V07", "action", a.getId(), "publishesEventIds", "event not found: " + eid));
    }

    private void v08_eventNameTense(ManifestDocument doc, ManifestValidationResult r) {
        for (ManifestDocument.EventDef e : doc.getEvents()) {
            String name = e.getNameEn();
            if (name != null && !name.endsWith("ed") && !name.endsWith("d"))
                r.addError(ValidationError.warning("V08", "event", e.getId(), "nameEn", "may not be past tense: " + name));
        }
    }

    private void v09_stateMachineInit(ManifestDocument doc, ManifestValidationResult r) {
        for (ManifestDocument.StateMachineDef sm : doc.getStateMachines()) {
            if (sm.getStates() == null) continue;
            long init = sm.getStates().stream().filter(ManifestDocument.StateDef::isInitial).count();
            if (init != 1)
                r.addError(ValidationError.of("V09", "stateMachine", sm.getId(), "states", "exactly 1 isInitial required, found " + init));
        }
    }

    private void v10_noCredentials(ManifestDocument doc, ManifestValidationResult r) {
        ManifestDocument.Spec spec = doc.getSpec();
        if (spec != null && spec.getDataSources() != null)
            for (ManifestDocument.DataSource ds : spec.getDataSources())
                if (ds.getConnectionConfig() != null && hasCred(ds.getConnectionConfig().toString()))
                    r.addError(ValidationError.of("V10", "dataSource", ds.getId(), "connectionConfig", "plaintext credentials, use credentialRef"));
    }

    private boolean hasCred(String s) {
        String l = s.toLowerCase();
        return l.contains("password") || l.contains("apikey") || l.contains("secret");
    }

    private void v11_uniqueIds(ManifestDocument doc, ManifestValidationResult r) {
        Set<String> seen = new HashSet<>();
        for (ManifestDocument.ObjectType t : doc.getObjectTypes()) { if (!seen.add(t.getId())) r.addError(ValidationError.of("V11","objectType",t.getId(),"id","duplicate id")); }
        for (ManifestDocument.ActionDef t : doc.getActions()) { if (!seen.add(t.getId())) r.addError(ValidationError.of("V11","action",t.getId(),"id","duplicate id")); }
        for (ManifestDocument.EventDef t : doc.getEvents()) { if (!seen.add(t.getId())) r.addError(ValidationError.of("V11","event",t.getId(),"id","duplicate id")); }
        for (ManifestDocument.RuleDef t : doc.getRules()) { if (!seen.add(t.getId())) r.addError(ValidationError.of("V11","rule",t.getId(),"id","duplicate id")); }
        for (ManifestDocument.StateMachineDef t : doc.getStateMachines()) { if (!seen.add(t.getId())) r.addError(ValidationError.of("V11","stateMachine",t.getId(),"id","duplicate id")); }
    }

    private Set<String> objectTypeIds(ManifestDocument doc) { Set<String> s=new HashSet<>(); doc.getObjectTypes().forEach(t->s.add(t.getId())); return s; }
    private static <T> List<T> nvl(List<T> l) { return l!=null ? l : List.of(); }
}

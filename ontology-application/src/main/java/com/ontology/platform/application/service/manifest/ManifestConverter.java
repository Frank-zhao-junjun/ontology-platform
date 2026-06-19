package com.ontology.platform.application.service.manifest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.vo.manifest.ManifestDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目1 OntologyManifest → 项目2 ManifestDocument 转换器
 *
 * <p>将项目1简化模型导出的 JSON Manifest 映射为项目2内部格式，
 * 使其能被现有的 ManifestService + ManifestController 处理。</p>
 */
@Component
@RequiredArgsConstructor
public class ManifestConverter {

    private final ObjectMapper objectMapper;

    /**
     * 转换项目1的 OntologyManifest JSON → 项目2的 ManifestDocument
     *
     * @param json 项目1导出的 OntologyManifest JSON 字符串
     * @return 项目2的 ManifestDocument
     */
    public ManifestDocument convert(String json) {
        JsonNode root;
        try {
            root = objectMapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalArgumentException("无法解析 Manifest JSON: " + e.getMessage(), e);
        }

        // metadata
        JsonNode meta = root.path("metadata");
        ManifestDocument.Metadata metadata = ManifestDocument.Metadata.builder()
                .id(pathStr(meta, "id"))
                .name(pathStr(meta, "name"))
                .displayName(pathStr(meta, "name"))
                .version(pathStr(meta, "version"))
                .description(pathStr(meta, "description"))
                .build();

        // spec
        JsonNode spec = root.path("spec");
        List<ManifestDocument.StateMachineDef> stateMachinesFromSemantic = extractStateMachinesFromSemantic(spec.path("semantic"));
        ManifestDocument.Semantic semantic = buildSemantic(spec.path("semantic"));
        ManifestDocument.Behavior behavior = buildBehavior(spec.path("behavior"));

        // Project 1 stores stateMachines in spec.semantic.stateMachines → merge into behavior
        if (behavior != null && !stateMachinesFromSemantic.isEmpty()) {
            List<ManifestDocument.StateMachineDef> merged = new ArrayList<>(behavior.getStateMachines());
            merged.addAll(stateMachinesFromSemantic);
            behavior.setStateMachines(merged);
        } else if (behavior == null && !stateMachinesFromSemantic.isEmpty()) {
            behavior = ManifestDocument.Behavior.builder()
                    .stateMachines(stateMachinesFromSemantic)
                    .build();
        }

        ManifestDocument.Spec specObj = ManifestDocument.Spec.builder()
                .semantic(semantic)
                .behavior(behavior)
                .events(buildEvents(spec.path("events")))
                .governance(buildGovernance(spec.path("governance")))
                .epc(buildEpc(spec.path("process")))
                .dataSources(buildDataSources(spec.path("dataSources")))
                .build();

        return ManifestDocument.builder()
                .apiVersion(pathStr(root, "apiVersion"))
                .kind(pathStr(root, "kind"))
                .metadata(metadata)
                .spec(specObj)
                .build();
    }

    private ManifestDocument.Semantic buildSemantic(JsonNode semantic) {
        if (semantic.isMissingNode() || semantic.isNull()) return null;

        List<ManifestDocument.ObjectType> objectTypes = new ArrayList<>();
        for (JsonNode ot : nonNullArray(semantic.path("objectTypes"))) {
            objectTypes.add(ManifestDocument.ObjectType.builder()
                    .id(pathStr(ot, "id"))
                    .name(pathStr(ot, "name"))
                    .nameEn(pathStr(ot, "nameEn"))
                    .kind(pathStr(ot, "kind"))
                    .description(pathStr(ot, "description"))
                    .build());
        }

        return ManifestDocument.Semantic.builder()
                .objectTypes(objectTypes)
                .build();
    }

    private List<ManifestDocument.StateMachineDef> extractStateMachinesFromSemantic(JsonNode semantic) {
        List<ManifestDocument.StateMachineDef> list = new ArrayList<>();
        for (JsonNode sm : nonNullArray(semantic.path("stateMachines"))) {
            list.add(buildStateMachine(sm));
        }
        return list;
    }

    private ManifestDocument.Behavior buildBehavior(JsonNode behavior) {
        if (behavior.isMissingNode() || behavior.isNull()) return null;

        List<ManifestDocument.ActionDef> actions = new ArrayList<>();
        for (JsonNode a : nonNullArray(behavior.path("actions"))) {
            actions.add(ManifestDocument.ActionDef.builder()
                    .id(pathStr(a, "id"))
                    .name(pathStr(a, "name"))
                    .nameEn(pathStr(a, "nameEn"))
                    .description(pathStr(a, "description"))
                    .build());
        }

        List<ManifestDocument.RuleDef> rules = new ArrayList<>();
        for (JsonNode r : nonNullArray(behavior.path("rules"))) {
            rules.add(ManifestDocument.RuleDef.builder()
                    .id(pathStr(r, "id"))
                    .name(pathStr(r, "name"))
                    .description(pathStr(r, "description"))
                    .build());
        }

        List<ManifestDocument.StateMachineDef> stateMachines = new ArrayList<>();
        // Project 1 stores stateMachines in semantic, not behavior
        // We'll handle them if present
        for (JsonNode sm : nonNullArray(behavior.path("stateMachines"))) {
            stateMachines.add(buildStateMachine(sm));
        }

        return ManifestDocument.Behavior.builder()
                .actions(actions)
                .rules(rules)
                .stateMachines(stateMachines)
                .build();
    }

    private ManifestDocument.StateMachineDef buildStateMachine(JsonNode sm) {
        List<ManifestDocument.StateDef> states = new ArrayList<>();
        for (JsonNode s : nonNullArray(sm.path("states"))) {
            states.add(ManifestDocument.StateDef.builder()
                    .name(pathStr(s, "name"))
                    .isInitial(s.path("isInitial").asBoolean(false))
                    .isFinal(s.path("isFinal").asBoolean(false))
                    .build());
        }

        List<ManifestDocument.TransitionDef> transitions = new ArrayList<>();
        for (JsonNode t : nonNullArray(sm.path("transitions"))) {
            transitions.add(ManifestDocument.TransitionDef.builder()
                    .from(pathStr(t, "from"))
                    .to(pathStr(t, "to"))
                    .trigger(pathStr(t, "trigger"))
                    .build());
        }

        return ManifestDocument.StateMachineDef.builder()
                .id(pathStr(sm, "id"))
                .name(pathStr(sm, "name"))
                .states(states)
                .transitions(transitions)
                .build();
    }

    private ManifestDocument.Events buildEvents(JsonNode events) {
        if (events.isMissingNode() || events.isNull()) return null;

        List<ManifestDocument.EventDef> domainEvents = new ArrayList<>();
        for (JsonNode e : nonNullArray(events.path("domainEvents"))) {
            domainEvents.add(ManifestDocument.EventDef.builder()
                    .id(pathStr(e, "id"))
                    .name(pathStr(e, "name"))
                    .nameEn(pathStr(e, "nameEn"))
                    .build());
        }

        return ManifestDocument.Events.builder()
                .domainEvents(domainEvents)
                .build();
    }

    private ManifestDocument.Governance buildGovernance(JsonNode gov) {
        if (gov.isMissingNode() || gov.isNull()) return null;

        List<ManifestDocument.RoleDef> roles = new ArrayList<>();
        for (JsonNode r : nonNullArray(gov.path("roles"))) {
            roles.add(ManifestDocument.RoleDef.builder()
                    .id(pathStr(r, "id"))
                    .name(pathStr(r, "name"))
                    .build());
        }

        return ManifestDocument.Governance.builder()
                .roles(roles)
                .build();
    }

    private List<ManifestDocument.Epc> buildEpc(JsonNode process) {
        if (process.isMissingNode() || process.isNull()) return List.of();

        List<ManifestDocument.Epc> epcList = new ArrayList<>();
        for (JsonNode orch : nonNullArray(process.path("orchestrations"))) {
            List<ManifestDocument.EpcStepDef> steps = new ArrayList<>();
            int order = 0;
            for (JsonNode step : nonNullArray(orch.path("steps"))) {
                steps.add(ManifestDocument.EpcStepDef.builder()
                        .stepOrder(order++)
                        .actionId(pathStr(step, "actionId"))
                        .conditions(List.of(pathStr(step, "type")))
                        .build());
            }

            epcList.add(ManifestDocument.Epc.builder()
                    .id(pathStr(orch, "id"))
                    .flowName(pathStr(orch, "name"))
                    .steps(steps)
                    .build());
        }
        return epcList;
    }

    private List<ManifestDocument.DataSource> buildDataSources(JsonNode ds) {
        if (ds.isMissingNode() || ds.isNull()) return List.of();

        List<ManifestDocument.DataSource> list = new ArrayList<>();
        for (JsonNode d : nonNullArray(ds)) {
            list.add(ManifestDocument.DataSource.builder()
                    .id(pathStr(d, "id"))
                    .name(pathStr(d, "name"))
                    .sourceType(pathStr(d, "sourceType"))
                    .build());
        }
        return list;
    }

    // ==================== 工具 ====================

    private static String pathStr(JsonNode node, String field) {
        JsonNode val = node.path(field);
        return val.isTextual() ? val.asText() : null;
    }

    private static List<JsonNode> nonNullArray(JsonNode arr) {
        List<JsonNode> result = new ArrayList<>();
        if (arr.isArray()) {
            for (JsonNode item : arr) {
                if (!item.isNull()) result.add(item);
            }
        }
        return result;
    }
}

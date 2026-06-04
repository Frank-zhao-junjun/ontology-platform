package com.ontology.platform.api.controller;

import com.ontology.platform.application.service.BehaviorService;
import com.ontology.platform.common.enums.InvocationMode;
import com.ontology.platform.domain.entity.DomainEventDefinition;
import com.ontology.platform.domain.entity.OntologyAction;
import com.ontology.platform.domain.entity.ValidationRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/contexts/{contextId}")
@RequiredArgsConstructor
@Tag(name = "Behavior", description = "行为 / 规则 / 领域事件 (US-B01, B03, E01)")
public class BehaviorController {
    private final BehaviorService behaviorService;

    @PostMapping("/validation-rules")
    @Operation(summary = "创建校验规则 (US-B03)")
    public ResponseEntity<Map<String, Object>> createRule(@PathVariable String contextId,
                                                           @RequestBody Map<String, Object> body) {
        ValidationRule r = behaviorService.createRule(contextId,
                str(body, "manifestCode"), str(body, "name"), str(body, "ruleType"),
                str(body, "expressionJson", "{}"), str(body, "errorMessage"),
                str(body, "failurePayloadSchema"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("code", 0, "message", "success", "data", toRuleMap(r)));
    }

    @GetMapping("/validation-rules")
    public ResponseEntity<Map<String, Object>> listRules(@PathVariable String contextId) {
        var data = behaviorService.listRules(contextId).stream().map(this::toRuleMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    @PostMapping("/actions")
    @Operation(summary = "创建行为 (US-B01)")
    public ResponseEntity<Map<String, Object>> createAction(@PathVariable String contextId,
                                                            @RequestBody Map<String, Object> body) {
        InvocationMode mode = body.get("invocationMode") != null
                ? InvocationMode.fromCode(body.get("invocationMode").toString()) : InvocationMode.BOTH;
        OntologyAction a = behaviorService.createAction(contextId,
                str(body, "manifestCode"), str(body, "name"), str(body, "nameEn"),
                str(body, "description"), str(body, "aggregateRootId"), mode,
                str(body, "parametersJson", "[]"), str(body, "publishesEventIdsJson", "[]"),
                str(body, "allowedStateFromJson", "[]"), str(body, "businessScenarioIdsJson", "[]"),
                str(body, "mcpToolName"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("code", 0, "message", "success", "data", toActionMap(a)));
    }

    @GetMapping("/actions")
    public ResponseEntity<Map<String, Object>> listActions(@PathVariable String contextId) {
        var data = behaviorService.listActions(contextId).stream().map(this::toActionMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    @PutMapping("/actions/{actionId}/rules")
    @Operation(summary = "关联校验规则到行为，有序 (US-B03 AC-3)")
    public ResponseEntity<Map<String, Object>> linkRules(@PathVariable String contextId,
                                                         @PathVariable String actionId,
                                                         @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> ruleIds = (List<String>) body.get("ruleIds");
        behaviorService.linkRulesToAction(actionId, ruleIds);
        return ResponseEntity.ok(Map.of("code", 0, "message", "success",
                "data", Map.of("actionId", actionId, "ruleIds", behaviorService.listRuleIdsForAction(actionId))));
    }

    @PostMapping("/domain-events")
    @Operation(summary = "创建领域事件 (US-E01)")
    public ResponseEntity<Map<String, Object>> createEvent(@PathVariable String contextId,
                                                           @RequestBody Map<String, Object> body) {
        DomainEventDefinition e = behaviorService.createDomainEvent(contextId,
                str(body, "manifestCode"), str(body, "name"), str(body, "nameEn"),
                str(body, "aggregateRootId"), str(body, "triggerActionId"),
                str(body, "payloadSchemaJson", "{}"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("code", 0, "message", "success", "data", toEventMap(e)));
    }

    @GetMapping("/domain-events")
    public ResponseEntity<Map<String, Object>> listEvents(@PathVariable String contextId) {
        var data = behaviorService.listDomainEvents(contextId).stream().map(this::toEventMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    private Map<String, Object> toRuleMap(ValidationRule r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("manifestCode", r.getManifestCode());
        m.put("name", r.getName());
        m.put("ruleType", r.getRuleType());
        m.put("enabled", r.isEnabled());
        return m;
    }

    private Map<String, Object> toActionMap(OntologyAction a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("manifestCode", a.getManifestCode());
        m.put("name", a.getName());
        m.put("nameEn", a.getNameEn());
        m.put("aggregateRootId", a.getAggregateRootId());
        m.put("invocationMode", a.getInvocationMode().name());
        return m;
    }

    private Map<String, Object> toEventMap(DomainEventDefinition e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("manifestCode", e.getManifestCode());
        m.put("name", e.getName());
        m.put("nameEn", e.getNameEn());
        m.put("triggerActionId", e.getTriggerActionId() != null ? e.getTriggerActionId() : "");
        return m;
    }

    private static String str(Map<String, Object> body, String key) {
        return str(body, key, null);
    }

    private static String str(Map<String, Object> body, String key, String def) {
        Object v = body.get(key);
        return v != null ? v.toString() : def;
    }
}

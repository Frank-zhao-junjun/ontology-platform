package com.ontology.platform.api.controller;

import com.ontology.platform.application.service.GovernanceService;
import com.ontology.platform.domain.entity.AgentSandbox;
import com.ontology.platform.domain.entity.ObjectPermission;
import com.ontology.platform.domain.entity.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "Governance", description = "角色权限与沙箱 (US-G01, G04)")
public class GovernanceController {
    private final GovernanceService svc;

    @PostMapping("/roles")
    @Operation(summary = "创建角色 (US-G01)")
    public ResponseEntity<Map<String, Object>> createRole(@RequestBody Map<String, String> body) {
        Role r = svc.createRole(body.get("contextId"), body.get("name"), body.get("code"), body.get("description"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("code", 0, "message", "success", "data", toRoleMap(r)));
    }

    @GetMapping("/roles")
    public ResponseEntity<Map<String, Object>> listRoles(
            @RequestParam(required = false) String contextId,
            @RequestParam(required = false) Boolean isGlobal) {
        var data = svc.listRoles(contextId, isGlobal).stream().map(this::toRoleMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    @PostMapping("/roles/{roleId}/object-permissions")
    @Operation(summary = "配置对象级权限 (US-G01 AC-2)")
    public ResponseEntity<Map<String, Object>> createObjectPermission(@PathVariable String roleId,
                                                                      @RequestBody Map<String, Object> body) {
        ObjectPermission p = svc.addObjectPermission(roleId, str(body, "objectTypeId"),
                bool(body, "permRead"), bool(body, "permWrite"),
                bool(body, "permDelete"), bool(body, "permExecute"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("code", 0, "message", "success", "data", toPermMap(p)));
    }

    @PostMapping("/sandboxes")
    @Operation(summary = "配置 AI Agent 沙箱 (US-G04)")
    public ResponseEntity<Map<String, Object>> createSandbox(@RequestBody Map<String, Object> body) {
        int maxOps = body.get("maxOpsPerSecond") instanceof Number n ? n.intValue() : 10;
        AgentSandbox sb = svc.createSandbox(str(body, "name"), str(body, "manifestVersionId"), str(body, "agentRoleId"),
                stringList(body, "allowedTools"), stringList(body, "allowedAggregateRoots"),
                stringList(body, "allowedBehaviors"), maxOps);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("code", 0, "message", "success", "data", toSandboxMap(sb)));
    }

    @GetMapping("/sandboxes")
    public ResponseEntity<Map<String, Object>> listSandboxes() {
        var data = svc.listSandboxes().stream().map(this::toSandboxMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    private Map<String, Object> toRoleMap(Role r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("name", r.getName());
        m.put("code", r.getCode());
        m.put("description", r.getDescription() != null ? r.getDescription() : "");
        m.put("contextId", r.getContextId() != null ? r.getContextId() : "");
        m.put("isGlobal", r.isGlobal());
        return m;
    }

    private Map<String, Object> toPermMap(ObjectPermission p) {
        return Map.of("id", p.getId(), "roleId", p.getRoleId(), "objectTypeId", p.getObjectTypeId(),
                "permRead", p.isPermRead(), "permWrite", p.isPermWrite(),
                "permDelete", p.isPermDelete(), "permExecute", p.isPermExecute());
    }

    private Map<String, Object> toSandboxMap(AgentSandbox sb) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", sb.getId());
        m.put("name", sb.getName());
        m.put("manifestVersionId", sb.getManifestVersionId() != null ? sb.getManifestVersionId() : "");
        m.put("agentRoleId", sb.getAgentRoleId() != null ? sb.getAgentRoleId() : "");
        m.put("allowedTools", sb.getAllowedTools());
        m.put("allowedAggregateRoots", sb.getAllowedAggregateRoots());
        m.put("allowedBehaviors", sb.getAllowedBehaviors());
        m.put("maxOpsPerSecond", sb.getMaxOpsPerSecond());
        return m;
    }

    @SuppressWarnings("unchecked")
    private static List<String> stringList(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v instanceof List<?> list) return list.stream().map(Object::toString).collect(Collectors.toList());
        return List.of();
    }

    private static String str(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v != null ? v.toString() : null;
    }

    private static boolean bool(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v instanceof Boolean b && b;
    }
}

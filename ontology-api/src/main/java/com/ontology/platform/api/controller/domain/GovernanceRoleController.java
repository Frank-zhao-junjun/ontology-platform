package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.CreateGovernanceRoleRequest;
import com.ontology.platform.application.dto.domain.GovernanceRoleResponse;
import com.ontology.platform.application.service.GovernanceRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ontologies/{ontologyId}/governance-roles")
@RequiredArgsConstructor
@Tag(name = "GovernanceRole", description = "治理角色管理")
public class GovernanceRoleController {
    private final GovernanceRoleService service;

    @PostMapping
    @Operation(summary = "创建治理角色")
    public ResponseEntity<ApiResponse<GovernanceRoleResponse>> create(
            @PathVariable String ontologyId, @RequestBody CreateGovernanceRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(ontologyId, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取治理角色")
    public ResponseEntity<ApiResponse<GovernanceRoleResponse>> getById(@PathVariable String id) {
        var resp = service.getById(id);
        return resp != null ? ResponseEntity.ok(ApiResponse.success(resp))
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    @Operation(summary = "列出治理角色")
    public ResponseEntity<ApiResponse<List<GovernanceRoleResponse>>> list(
            @PathVariable String ontologyId) {
        return ResponseEntity.ok(ApiResponse.success(service.listByOntologyId(ontologyId)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除治理角色")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

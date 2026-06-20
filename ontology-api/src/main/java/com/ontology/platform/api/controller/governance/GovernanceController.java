package com.ontology.platform.api.controller.governance;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.governance.*;
import com.ontology.platform.application.service.governance.GovernanceService;
import com.ontology.platform.domain.entity.governance.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/governance")
@RequiredArgsConstructor
@Tag(name = "Governance", description = "Agent Token & RBAC governance")
public class GovernanceController {

    private final GovernanceService governanceService;

    @PostMapping("/tokens")
    @Operation(summary = "Issue agent token")
    public ResponseEntity<ApiResponse<TokenResponse>> createToken(
            @Valid @RequestBody CreateTokenRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "admin") String userId) {
        log.info("REST: Create token for agentId={}", request.getAgentId());
        TokenResponse response = governanceService.createToken(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/tokens")
    @Operation(summary = "List agent tokens")
    public ResponseEntity<ApiResponse<List<TokenResponse>>> listTokens(
            @RequestParam(defaultValue = "default") String tenantId) {
        log.debug("REST: List tokens for tenantId={}", tenantId);
        List<TokenResponse> tokens = governanceService.listTokens(tenantId);
        return ResponseEntity.ok(ApiResponse.success(tokens));
    }

    @DeleteMapping("/tokens/{id}")
    @Operation(summary = "Revoke agent token")
    public ResponseEntity<ApiResponse<Void>> revokeToken(@Parameter(description = "令牌ID") @PathVariable String id) {
        log.info("REST: Revoke token id={}", id);
        governanceService.revokeToken(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ==================== Roles ====================

    @PostMapping("/roles")
    @Operation(summary = "Create agent role")
    public ResponseEntity<ApiResponse<AgentRole>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        AgentRole role = governanceService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(role));
    }

    @GetMapping("/roles")
    @Operation(summary = "List roles by token")
    public ResponseEntity<ApiResponse<List<AgentRole>>> listRoles(@RequestParam String tokenId) {
        return ResponseEntity.ok(ApiResponse.success(governanceService.listRolesByToken(tokenId)));
    }

    // ==================== Permissions ====================

    @PostMapping("/permissions")
    @Operation(summary = "Create role permission")
    public ResponseEntity<ApiResponse<RolePermission>> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        RolePermission perm = governanceService.createPermission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(perm));
    }

    @GetMapping("/permissions")
    @Operation(summary = "List permissions by domain")
    public ResponseEntity<ApiResponse<List<RolePermission>>> listPermissions(@RequestParam String domain) {
        return ResponseEntity.ok(ApiResponse.success(governanceService.listPermissionsByDomain(domain)));
    }

    // ==================== Approvals ====================

    @PostMapping("/approvals")
    @Operation(summary = "Submit approval request")
    public ResponseEntity<ApiResponse<ApprovalRequest>> submitApproval(
            @Valid @RequestBody SubmitApprovalRequest request) {
        log.info("REST: Submit approval, agentId={}, actionId={}, op={}", request.getAgentId(), request.getActionId(), request.getRequestedOp());
        ApprovalRequest approval = governanceService.submitApproval(
                request.getAgentId(), request.getActionId(), request.getRequestedOp());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(approval));
    }

    @GetMapping("/approvals/{id}")
    @Operation(summary = "Get approval status")
    public ResponseEntity<ApiResponse<ApprovalRequest>> getApproval(@Parameter(description = "审批ID") @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(governanceService.getApproval(id)));
    }

    @GetMapping("/approvals")
    @Operation(summary = "List pending approvals")
    public ResponseEntity<ApiResponse<List<ApprovalRequest>>> listPendingApprovals() {
        return ResponseEntity.ok(ApiResponse.success(governanceService.listPendingApprovals()));
    }

    @PutMapping("/approvals/{id}")
    @Operation(summary = "Resolve approval (approve/reject)")
    public ResponseEntity<ApiResponse<ApprovalRequest>> resolveApproval(
            @Parameter(description = "审批ID") @PathVariable String id, @Valid @RequestBody ResolveApprovalRequest request) {
        return ResponseEntity.ok(ApiResponse.success(governanceService.resolveApproval(id, request)));
    }
}

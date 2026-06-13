package com.ontology.platform.api.controller.governance;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.governance.CreateTokenRequest;
import com.ontology.platform.application.dto.governance.TokenResponse;
import com.ontology.platform.application.service.governance.GovernanceService;
import io.swagger.v3.oas.annotations.Operation;
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
            @RequestHeader(value = "X-User-Id", defaultValue = "admin") String userId) {
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
    public ResponseEntity<ApiResponse<Void>> revokeToken(@PathVariable String id) {
        log.info("REST: Revoke token id={}", id);
        governanceService.revokeToken(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

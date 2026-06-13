package com.ontology.platform.application.service.governance;

import com.ontology.platform.application.dto.governance.*;
import com.ontology.platform.domain.entity.governance.*;
import java.util.List;

public interface GovernanceService {
    // Token
    TokenResponse createToken(CreateTokenRequest request, String createdBy);
    List<TokenResponse> listTokens(String tenantId);
    void revokeToken(String id);
    // Role & Permission
    AgentRole createRole(CreateRoleRequest request);
    List<AgentRole> listRolesByToken(String tokenId);
    RolePermission createPermission(CreatePermissionRequest request);
    List<RolePermission> listPermissionsByDomain(String domain);
    // Approval
    ApprovalRequest submitApproval(String agentId, String actionId, String requestedOp);
    ApprovalRequest resolveApproval(String id, ResolveApprovalRequest request);
    ApprovalRequest getApproval(String id);
    List<ApprovalRequest> listPendingApprovals();
}

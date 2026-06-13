package com.ontology.platform.application.service.governance;

import com.ontology.platform.application.dto.governance.CreateTokenRequest;
import com.ontology.platform.application.dto.governance.TokenResponse;
import java.util.List;

public interface GovernanceService {
    TokenResponse createToken(CreateTokenRequest request, String createdBy);
    List<TokenResponse> listTokens(String tenantId);
    void revokeToken(String id);
}

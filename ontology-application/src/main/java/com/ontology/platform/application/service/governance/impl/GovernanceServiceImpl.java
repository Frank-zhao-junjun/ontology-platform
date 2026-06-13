package com.ontology.platform.application.service.governance.impl;

import com.ontology.platform.application.dto.governance.CreateTokenRequest;
import com.ontology.platform.application.dto.governance.TokenResponse;
import com.ontology.platform.application.service.governance.GovernanceService;
import com.ontology.platform.common.enums.TokenStatus;
import com.ontology.platform.common.exception.ValidationException;
import com.ontology.platform.domain.entity.governance.AgentToken;
import com.ontology.platform.domain.repository.governance.AgentTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GovernanceServiceImpl implements GovernanceService {

    private final AgentTokenRepository tokenRepository;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    @Transactional
    public TokenResponse createToken(CreateTokenRequest request, String createdBy) {
        // Check duplicate
        tokenRepository.findByAgentId(request.getAgentId()).ifPresent(existing -> {
            if (existing.isActive()) {
                throw new ValidationException(
                    com.ontology.platform.common.enums.ErrorCode.DUPLICATE_NAME,
                    "Agent token already exists for agentId: " + request.getAgentId()
                );
            }
        });

        // Generate raw token
        byte[] rawBytes = new byte[32];
        SECURE_RANDOM.nextBytes(rawBytes);
        String rawToken = "otp_" + Base64.getUrlEncoder().withoutPadding().encodeToString(rawBytes);

        // Hash for storage
        String tokenHash = hashToken(rawToken);

        // Persist
        AgentToken entity = AgentToken.create(
            request.getAgentId(), tokenHash, request.getTenantId(),
            request.getDisplayName(), createdBy, request.getTtlDays()
        );
        tokenRepository.save(entity);

        log.info("Token created: agentId={}, tenantId={}", request.getAgentId(), request.getTenantId());

        return TokenResponse.builder()
                .id(entity.getId())
                .agentId(entity.getAgentId())
                .token(rawToken)
                .tenantId(entity.getTenantId())
                .displayName(entity.getDisplayName())
                .status(entity.getStatus().name())
                .domains(request.getDomains())
                .issuedAt(entity.getIssuedAt())
                .expiresAt(entity.getExpiresAt())
                .build();
    }

    @Override
    public List<TokenResponse> listTokens(String tenantId) {
        return tokenRepository.findByTenantId(tenantId).stream()
                .map(t -> TokenResponse.builder()
                        .id(t.getId())
                        .agentId(t.getAgentId())
                        .tenantId(t.getTenantId())
                        .displayName(t.getDisplayName())
                        .status(t.getStatus().name())
                        .issuedAt(t.getIssuedAt())
                        .expiresAt(t.getExpiresAt())
                        .lastUsedAt(t.getLastUsedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void revokeToken(String id) {
        AgentToken token = tokenRepository.findById(id)
                .orElseThrow(() -> new ValidationException(
                    com.ontology.platform.common.enums.ErrorCode.RESOURCE_NOT_FOUND,
                    "Token not found: " + id
                ));
        token.revoke();
        tokenRepository.save(token);
        log.info("Token revoked: id={}, agentId={}", id, token.getAgentId());
    }

    private String hashToken(String rawToken) {
        // TODO: Phase 2 use bcrypt
        try {
            return Base64.getEncoder().encodeToString(
                java.security.MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes())
            );
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}

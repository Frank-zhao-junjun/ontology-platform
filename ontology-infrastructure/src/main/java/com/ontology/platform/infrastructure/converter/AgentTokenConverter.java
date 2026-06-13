package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.governance.AgentToken;
import com.ontology.platform.infrastructure.persistence.AgentTokenPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 令牌 PO <-> Entity 转换器
 * Agent Token PO Converter
 */
@Slf4j
@Component
public class AgentTokenConverter {

    /**
     * PO 转换为 Entity
     */
    public AgentToken toEntity(AgentTokenPO po) {
        if (po == null) {
            return null;
        }
        return AgentToken.builder()
                .id(po.getId())
                .agentId(po.getAgentId())
                .tokenHash(po.getTokenHash())
                .tenantId(po.getTenantId())
                .displayName(po.getDisplayName())
                .status(po.getStatusEnum())
                .issuedAt(po.getIssuedAt())
                .expiresAt(po.getExpiresAt())
                .lastUsedAt(po.getLastUsedAt())
                .createdBy(po.getCreatedBy())
                .createdAt(po.getCreatedAt())
                .build();
    }

    /**
     * Entity 转换为 PO
     */
    public AgentTokenPO toPO(AgentToken entity) {
        if (entity == null) {
            return null;
        }
        return AgentTokenPO.builder()
                .id(entity.getId())
                .agentId(entity.getAgentId())
                .tokenHash(entity.getTokenHash())
                .tenantId(entity.getTenantId())
                .displayName(entity.getDisplayName())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .issuedAt(entity.getIssuedAt())
                .expiresAt(entity.getExpiresAt())
                .lastUsedAt(entity.getLastUsedAt())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * PO 列表转换为 Entity 列表
     */
    public List<AgentToken> toEntityList(List<AgentTokenPO> poList) {
        if (poList == null || poList.isEmpty()) {
            return List.of();
        }
        List<AgentToken> result = new ArrayList<>(poList.size());
        for (AgentTokenPO po : poList) {
            result.add(toEntity(po));
        }
        return result;
    }
}

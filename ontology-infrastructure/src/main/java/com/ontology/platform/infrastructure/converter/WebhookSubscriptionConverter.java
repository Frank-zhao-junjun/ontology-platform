package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.WebhookSubscription;
import com.ontology.platform.infrastructure.persistence.WebhookSubscriptionPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WebhookSubscriptionConverter {

    public WebhookSubscription toEntity(WebhookSubscriptionPO po) {
        if (po == null) return null;
        return WebhookSubscription.builder()
                .id(po.getId())
                .tenantId(po.getTenantId())
                .agentId(po.getAgentId())
                .callbackUrl(po.getCallbackUrl())
                .eventTypes(po.getEventTypes())
                .secret(po.getSecret())
                .isActive(po.getIsActive())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public WebhookSubscriptionPO toPO(WebhookSubscription entity) {
        if (entity == null) return null;
        return WebhookSubscriptionPO.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .agentId(entity.getAgentId())
                .callbackUrl(entity.getCallbackUrl())
                .eventTypes(entity.getEventTypes())
                .secret(entity.getSecret())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<WebhookSubscription> toEntityList(List<WebhookSubscriptionPO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}

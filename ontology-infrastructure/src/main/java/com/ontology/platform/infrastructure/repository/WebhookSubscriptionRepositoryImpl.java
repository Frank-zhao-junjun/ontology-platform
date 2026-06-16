package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.WebhookSubscription;
import com.ontology.platform.domain.repository.WebhookSubscriptionRepository;
import com.ontology.platform.infrastructure.converter.WebhookSubscriptionConverter;
import com.ontology.platform.infrastructure.persistence.WebhookSubscriptionPO;
import com.ontology.platform.infrastructure.persistence.WebhookSubscriptionPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class WebhookSubscriptionRepositoryImpl implements WebhookSubscriptionRepository {

    private final WebhookSubscriptionPOMapper webhookSubscriptionPOMapper;
    private final WebhookSubscriptionConverter webhookSubscriptionConverter;

    @Override
    public Optional<WebhookSubscription> findById(UUID id) {
        log.debug("Finding webhook subscription by id: {}", id);
        WebhookSubscriptionPO po = webhookSubscriptionPOMapper.selectById(id);
        return Optional.ofNullable(webhookSubscriptionConverter.toEntity(po));
    }

    @Override
    public List<WebhookSubscription> findActiveByTenant(String tenantId) {
        log.debug("Finding active webhook subscriptions by tenant: {}", tenantId);
        List<WebhookSubscriptionPO> poList = webhookSubscriptionPOMapper.selectActiveByTenant(tenantId);
        return webhookSubscriptionConverter.toEntityList(poList);
    }

    @Override
    public List<WebhookSubscription> findByEventType(String eventType) {
        log.debug("Finding webhook subscriptions by event type: {}", eventType);
        List<WebhookSubscriptionPO> poList = webhookSubscriptionPOMapper.selectByEventType(eventType);
        return webhookSubscriptionConverter.toEntityList(poList);
    }

    @Override
    public WebhookSubscription save(WebhookSubscription entity) {
        log.debug("Saving webhook subscription: {}", entity.getId());
        WebhookSubscriptionPO po = webhookSubscriptionConverter.toPO(entity);
        if (po.getCreatedAt() == null) {
            po.setCreatedAt(Instant.now());
        }
        po.setUpdatedAt(Instant.now());
        if (webhookSubscriptionPOMapper.selectById(entity.getId()) != null) {
            webhookSubscriptionPOMapper.updateById(po);
        } else {
            webhookSubscriptionPOMapper.insert(po);
        }
        return entity;
    }

    @Override
    public void deleteById(UUID id) {
        log.debug("Deleting webhook subscription: {}", id);
        webhookSubscriptionPOMapper.deleteById(id);
    }
}

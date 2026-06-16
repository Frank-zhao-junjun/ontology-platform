package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.WebhookSubscription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebhookSubscriptionRepository {

    Optional<WebhookSubscription> findById(UUID id);

    List<WebhookSubscription> findActiveByTenant(String tenantId);

    List<WebhookSubscription> findByEventType(String eventType);

    WebhookSubscription save(WebhookSubscription entity);

    void deleteById(UUID id);
}

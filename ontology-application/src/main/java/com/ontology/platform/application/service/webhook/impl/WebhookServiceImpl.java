package com.ontology.platform.application.service.webhook.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.webhook.CreateWebhookRequest;
import com.ontology.platform.application.dto.webhook.WebhookResponse;
import com.ontology.platform.application.service.webhook.WebhookService;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.entity.WebhookSubscription;
import com.ontology.platform.domain.repository.WebhookSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {

    private final WebhookSubscriptionRepository webhookRepository;
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    @Transactional
    public WebhookResponse createWebhook(CreateWebhookRequest request, String tenantId, String userId) {
        var entity = WebhookSubscription.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .agentId(userId)
                .callbackUrl(request.getCallbackUrl())
                .eventTypes(toJson(request.getEventTypes()))
                .secret(request.getSecret())
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        webhookRepository.save(entity);
        log.info("Webhook registered: id={}, url={}, tenant={}", entity.getId(), request.getCallbackUrl(), tenantId);
        return toResponse(entity);
    }

    @Override
    public List<WebhookResponse> listWebhooks(String tenantId) {
        return webhookRepository.findActiveByTenant(tenantId).stream()
                .map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void deleteWebhook(UUID webhookId) {
        var entity = webhookRepository.findById(webhookId);
        if (entity.isEmpty()) {
            throw new ResourceNotFoundException("Webhook", webhookId.toString());
        }
        webhookRepository.deleteById(webhookId);
        log.info("Webhook deleted: id={}", webhookId);
    }

    @SuppressWarnings("unchecked")
    private WebhookResponse toResponse(WebhookSubscription entity) {
        return WebhookResponse.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .agentId(entity.getAgentId())
                .callbackUrl(entity.getCallbackUrl())
                .eventTypes(fromJsonList(entity.getEventTypes()))
                .isActive(entity.getIsActive() != null && entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String toJson(Object obj) {
        try { return mapper.writeValueAsString(obj); }
        catch (Exception e) { log.error("JSON error", e); return "[]"; }
    }

    @SuppressWarnings("unchecked")
    private List<String> fromJsonList(String json) {
        if (json == null) return List.of();
        try { return mapper.readValue(json, new TypeReference<List<String>>() {}); }
        catch (Exception e) { log.error("JSON deserialize error", e); return List.of(); }
    }
}

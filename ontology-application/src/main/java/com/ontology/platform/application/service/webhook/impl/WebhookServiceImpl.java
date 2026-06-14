package com.ontology.platform.application.service.webhook.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.webhook.CreateWebhookRequest;
import com.ontology.platform.application.dto.webhook.WebhookResponse;
import com.ontology.platform.application.service.webhook.WebhookService;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.infrastructure.persistence.WebhookSubscriptionPO;
import com.ontology.platform.infrastructure.persistence.WebhookSubscriptionPOMapper;
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

    private final WebhookSubscriptionPOMapper webhookMapper;
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    @Transactional
    public WebhookResponse createWebhook(CreateWebhookRequest request, String tenantId, String userId) {
        var po = WebhookSubscriptionPO.builder()
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
        webhookMapper.insert(po);
        log.info("Webhook registered: id={}, url={}, tenant={}", po.getId(), request.getCallbackUrl(), tenantId);
        return toResponse(po);
    }

    @Override
    public List<WebhookResponse> listWebhooks(String tenantId) {
        return webhookMapper.selectActiveByTenant(tenantId).stream()
                .map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void deleteWebhook(UUID webhookId) {
        var po = webhookMapper.selectById(webhookId);
        if (po == null) {
            throw new ResourceNotFoundException("Webhook", webhookId.toString());
        }
        webhookMapper.deleteById(webhookId);
        log.info("Webhook deleted: id={}", webhookId);
    }

    @SuppressWarnings("unchecked")
    private WebhookResponse toResponse(WebhookSubscriptionPO po) {
        return WebhookResponse.builder()
                .id(po.getId())
                .tenantId(po.getTenantId())
                .agentId(po.getAgentId())
                .callbackUrl(po.getCallbackUrl())
                .eventTypes(fromJsonList(po.getEventTypes()))
                .isActive(po.getIsActive() != null && po.getIsActive())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
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

package com.ontology.platform.application.service.webhook;

import com.ontology.platform.application.dto.webhook.CreateWebhookRequest;
import com.ontology.platform.application.dto.webhook.WebhookResponse;

import java.util.List;
import java.util.UUID;

public interface WebhookService {

    WebhookResponse createWebhook(CreateWebhookRequest request, String tenantId, String userId);

    List<WebhookResponse> listWebhooks(String tenantId);

    void deleteWebhook(UUID webhookId);
}

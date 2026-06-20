package com.ontology.platform.api.controller.webhook;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.webhook.CreateWebhookRequest;
import com.ontology.platform.application.dto.webhook.WebhookResponse;
import com.ontology.platform.application.service.webhook.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Webhook subscription management API. Phase 2b / F01.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhook", description = "Webhook订阅管理API")
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping
    @Operation(summary = "注册Webhook", description = "注册一个Webhook回调地址")
    public ResponseEntity<ApiResponse<WebhookResponse>> createWebhook(
            @Valid @RequestBody CreateWebhookRequest request,
            @Parameter(description = "租户ID") @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId,
            @Parameter(description = "操作用户ID") @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {

        log.info("REST: Register webhook, url={}, tenant={}", request.getCallbackUrl(), tenantId);
        WebhookResponse response = webhookService.createWebhook(request, tenantId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "查询Webhook列表", description = "获取当前租户的所有活跃Webhook")
    public ResponseEntity<ApiResponse<List<WebhookResponse>>> listWebhooks(
            @Parameter(description = "租户ID") @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId) {

        log.debug("REST: List webhooks, tenant={}", tenantId);
        List<WebhookResponse> response = webhookService.listWebhooks(tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除Webhook", description = "删除一个Webhook订阅")
    public ResponseEntity<ApiResponse<Void>> deleteWebhook(
            @Parameter(description = "Webhook ID") @PathVariable("id") UUID id) {

        log.info("REST: Delete webhook, id={}", id);
        webhookService.deleteWebhook(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

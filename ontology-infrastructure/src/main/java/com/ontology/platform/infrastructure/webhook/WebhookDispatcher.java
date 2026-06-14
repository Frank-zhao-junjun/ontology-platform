package com.ontology.platform.infrastructure.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

/**
 * Webhook dispatcher — sends HTTP POST callbacks with HMAC-SHA256 signing.
 * Phase 2b / F01.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookDispatcher {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final int MAX_RETRIES = 3;

    /**
     * Dispatch a webhook event to a callback URL.
     *
     * @param callbackUrl destination URL
     * @param secret      HMAC secret for signing
     * @param event       event payload
     * @return true if delivered successfully
     */
    public boolean dispatch(String callbackUrl, String secret, Map<String, Object> event) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                var body = mapper.writeValueAsString(event);
                var signature = hmacSha256(secret, body);

                var req = HttpRequest.newBuilder()
                        .uri(URI.create(callbackUrl))
                        .header("Content-Type", "application/json")
                        .header("X-Webhook-Signature", signature)
                        .header("X-Webhook-Attempt", String.valueOf(attempt))
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .timeout(Duration.ofSeconds(10))
                        .build();

                var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    log.debug("Webhook delivered: url={}, attempt={}", callbackUrl, attempt);
                    return true;
                }
                log.warn("Webhook failed: url={}, status={}, attempt={}", callbackUrl, resp.statusCode(), attempt);
            } catch (Exception e) {
                log.warn("Webhook error: url={}, attempt={}, error={}", callbackUrl, attempt, e.getMessage());
            }
            if (attempt < MAX_RETRIES) {
                sleep((long) Math.pow(5, attempt) * 100); // 500ms, 2.5s, 12.5s
            }
        }
        log.error("Webhook exhausted retries: url={}", callbackUrl);
        return false;
    }

    private static String hmacSha256(String secret, String body) throws Exception {
        var mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(body.getBytes()));
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}

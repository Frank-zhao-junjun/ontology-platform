package com.ontology.platform.api.config;

import com.ontology.platform.api.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.infrastructure.metrics.PlatformMetrics;
import com.ontology.platform.infrastructure.ratelimit.RateLimiterService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Rate limiter filter using Redis Token Bucket. Phase 2b / F05.
 *
 * <p>Reads X-Tenant-Id and X-Agent-Id headers to identify the caller.
 * Applies rate limiting at three levels: AGENT, TENANT, TOOL.
 * Returns 429 Too Many Requests with X-RateLimit-* headers.</p>
 */
@Slf4j
@Component
@Order(10) // After RequestContextFilter (Order 0)
@RequiredArgsConstructor
public class RateLimiterFilter implements Filter {

    private final RateLimiterService rateLimiterService;
    private final PlatformMetrics metrics;
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final int DEFAULT_MAX_REQUESTS = 1000;
    private static final int AGENT_MAX_REQUESTS = 100;
    private static final int DEFAULT_WINDOW_SECONDS = 60;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        var request = (HttpServletRequest) req;
        var response = (HttpServletResponse) res;

        String tenantId = request.getHeader("X-Tenant-Id");
        String agentId = request.getHeader("X-Agent-Id");

        if (tenantId == null) tenantId = "default";

        // Check tenant-level rate limit
        if (!tryAcquire("TENANT", tenantId, DEFAULT_MAX_REQUESTS, DEFAULT_WINDOW_SECONDS)) {
            sendRateLimitError(response, "TENANT", tenantId);
            return;
        }

        // Check agent-level rate limit (if agent header present)
        if (agentId != null && !agentId.isBlank()) {
            if (!tryAcquire("AGENT", agentId, AGENT_MAX_REQUESTS, DEFAULT_WINDOW_SECONDS)) {
                sendRateLimitError(response, "AGENT", agentId);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean tryAcquire(String scopeType, String scopeValue, int maxRequests, int windowSeconds) {
        boolean allowed = rateLimiterService.tryAcquire(scopeType, scopeValue, maxRequests, windowSeconds);
        if (!allowed) {
            log.warn("Rate limit exceeded: scope={}, value={}", scopeType, scopeValue);
        }
        return allowed;
    }

    private void sendRateLimitError(HttpServletResponse response, String scopeType, String scopeValue)
            throws IOException {

        metrics.recordRateLimitExceeded(scopeType, scopeValue);
        long remaining = rateLimiterService.remainingTokens(scopeType, scopeValue);
        long retryAfter = rateLimiterService.ttlSeconds(scopeType, scopeValue);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("Retry-After", String.valueOf(retryAfter));

        var body = ApiResponse.error(7001,
                String.format("Rate limit exceeded for %s: %s. Retry after %d seconds",
                        scopeType.toLowerCase(), scopeValue, retryAfter));

        response.getWriter().write(mapper.writeValueAsString(body));
    }
}

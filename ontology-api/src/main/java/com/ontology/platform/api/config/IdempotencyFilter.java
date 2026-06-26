package com.ontology.platform.api.config;

import com.ontology.platform.infrastructure.idempotency.IdempotencyService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * Idempotency filter — handles Idempotency-Key header for all write operations.
 * Phase 2a / F02.
 *
 * Only active when IdempotencyService bean is available (not in dev profile without Redis).
 *
 * Supports: POST, PUT, PATCH, DELETE
 * Skips: GET, HEAD, OPTIONS (read-only, no idempotency needed)
 */
@Slf4j
@Component
@ConditionalOnBean(IdempotencyService.class)
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class IdempotencyFilter implements Filter {

    private final IdempotencyService idempotencyService;

    private static final String HEADER = "Idempotency-Key";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        var req = (HttpServletRequest) request;
        var res = (HttpServletResponse) response;

        String method = req.getMethod();
        String key = req.getHeader(HEADER);

        // Skip idempotency for read-only methods or missing header
        if (key == null || key.isBlank() || isReadOnly(method)) {
            chain.doFilter(request, response);
            return;
        }

        // Extract tenant and agent from existing context
        String tenantId = req.getHeader("X-Tenant-Id");
        if (tenantId == null) tenantId = "default";
        String agentId = req.getHeader("X-Agent-Id");

        // Check idempotency
        var result = idempotencyService.acquire(key, tenantId, agentId, method, req.getRequestURI());

        if (result.isInProgress()) {
            res.setStatus(HttpStatus.CONFLICT.value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write("{\"code\":409,\"message\":\"Request in progress\"}");
            return;
        }

        if (!result.isFirstRequest()) {
            // Return cached response
            res.setStatus(result.getCachedStatus());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write(result.getCachedBody());
            return;
        }

        // First request — wrap response to capture body, then proceed
        var wrapper = new ContentCachingResponseWrapper(res);
        try {
            chain.doFilter(request, wrapper);
            // Store completed response
            int status = wrapper.getStatus();
            String body = new String(wrapper.getContentAsByteArray(), wrapper.getCharacterEncoding());
            if (body.isEmpty()) body = "{}";
            idempotencyService.complete(key, status, body);
            // Copy cached content to actual response
            wrapper.copyBodyToResponse();
        } catch (Exception e) {
            log.error("Idempotency filter error for key={}", key, e);
            throw e;
        }
    }

    private boolean isReadOnly(String method) {
        return "GET".equalsIgnoreCase(method)
                || "HEAD".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method);
    }
}

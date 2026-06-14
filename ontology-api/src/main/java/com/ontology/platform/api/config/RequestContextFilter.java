package com.ontology.platform.api.config;

import com.ontology.platform.api.dto.RequestContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Request context filter — sets request ID, trace ID, and user context.
 * Phase 2a: extended with MDC trace_id for full-chain tracing.
 */
@Slf4j
@Component("apiRequestContextFilter")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestContextFilter implements Filter {

    private static final String TRACE_HEADER = "X-Trace-Id";
    private static final String REQUEST_HEADER = "X-Request-ID";
    private static final String MDC_TRACE_KEY = "trace_id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // Trace ID — propagate from incoming or generate new (UUID v7 style)
            String traceId = httpRequest.getHeader(TRACE_HEADER);
            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString();
            }
            MDC.put(MDC_TRACE_KEY, traceId);
            httpResponse.setHeader(TRACE_HEADER, traceId);

            // Request ID
            String requestId = httpRequest.getHeader(REQUEST_HEADER);
            if (requestId == null || requestId.isBlank()) {
                requestId = "req_" + System.currentTimeMillis() + "_" +
                        Integer.toHexString((int) (Math.random() * 0xFFFF));
            }
            RequestContext.setRequestId(requestId);
            httpResponse.setHeader(REQUEST_HEADER, requestId);

            // User ID
            String userId = httpRequest.getHeader("X-User-Id");
            if (userId != null && !userId.isBlank()) {
                RequestContext.setUserId(userId);
            }

            long startTime = System.currentTimeMillis();
            log.debug("Request started: method={}, uri={}", httpRequest.getMethod(), httpRequest.getRequestURI());

            chain.doFilter(request, response);

            long duration = System.currentTimeMillis() - startTime;
            log.debug("Request completed: method={}, uri={}, status={}, duration={}ms",
                    httpRequest.getMethod(), httpRequest.getRequestURI(),
                    httpResponse.getStatus(), duration);

        } finally {
            RequestContext.clear();
            MDC.remove(MDC_TRACE_KEY);
        }
    }
}

package com.ontology.platform.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Set;

/**
 * 可选 API Key 认证（Sprint 2 基础框架）。h2 测试 profile 默认关闭。
 */
@Component
@ConditionalOnProperty(name = "ontology.api.auth.enabled", havingValue = "true")
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private static final Set<String> SKIP_PREFIXES = Set.of("/actuator", "/v3/api-docs", "/swagger-ui");

    @Value("${ontology.api.auth.api-key:}")
    private String expectedApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        for (String prefix : SKIP_PREFIXES) {
            if (path.contains(prefix)) {
                chain.doFilter(request, response);
                return;
            }
        }
        String provided = request.getHeader("X-Api-Key");
        if (expectedApiKey == null || expectedApiKey.isBlank()
                || expectedApiKey.equals(provided)) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"code\":401,\"message\":\"Invalid or missing X-Api-Key\"}");
    }
}

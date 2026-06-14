package com.ontology.platform.api.config;

import com.ontology.platform.infrastructure.metrics.PlatformMetrics;
import com.ontology.platform.infrastructure.ratelimit.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RateLimiterFilter}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimiterFilter Unit Tests")
class RateLimiterFilterTest {

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private PlatformMetrics metrics;

    private RateLimiterFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RateLimiterFilter(rateLimiterService, metrics);
        lenient().when(request.getMethod()).thenReturn("GET");
        lenient().when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    @Nested
    @DisplayName("Rate limit - pass through")
    class PassThroughTests {

        @Test
        @DisplayName("should pass when under tenant limit")
        void passUnderTenantLimit() throws Exception {
            when(request.getHeader("X-Tenant-Id")).thenReturn("tenant-1");
            when(rateLimiterService.tryAcquire(eq("TENANT"), eq("tenant-1"), anyInt(), anyInt()))
                    .thenReturn(true);

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }

        @Test
        @DisplayName("should pass when under both tenant and agent limits")
        void passUnderBothLimits() throws Exception {
            when(request.getHeader("X-Tenant-Id")).thenReturn("tenant-1");
            when(request.getHeader("X-Agent-Id")).thenReturn("agent-42");
            when(rateLimiterService.tryAcquire(eq("TENANT"), eq("tenant-1"), anyInt(), anyInt()))
                    .thenReturn(true);
            when(rateLimiterService.tryAcquire(eq("AGENT"), eq("agent-42"), anyInt(), anyInt()))
                    .thenReturn(true);

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }

        @Test
        @DisplayName("should skip agent check when no agent header")
        void skipAgentCheckWhenNoHeader() throws Exception {
            when(request.getHeader("X-Tenant-Id")).thenReturn("tenant-1");
            when(request.getHeader("X-Agent-Id")).thenReturn(null);
            when(rateLimiterService.tryAcquire(eq("TENANT"), eq("tenant-1"), anyInt(), anyInt()))
                    .thenReturn(true);

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verify(rateLimiterService, never()).tryAcquire(eq("AGENT"), anyString(), anyInt(), anyInt());
        }
    }

    @Nested
    @DisplayName("Rate limit - blocked (429)")
    class BlockedTests {

        @Test
        @DisplayName("should return 429 when tenant exceeds limit")
        void blockTenantExceeded() throws Exception {
            when(request.getHeader("X-Tenant-Id")).thenReturn("tenant-1");
            when(rateLimiterService.tryAcquire(eq("TENANT"), eq("tenant-1"), anyInt(), anyInt()))
                    .thenReturn(false);
            when(rateLimiterService.remainingTokens("TENANT", "tenant-1")).thenReturn(0L);
            when(rateLimiterService.ttlSeconds("TENANT", "tenant-1")).thenReturn(30L);

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            when(response.getWriter()).thenReturn(pw);

            filter.doFilter(request, response, chain);

            verify(response).setStatus(429);
            verify(response).setHeader("Retry-After", "30");
            verify(response).setHeader("X-RateLimit-Remaining", "0");
            verify(chain, never()).doFilter(request, response);
        }

        @Test
        @DisplayName("should return 429 when agent exceeds limit")
        void blockAgentExceeded() throws Exception {
            when(request.getHeader("X-Tenant-Id")).thenReturn("tenant-1");
            when(request.getHeader("X-Agent-Id")).thenReturn("agent-42");
            when(rateLimiterService.tryAcquire(eq("TENANT"), eq("tenant-1"), anyInt(), anyInt()))
                    .thenReturn(true);
            when(rateLimiterService.tryAcquire(eq("AGENT"), eq("agent-42"), anyInt(), anyInt()))
                    .thenReturn(false);
            when(rateLimiterService.remainingTokens("AGENT", "agent-42")).thenReturn(0L);
            when(rateLimiterService.ttlSeconds("AGENT", "agent-42")).thenReturn(15L);

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            when(response.getWriter()).thenReturn(pw);

            filter.doFilter(request, response, chain);

            verify(response).setStatus(429);
            verify(response).setHeader("Retry-After", "15");
            verify(chain, never()).doFilter(request, response);
        }

        @Test
        @DisplayName("should default tenant to 'default' when header absent")
        void defaultTenantWhenMissing() throws Exception {
            when(request.getHeader("X-Tenant-Id")).thenReturn(null);
            when(rateLimiterService.tryAcquire(eq("TENANT"), eq("default"), anyInt(), anyInt()))
                    .thenReturn(true);

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }
    }
}
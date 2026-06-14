package com.ontology.platform.api.config;

import com.ontology.platform.infrastructure.idempotency.IdempotencyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link IdempotencyFilter}.
 * <p>
 * Validates idempotency key handling: skip for read-only / missing key,
 * 409 for in-progress requests, 200 for cached responses,
 * and wrap-then-complete for first requests.
 */
@DisplayName("IdempotencyFilter Unit Tests")
@ExtendWith(MockitoExtension.class)
class IdempotencyFilterTest {

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private FilterChain chain;

    private IdempotencyFilter filter;

    private static final String KEY = "idem-key-999";

    @BeforeEach
    void setUp() {
        filter = new IdempotencyFilter(idempotencyService);
    }

    @Nested
    @DisplayName("Skip conditions — no idempotency check")
    class SkipTests {

        @Test
        @DisplayName("missing Idempotency-Key → chain.doFilter, no service call")
        void noKey_skips() throws Exception {
            when(request.getHeader("Idempotency-Key")).thenReturn(null);
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verifyNoInteractions(idempotencyService);
        }

        @Test
        @DisplayName("GET method → chain.doFilter, no service call")
        void readOnly_skips() throws Exception {
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Idempotency-Key")).thenReturn(KEY);
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verifyNoInteractions(idempotencyService);
        }

        @Test
        @DisplayName("HEAD method → chain.doFilter")
        void headMethod_skips() throws Exception {
            when(request.getMethod()).thenReturn("HEAD");
            when(request.getHeader("Idempotency-Key")).thenReturn(KEY);
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verifyNoInteractions(idempotencyService);
        }

        @Test
        @DisplayName("OPTIONS method → chain.doFilter")
        void optionsMethod_skips() throws Exception {
            when(request.getMethod()).thenReturn("OPTIONS");
            when(request.getHeader("Idempotency-Key")).thenReturn(KEY);
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verifyNoInteractions(idempotencyService);
        }
    }

    @Nested
    @DisplayName("In-progress handling — 409 Conflict")
    class InProgressTests {

        @Test
        @DisplayName("acquire returns inProgress → 409 + JSON body, no chain")
        void inProgress_409() throws Exception {
            when(request.getMethod()).thenReturn("POST");
            when(request.getHeader("Idempotency-Key")).thenReturn(KEY);
            when(request.getHeader("X-Tenant-Id")).thenReturn("default");
            when(request.getHeader("X-Agent-Id")).thenReturn(null);
            when(request.getRequestURI()).thenReturn("/api/ontologies");
            when(idempotencyService.acquire(KEY, "default", null, "POST", "/api/ontologies"))
                    .thenReturn(IdempotencyService.IdempotencyResult.inProgress());

            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, chain);

            assertThat(response.getStatus()).isEqualTo(409);
            assertThat(response.getContentAsString()).contains("Request in progress");
            verify(chain, never()).doFilter(any(), any());
        }
    }

    @Nested
    @DisplayName("Cached response — 200 with cached body")
    class CachedTests {

        @Test
        @DisplayName("acquire returns completed → 200 + cached body, no chain")
        void cached_200() throws Exception {
            when(request.getMethod()).thenReturn("PUT");
            when(request.getHeader("Idempotency-Key")).thenReturn(KEY);
            when(request.getHeader("X-Tenant-Id")).thenReturn("default");
            when(request.getHeader("X-Agent-Id")).thenReturn(null);
            when(request.getRequestURI()).thenReturn("/api/ontologies/1");
            when(idempotencyService.acquire(KEY, "default", null, "PUT", "/api/ontologies/1"))
                    .thenReturn(IdempotencyService.IdempotencyResult.completed(200, "{\"id\":1}"));

            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, chain);

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getContentAsString()).isEqualTo("{\"id\":1}");
            verify(chain, never()).doFilter(any(), any());
        }
    }

    @Nested
    @DisplayName("First request — execute chain, then complete")
    class FirstRequestTests {

        @Test
        @DisplayName("acquire returns firstRequest → wrap response, chain, complete")
        void firstRequest_executes() throws Exception {
            // Arrange
            when(request.getMethod()).thenReturn("POST");
            when(request.getHeader("Idempotency-Key")).thenReturn(KEY);
            when(request.getHeader("X-Tenant-Id")).thenReturn("tenant-1");
            when(request.getHeader("X-Agent-Id")).thenReturn("agent-1");
            when(request.getRequestURI()).thenReturn("/api/ontologies");
            when(idempotencyService.acquire(KEY, "tenant-1", "agent-1", "POST", "/api/ontologies"))
                    .thenReturn(IdempotencyService.IdempotencyResult.firstRequest());

            MockHttpServletResponse response = new MockHttpServletResponse();

            // We need the chain.doFilter to actually write something so the wrapper has content
            doAnswer(invocation -> {
                // The filter passes a ContentCachingResponseWrapper to chain.doFilter
                // We need to capture it and write a response body
                var wrapper = (org.springframework.web.util.ContentCachingResponseWrapper) invocation.getArgument(1);
                wrapper.setStatus(201);
                wrapper.getWriter().write("{\"id\":42}");
                wrapper.flushBuffer();
                return null;
            }).when(chain).doFilter(any(), any());

            // Act
            filter.doFilter(request, response, chain);

            // Assert — response was copied to the original response
            assertThat(response.getStatus()).isEqualTo(201);
            assertThat(response.getContentAsString()).isEqualTo("{\"id\":42}");

            // Assert — complete was called with the captured status and body
            verify(idempotencyService).complete(KEY, 201, "{\"id\":42}");
        }

        @Test
        @DisplayName("firstRequest with empty body → body defaults to {}")
        void firstRequest_emptyBody() throws Exception {
            when(request.getMethod()).thenReturn("POST");
            when(request.getHeader("Idempotency-Key")).thenReturn(KEY);
            when(request.getHeader("X-Tenant-Id")).thenReturn("default");
            when(request.getHeader("X-Agent-Id")).thenReturn(null);
            when(request.getRequestURI()).thenReturn("/api/ontologies");
            when(idempotencyService.acquire(KEY, "default", null, "POST", "/api/ontologies"))
                    .thenReturn(IdempotencyService.IdempotencyResult.firstRequest());

            MockHttpServletResponse response = new MockHttpServletResponse();

            // chain writes nothing — empty body
            doAnswer(invocation -> {
                var wrapper = (org.springframework.web.util.ContentCachingResponseWrapper) invocation.getArgument(1);
                wrapper.setStatus(204);
                // deliberately do NOT write any body content
                return null;
            }).when(chain).doFilter(any(), any());

            filter.doFilter(request, response, chain);

            assertThat(response.getStatus()).isEqualTo(204);
            verify(idempotencyService).complete(eq(KEY), eq(204), eq("{}"));
        }
    }
}

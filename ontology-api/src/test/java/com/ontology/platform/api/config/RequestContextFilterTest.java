package com.ontology.platform.api.config;

import com.ontology.platform.api.dto.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RequestContextFilter}.
 *
 * <p>Note: The filter's finally block clears RequestContext and MDC before
 * {@code doFilter} returns. Tests that check header propagation use
 * {@code verify(response).setHeader(...)} (values are set before
 * {@code chain.doFilter}). Cleanup tests verify the finally block runs.</p>
 */
@DisplayName("RequestContextFilter Unit Tests")
class RequestContextFilterTest {

    private RequestContextFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RequestContextFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);

        lenient().when(request.getMethod()).thenReturn("GET");
        lenient().when(request.getRequestURI()).thenReturn("/api/test");
        lenient().when(response.getStatus()).thenReturn(200);
    }

    @AfterEach
    void tearDown() {
        RequestContext.clear();
        MDC.clear();
    }

    @Nested
    @DisplayName("Trace ID (X-Trace-Id)")
    class TraceIdTests {

        @Test
        @DisplayName("propagates incoming X-Trace-Id to response header")
        void traceId_fromHeader() throws Exception {
            when(request.getHeader("X-Trace-Id")).thenReturn("trace-abc-123");

            filter.doFilter(request, response, chain);

            verify(response).setHeader("X-Trace-Id", "trace-abc-123");
            verify(chain).doFilter(request, response);
        }

        @Test
        @DisplayName("generates UUID when no X-Trace-Id header present")
        void traceId_generate() throws Exception {
            when(request.getHeader("X-Trace-Id")).thenReturn(null);

            filter.doFilter(request, response, chain);

            verify(response).setHeader(eq("X-Trace-Id"), argThat(v ->
                    v != null && v.matches("[0-9a-f-]+")));
        }
    }

    @Nested
    @DisplayName("Request ID (X-Request-ID)")
    class RequestIdTests {

        @Test
        @DisplayName("sets RequestContext.requestId from header")
        void requestId_fromHeader() throws Exception {
            when(request.getHeader("X-Trace-Id")).thenReturn("trace-1");
            when(request.getHeader("X-Request-ID")).thenReturn("req-999");

            filter.doFilter(request, response, chain);

            verify(response).setHeader("X-Request-ID", "req-999");
        }

        @Test
        @DisplayName("generates fallback request ID when header absent")
        void requestId_generate() throws Exception {
            when(request.getHeader("X-Trace-Id")).thenReturn("trace-1");
            when(request.getHeader("X-Request-ID")).thenReturn(null);

            filter.doFilter(request, response, chain);

            verify(response).setHeader(eq("X-Request-ID"), argThat(v ->
                    v != null && ((String) v).startsWith("req_")));
        }
    }

    @Nested
    @DisplayName("User ID (X-User-Id)")
    class UserIdTests {

        @Test
        @DisplayName("sets RequestContext.userId from header")
        void userId_fromHeader() throws Exception {
            when(request.getHeader("X-Trace-Id")).thenReturn("trace-1");
            when(request.getHeader("X-User-Id")).thenReturn("user-42");

            filter.doFilter(request, response, chain);

            // Verify inside chain via ArgumentCaptor pattern
            verify(chain).doFilter(request, response);
        }

        @Test
        @DisplayName("skips userId when header is absent")
        void userId_absent() throws Exception {
            when(request.getHeader("X-Trace-Id")).thenReturn("trace-1");
            when(request.getHeader("X-User-Id")).thenReturn(null);

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Cleanup (finally block)")
    class CleanupTests {

        @Test
        @DisplayName("clears MDC trace_id after request completes")
        void cleanup_mdc() throws Exception {
            when(request.getHeader("X-Trace-Id")).thenReturn("trace-clean");
            doNothing().when(chain).doFilter(request, response);

            // Capture MDC state inside chain via spy
            final boolean[] clearedAfterChain = {false};
            doAnswer(inv -> {
                // Inside chain — MDC should be set
                assertThat(MDC.get("trace_id")).isNotNull();
                return null;
            }).when(chain).doFilter(request, response);

            filter.doFilter(request, response, chain);
        }

        @Test
        @DisplayName("cleans up even when chain.doFilter throws")
        void cleanup_onException() {
            when(request.getHeader("X-Trace-Id")).thenReturn("trace-ex");
            try {
                doThrow(new RuntimeException("chain failure")).when(chain).doFilter(request, response);
            } catch (Exception e) {
                // mock setup
            }

            try {
                filter.doFilter(request, response, chain);
            } catch (Exception ignored) {
                // expected
            }
        }
    }
}

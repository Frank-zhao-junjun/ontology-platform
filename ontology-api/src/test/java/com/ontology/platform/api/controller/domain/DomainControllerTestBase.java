package com.ontology.platform.api.controller.domain;

import com.ontology.platform.infrastructure.idempotency.IdempotencyService;
import com.ontology.platform.infrastructure.metrics.PlatformMetrics;
import com.ontology.platform.infrastructure.ratelimit.RateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * domain Controller @WebMvcTest 基类。
 *
 * 为自动注册的 IdempotencyFilter / RateLimiterFilter 提供依赖的 mock bean，
 * 避免 ApplicationContext 因找不到 infrastructure Service 而启动失败。
 */
public abstract class DomainControllerTestBase {

    @MockBean
    protected IdempotencyService idempotencyService;

    @MockBean
    protected RateLimiterService rateLimiterService;

    @MockBean
    protected PlatformMetrics platformMetrics;

    @BeforeEach
    void setupFilterMocks() {
        when(idempotencyService.acquire(anyString(), anyString(), any(), anyString(), anyString()))
                .thenReturn(IdempotencyService.IdempotencyResult.firstRequest());
        when(rateLimiterService.tryAcquire(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(true);
    }
}

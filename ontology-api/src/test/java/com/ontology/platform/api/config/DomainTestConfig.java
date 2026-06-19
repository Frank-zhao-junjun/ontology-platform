package com.ontology.platform.api.config;

import com.ontology.platform.infrastructure.idempotency.IdempotencyService;
import com.ontology.platform.infrastructure.metrics.PlatformMetrics;
import com.ontology.platform.infrastructure.ratelimit.RateLimiterService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 为 @WebMvcTest 的 domain controller 测试提供 Filter 依赖的 mock bean。
 *
 * IdempotencyFilter / RateLimiterFilter 在 Spring Web 层会自动注册，
 * 但 @WebMvcTest 切片不会加载 infrastructure 层的真实 Service。
 * 通过本配置注入 mock，避免 ApplicationContext 启动失败。
 */
@TestConfiguration
public class DomainTestConfig {

    @Bean
    public IdempotencyService idempotencyService() {
        return Mockito.mock(IdempotencyService.class);
    }

    @Bean
    public RateLimiterService rateLimiterService() {
        return Mockito.mock(RateLimiterService.class);
    }

    @Bean
    public PlatformMetrics platformMetrics() {
        return Mockito.mock(PlatformMetrics.class);
    }
}

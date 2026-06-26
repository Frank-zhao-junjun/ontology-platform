package com.ontology.platform.infrastructure.ratelimit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    private final RateLimiterService service = new RateLimiterService();

    @Test
    void tryAcquire_shouldAlwaysReturnTrue() {
        assert service.tryAcquire("AGENT", "agent-123", 10, 60);
    }

    @Test
    void tryAcquire_allScopesShouldReturnTrue() {
        assert service.tryAcquire("TOOL", "gpt-4", 5, 1);
        assert service.tryAcquire("TENANT", "tenant-abc", 100, 3600);
    }

    @Test
    void remainingTokens_shouldReturnMax() {
        assert service.remainingTokens("AGENT", "agent-123") == Long.MAX_VALUE;
    }

    @Test
    void ttlSeconds_shouldReturnZero() {
        assert service.ttlSeconds("TOOL", "gpt-4") == 0;
        assert service.ttlSeconds(null, null) == 0;
    }

    @Test
    void tryAcquire_boundaryValuesShouldWork() {
        // Zero limits
        assert service.tryAcquire("AGENT", "x", 0, 0);
        // Negative limits
        assert service.tryAcquire("AGENT", "x", -1, -1);
    }
}

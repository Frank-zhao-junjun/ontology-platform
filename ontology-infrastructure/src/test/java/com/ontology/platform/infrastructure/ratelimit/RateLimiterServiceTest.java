package com.ontology.platform.infrastructure.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private HashOperations<String, Object, Object> hashOps;

    private RateLimiterService service;

    @BeforeEach
    void setUp() {
        service = new RateLimiterService(redis);
    }

    @Test
    void tryAcquire_allowed_shouldReturnTrue() {
        var result = 1L;
        when(redis.execute(any(), anyList(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(result);

        var allowed = service.tryAcquire("AGENT", "agent-123", 10, 60);

        assertTrue(allowed);
    }

    @Test
    void tryAcquire_blocked_shouldReturnFalse() {
        var result = 0L;
        when(redis.execute(any(), anyList(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(result);

        var allowed = service.tryAcquire("AGENT", "agent-123", 10, 60);

        assertFalse(allowed);
    }

    @Test
    void tryAcquire_nullResult_shouldReturnFalse() {
        when(redis.execute(any(), anyList(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null);

        var allowed = service.tryAcquire("TOOL", "gpt-4", 5, 1);

        assertFalse(allowed);
    }

    @Test
    void tryAcquire_shouldBuildCorrectKeyAndArgs() {
        var result = 1L;
        when(redis.execute(any(), anyList(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(result);

        service.tryAcquire("TENANT", "tenant-abc", 100, 3600);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<String> argsCaptor1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argsCaptor2 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argsCaptor3 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argsCaptor4 = ArgumentCaptor.forClass(String.class);

        verify(redis).execute(any(), keysCaptor.capture(),
                argsCaptor1.capture(), argsCaptor2.capture(),
                argsCaptor3.capture(), argsCaptor4.capture());

        // Key should be lowercase scopeType + scopeValue
        assertEquals(List.of("ratelimit:tenant:tenant-abc"), keysCaptor.getValue());

        // maxRequests = 100
        assertEquals("100", argsCaptor1.getValue());

        // rate = maxRequests / windowSeconds * 1000 = 100 / 3600 * 1000 ≈ 27.78 → cast to long = 27
        assertEquals("27", argsCaptor2.getValue());

        // now is a timestamp (non-null, numeric)
        assertNotNull(argsCaptor3.getValue());
        assertTrue(Long.parseLong(argsCaptor3.getValue()) > 0);

        // requested = 1
        assertEquals("1", argsCaptor4.getValue());
    }

    @Test
    void remainingTokens_shouldReturnValueFromRedis() {
        var key = "ratelimit:agent:agent-123";
        when(redis.opsForHash()).thenReturn(hashOps);
        when(hashOps.get(key, "tokens")).thenReturn("5");

        var remaining = service.remainingTokens("AGENT", "agent-123");

        assertEquals(5L, remaining);
    }

    @Test
    void remainingTokens_shouldReturnZeroWhenNull() {
        var key = "ratelimit:agent:agent-123";
        when(redis.opsForHash()).thenReturn(hashOps);
        when(hashOps.get(key, "tokens")).thenReturn(null);

        var remaining = service.remainingTokens("AGENT", "agent-123");

        assertEquals(0L, remaining);
    }

    @Test
    void ttlSeconds_shouldReturnValueFromRedis() {
        var key = "ratelimit:tool:gpt-4";
        when(redis.getExpire(key)).thenReturn(42L);

        var ttl = service.ttlSeconds("TOOL", "gpt-4");

        assertEquals(42L, ttl);
    }

    @Test
    void ttlSeconds_shouldReturnZeroWhenNull() {
        var key = "ratelimit:tool:gpt-4";
        when(redis.getExpire(key)).thenReturn(null);

        var ttl = service.ttlSeconds("TOOL", "gpt-4");

        assertEquals(0L, ttl);
    }
}

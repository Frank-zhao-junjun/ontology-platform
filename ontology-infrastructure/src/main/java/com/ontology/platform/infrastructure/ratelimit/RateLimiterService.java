package com.ontology.platform.infrastructure.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * Token Bucket rate limiter using Redis Lua. Phase 2b / F05.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final StringRedisTemplate redis;

    private static final DefaultRedisScript<Long> TOKEN_BUCKET_SCRIPT;

    static {
        TOKEN_BUCKET_SCRIPT = new DefaultRedisScript<>("""
            local key = KEYS[1]
            local capacity = tonumber(ARGV[1])
            local rate = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            local requested = tonumber(ARGV[4])

            local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
            local tokens = tonumber(bucket[1]) or capacity
            local last_refill = tonumber(bucket[2]) or now

            local elapsed = math.max(0, now - last_refill)
            local refill = math.floor(elapsed * rate / 1000)
            tokens = math.min(capacity, tokens + refill)
            last_refill = now

            local allowed = 0
            if tokens >= requested then
                tokens = tokens - requested
                allowed = 1
            end

            redis.call('HMSET', key, 'tokens', tokens, 'last_refill', last_refill)
            redis.call('EXPIRE', key, math.ceil(capacity / rate) + 1)
            return allowed
            """, Long.class);
    }

    /**
     * Try to acquire tokens. Returns true if allowed, false if rate limited.
     *
     * @param scopeType  AGENT, TOOL, TENANT
     * @param scopeValue agent-123, tool-name, tenant-456
     * @param maxRequests max requests per window (capacity)
     * @param windowSeconds window in seconds
     */
    public boolean tryAcquire(String scopeType, String scopeValue,
                               int maxRequests, int windowSeconds) {
        var key = "ratelimit:" + scopeType.toLowerCase() + ":" + scopeValue;
        var now = System.currentTimeMillis();
        var rate = (double) maxRequests / windowSeconds * 1000;

        var result = redis.execute(TOKEN_BUCKET_SCRIPT,
                List.of(key),
                String.valueOf(maxRequests),
                String.valueOf((long) rate),
                String.valueOf(now),
                "1");

        return result != null && result == 1L;
    }

    /**
     * Get remaining tokens (for X-RateLimit-Remaining header).
     */
    public long remainingTokens(String scopeType, String scopeValue) {
        var key = "ratelimit:" + scopeType.toLowerCase() + ":" + scopeValue;
        var tokens = redis.opsForHash().get(key, "tokens");
        return tokens != null ? Long.parseLong(tokens.toString()) : 0;
    }

    /**
     * Get TTL in seconds (for Retry-After header).
     */
    public long ttlSeconds(String scopeType, String scopeValue) {
        var key = "ratelimit:" + scopeType.toLowerCase() + ":" + scopeValue;
        var ttl = redis.getExpire(key);
        return ttl != null ? ttl : 0;
    }
}

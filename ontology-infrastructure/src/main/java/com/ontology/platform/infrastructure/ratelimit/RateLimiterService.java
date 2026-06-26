package com.ontology.platform.infrastructure.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * No-op rate limiter — Redis not required for dev.
 * Returns true for all acquire attempts (no rate limiting).
 */
@Slf4j
@Service
public class RateLimiterService {

    public boolean tryAcquire(String scopeType, String scopeValue,
                               int maxRequests, int windowSeconds) {
        return true; // no-op: always allowed
    }

    public long remainingTokens(String scopeType, String scopeValue) {
        return Long.MAX_VALUE;
    }

    public long ttlSeconds(String scopeType, String scopeValue) {
        return 0;
    }
}

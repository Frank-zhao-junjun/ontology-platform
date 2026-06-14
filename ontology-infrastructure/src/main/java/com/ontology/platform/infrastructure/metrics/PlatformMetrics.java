package com.ontology.platform.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Custom application metrics. Phase 2c / F03.
 */
@Component
@RequiredArgsConstructor
public class PlatformMetrics {

    private final MeterRegistry registry;

    private final ConcurrentHashMap<String, Counter> toolCallCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> toolCallTimers = new ConcurrentHashMap<>();
    private final AtomicLong jobQueueSize = new AtomicLong(0);

    @PostConstruct
    void init() {
        Gauge.builder("job_queue_size", jobQueueSize, AtomicLong::get)
                .description("Current job queue depth")
                .register(registry);
    }

    // === MCP Tool Calls ===

    public void recordToolCall(String tool, String agentId, String status, long durationMs) {
        toolCallCounters.computeIfAbsent(tool + ":" + status, k ->
                Counter.builder("mcp_tools_calls_total")
                        .description("Total MCP tool calls")
                        .tag("tool", tool)
                        .tag("agent_id", agentId != null ? agentId : "unknown")
                        .tag("status", status)
                        .register(registry)
        ).increment();

        toolCallTimers.computeIfAbsent(tool, k ->
                Timer.builder("mcp_tools_calls_duration")
                        .description("MCP tool call duration")
                        .tag("tool", tool)
                        .register(registry)
        ).record(java.time.Duration.ofMillis(durationMs));
    }

    // === Job Execution ===

    public void recordJobExecution(String jobType, String status) {
        Counter.builder("job_execution_total")
                .description("Total job executions")
                .tag("job_type", jobType)
                .tag("status", status)
                .register(registry)
                .increment();
    }

    // === Job Queue ===

    public void setJobQueueSize(long size) { jobQueueSize.set(size); }

    // === Rate Limiting ===

    public void recordRateLimitExceeded(String scopeType, String scopeValue) {
        Counter.builder("rate_limit_exceeded_total")
                .description("Rate limit exceeded count")
                .tag("scope_type", scopeType)
                .tag("scope_value", scopeValue)
                .register(registry)
                .increment();
    }

    // === Idempotency ===

    public void recordIdempotencyHit() {
        Counter.builder("idempotency_hit_total")
                .description("Idempotency cache hit count")
                .register(registry)
                .increment();
    }
}

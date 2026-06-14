package com.ontology.platform.infrastructure.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobQueueService {

    private final StringRedisTemplate redis;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String QUEUE_KEY = "job:queue";
    private static final String PENDING_KEY = "job:pending";
    private static final Duration POP_TIMEOUT = Duration.ofSeconds(5);

    public String enqueue(String jobType, Map<String, Object> payload, String tenantId, String agentId) {
        var jobId = UUID.randomUUID().toString();
        try {
            var job = Map.of("id", jobId, "jobType", jobType,
                    "tenantId", tenantId, "agentId", agentId != null ? agentId : "",
                    "payload", payload);
            redis.opsForList().leftPush(QUEUE_KEY, mapper.writeValueAsString(job));
            log.debug("Job enqueued: id={}, type={}", jobId, jobType);
        } catch (Exception e) {
            throw new JobQueueException("Failed to enqueue job", e);
        }
        return jobId;
    }

    public JobMessage dequeue() {
        try {
            var raw = redis.opsForList().rightPopAndLeftPush(QUEUE_KEY, PENDING_KEY, POP_TIMEOUT);
            if (raw == null) return null;
            return parse(raw);
        } catch (Exception e) {
            log.error("Dequeue failed", e);
            return null;
        }
    }

    public void ack(String jobId, String raw) {
        redis.opsForList().remove(PENDING_KEY, 1, raw);
        log.debug("Job acked: {}", jobId);
    }

    public void retry(String raw) {
        redis.opsForList().remove(PENDING_KEY, 1, raw);
        redis.opsForList().leftPush(QUEUE_KEY, raw);
    }

    public long queueSize() {
        var s = redis.opsForList().size(QUEUE_KEY);
        return s != null ? s : 0;
    }

    @SuppressWarnings("unchecked")
    private JobMessage parse(String raw) throws Exception {
        var map = mapper.readValue(raw, Map.class);
        return new JobMessage((String) map.get("id"), (String) map.get("jobType"),
                (String) map.get("tenantId"), (String) map.get("agentId"),
                (Map<String, Object>) map.get("payload"), raw);
    }

    public record JobMessage(String id, String jobType, String tenantId,
                              String agentId, Map<String, Object> payload, String raw) {}

    public static class JobQueueException extends RuntimeException {
        public JobQueueException(String msg, Throwable cause) { super(msg, cause); }
    }
}

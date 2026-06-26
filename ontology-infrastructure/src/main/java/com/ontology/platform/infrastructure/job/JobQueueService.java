package com.ontology.platform.infrastructure.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * No-op job queue — Redis not adopted, all jobs acknowledged immediately.
 * Scheduled polling via {@link JobPoller} returns no messages.
 */
@Slf4j
@Service
public class JobQueueService {

    public String enqueue(String jobType, Map<String, Object> payload, String tenantId, String agentId) {
        var jobId = UUID.randomUUID().toString();
        log.debug("Job enqueued (no-op): id={}, type={}", jobId, jobType);
        return jobId;
    }

    public JobMessage dequeue() {
        return null; // queue always empty
    }

    public void ack(String jobId, String raw) {
        log.debug("Job acked (no-op): {}", jobId);
    }

    public void retry(String raw) {
        log.debug("Job retry (no-op)");
    }

    public long queueSize() {
        return 0;
    }

    public record JobMessage(String id, String jobType, String tenantId,
                              String agentId, Map<String, Object> payload, String raw) {}

    public static class JobQueueException extends RuntimeException {
        public JobQueueException(String msg, Throwable cause) { super(msg, cause); }
    }
}

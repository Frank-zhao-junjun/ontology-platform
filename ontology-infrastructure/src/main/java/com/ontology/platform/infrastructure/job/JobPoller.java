package com.ontology.platform.infrastructure.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.service.JobHandler;
import com.ontology.platform.infrastructure.persistence.JobRecordPO;
import com.ontology.platform.infrastructure.persistence.JobRecordPOMapper;
import com.ontology.platform.infrastructure.persistence.WebhookSubscriptionPOMapper;
import com.ontology.platform.infrastructure.metrics.PlatformMetrics;
import com.ontology.platform.infrastructure.webhook.WebhookDispatcher;
import com.ontology.platform.infrastructure.job.JobQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Scheduled poller that dequeues jobs from Redis and executes them
 * via registered {@link JobHandler} implementations, then dispatches
 * webhook callbacks on completion/failure.
 *
 * <p>Runs every 2 seconds. Each poll processes up to 3 jobs. Failed jobs
 * are retried up to {@code maxRetries} times with exponential backoff.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobPoller {

    private final JobQueueService jobQueueService;
    private final JobRecordPOMapper jobRecordMapper;
    private final WebhookSubscriptionPOMapper webhookMapper;
    private final WebhookDispatcher webhookDispatcher;
    private final PlatformMetrics metrics;
    private final List<JobHandler> handlers;
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final int MAX_JOBS_PER_POLL = 3;

    @Scheduled(fixedDelay = 2000)
    public void poll() {
        metrics.setJobQueueSize(jobQueueService.queueSize());
        for (int i = 0; i < MAX_JOBS_PER_POLL; i++) {
            var msg = jobQueueService.dequeue();
            if (msg == null) break; // queue empty
            processJob(msg);
        }
    }

    private void processJob(JobQueueService.JobMessage msg) {
        var jobId = UUID.fromString(msg.id());
        log.info("Processing job: id={}, type={}", jobId, msg.jobType());

        // Mark as RUNNING
        jobRecordMapper.updateById(getRunningUpdate(jobId));

        try {
            var handler = findHandler(msg.jobType());
            if (handler == null) {
                failJob(jobId, "No handler registered for job type: " + msg.jobType(), null);
                jobQueueService.ack(msg.id(), msg.raw());
                return;
            }

            Map<String, Object> result = handler.execute(msg.payload(), msg.tenantId(), msg.agentId());

            // Success
            completeJob(jobId, result);
            jobQueueService.ack(msg.id(), msg.raw());
            metrics.recordJobExecution(msg.jobType(), "COMPLETED");
            log.info("Job completed: id={}, type={}", jobId, msg.jobType());

        } catch (Exception e) {
            log.error("Job failed: id={}, type={}", jobId, msg.jobType(), e);
            var po = jobRecordMapper.selectById(jobId);
            int retries = po != null && po.getRetryCount() != null ? po.getRetryCount() : 0;
            int maxRetries = po != null && po.getMaxRetries() != null ? po.getMaxRetries() : 3;

            if (retries < maxRetries) {
                // Retry
                jobRecordMapper.updateById(getRetryUpdate(jobId, retries + 1));
                jobQueueService.retry(msg.raw());
                log.info("Job retry scheduled: id={}, attempt={}/{}", jobId, retries + 1, maxRetries);
            } else {
                failJob(jobId, e.getMessage(), jobId);
                metrics.recordJobExecution(msg.jobType(), "FAILED");
                jobQueueService.ack(msg.id(), msg.raw());
            }
        }
    }

    private JobHandler findHandler(String jobType) {
        return handlers.stream()
                .filter(h -> h.supportedJobType().equals(jobType))
                .findFirst().orElse(null);
    }

    private JobRecordPO getRunningUpdate(UUID jobId) {
        var po = new JobRecordPO();
        po.setId(jobId);
        po.setStatus("RUNNING");
        po.setStartedAt(Instant.now());
        return po;
    }

    private void completeJob(UUID jobId, Map<String, Object> result) {
        var po = new JobRecordPO();
        po.setId(jobId);
        po.setStatus("COMPLETED");
        po.setResult(toJson(result));
        po.setCompletedAt(Instant.now());
        jobRecordMapper.updateById(po);

        dispatchWebhooks(jobId, "job.completed", result);
    }

    private void failJob(UUID jobId, String errorMessage, Object ctx) {
        var po = new JobRecordPO();
        po.setId(jobId);
        po.setStatus("FAILED");
        po.setErrorMessage(errorMessage);
        po.setCompletedAt(Instant.now());
        jobRecordMapper.updateById(po);

        dispatchWebhooks(jobId, "job.failed", Map.of("error", errorMessage != null ? errorMessage : "unknown"));
    }

    private JobRecordPO getRetryUpdate(UUID jobId, int retryCount) {
        var po = new JobRecordPO();
        po.setId(jobId);
        po.setRetryCount(retryCount);
        po.setNextRetryAt(Instant.now().plusSeconds(30));
        return po;
    }

    private void dispatchWebhooks(UUID jobId, String eventType, Map<String, Object> result) {
        try {
            var subscriptions = webhookMapper.selectByEventType(eventType);
            if (subscriptions.isEmpty()) return;

            var event = Map.<String, Object>of(
                    "eventType", eventType,
                    "jobId", jobId.toString(),
                    "timestamp", Instant.now().toString(),
                    "result", result != null ? result : Map.of()
            );

            for (var sub : subscriptions) {
                if (sub.getIsActive() == null || !sub.getIsActive()) continue;
                try {
                    boolean delivered = webhookDispatcher.dispatch(
                            sub.getCallbackUrl(), sub.getSecret(), event);
                    log.debug("Webhook {} for job {}: delivered={}", eventType, jobId, delivered);
                } catch (Exception e) {
                    log.warn("Webhook dispatch failed for job {}: url={}", jobId, sub.getCallbackUrl(), e);
                }
            }
        } catch (Exception e) {
            log.warn("Webhook lookup failed for job {}", jobId, e);
        }
    }

    private String toJson(Object obj) {
        try { return obj != null ? mapper.writeValueAsString(obj) : null; }
        catch (Exception e) { log.error("JSON error", e); return null; }
    }
}

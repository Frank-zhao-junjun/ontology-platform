package com.ontology.platform.infrastructure.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.infrastructure.persistence.JobRecordPO;
import com.ontology.platform.infrastructure.persistence.JobRecordPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Database-backed job queue — polls {@code job_record} table for QUEUED jobs.
 * Replaces the previous no-op implementation that always returned empty.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobQueueService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int DEQUEUE_LIMIT = 1;

    private final JobRecordPOMapper jobRecordMapper;

    public String enqueue(String jobType, Map<String, Object> payload, String tenantId, String agentId) {
        var jobId = UUID.randomUUID();
        try {
            var po = JobRecordPO.builder()
                    .id(jobId)
                    .jobType(jobType)
                    .tenantId(tenantId)
                    .agentId(agentId)
                    .status("QUEUED")
                    .payload(objectMapper.writeValueAsString(payload))
                    .retryCount(0)
                    .maxRetries(3)
                    .createdAt(Instant.now())
                    .build();
            jobRecordMapper.insert(po);
            log.debug("Job enqueued: id={}, type={}", jobId, jobType);
        } catch (JsonProcessingException e) {
            throw new JobQueueException("Failed to serialize job payload", e);
        }
        return jobId.toString();
    }

    public JobMessage dequeue() {
        List<JobRecordPO> jobs = jobRecordMapper.selectByStatus("QUEUED", null, DEQUEUE_LIMIT);
        if (jobs.isEmpty()) {
            return null;
        }
        JobRecordPO po = jobs.get(0);
        return new JobMessage(
                po.getId().toString(),
                po.getJobType(),
                po.getTenantId(),
                po.getAgentId(),
                deserializePayload(po.getPayload()),
                po.getPayload()
        );
    }

    public void ack(String jobId, String raw) {
        log.debug("Job acked: {}", jobId);
        // The job status is updated to COMPLETED/FAILED in JobPoller,
        // so ack is a no-op in the DB polling model.
    }

    public void retry(String raw) {
        // Retry is handled by updating the job_record row in JobPoller.
        log.debug("Job retry requested");
    }

    public long queueSize() {
        List<JobRecordPO> queued = jobRecordMapper.selectByStatus("QUEUED", null, Integer.MAX_VALUE);
        return queued.size();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deserializePayload(String payload) {
        if (payload == null) return Map.of();
        try {
            return objectMapper.readValue(payload, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize job payload", e);
            return Map.of();
        }
    }

    public record JobMessage(String id, String jobType, String tenantId,
                              String agentId, Map<String, Object> payload, String raw) {}

    public static class JobQueueException extends RuntimeException {
        public JobQueueException(String msg, Throwable cause) { super(msg, cause); }
    }
}

package com.ontology.platform.application.service.job.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.job.JobResponse;
import com.ontology.platform.application.dto.job.SubmitJobRequest;
import com.ontology.platform.application.dto.job.SubmitJobResponse;
import com.ontology.platform.application.service.job.JobService;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.infrastructure.job.JobQueueService;
import com.ontology.platform.infrastructure.persistence.JobRecordPO;
import com.ontology.platform.infrastructure.persistence.JobRecordPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRecordPOMapper jobRecordMapper;
    private final JobQueueService jobQueueService;
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    @Transactional
    public SubmitJobResponse submitJob(SubmitJobRequest request, String tenantId, String userId) {
        var po = JobRecordPO.builder()
                .id(UUID.randomUUID())
                .jobType(request.getJobType())
                .tenantId(tenantId)
                .agentId(userId)
                .idempotencyKey(request.getIdempotencyKey())
                .status("QUEUED")
                .payload(toJson(request.getPayload()))
                .createdAt(Instant.now())
                .build();
        jobRecordMapper.insert(po);

        jobQueueService.enqueue(request.getJobType(),
                request.getPayload() != null ? request.getPayload() : Map.of(),
                tenantId, userId);

        return SubmitJobResponse.builder()
                .jobId(po.getId().toString())
                .jobType(request.getJobType())
                .status("QUEUED")
                .createdAt(po.getCreatedAt())
                .build();
    }

    @Override
    public JobResponse getJob(UUID jobId) {
        var po = jobRecordMapper.selectById(jobId);
        if (po == null) {
            throw new ResourceNotFoundException("Job", jobId.toString());
        }
        return toResponse(po);
    }

    @Override
    public List<JobResponse> listJobs(String status, String tenantId, int limit) {
        if (status == null || status.isBlank()) status = "QUEUED";
        if (limit <= 0) limit = 50;
        var list = jobRecordMapper.selectByStatus(status, tenantId, Math.min(limit, 100));
        return list.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void cancelJob(UUID jobId) {
        var po = jobRecordMapper.selectById(jobId);
        if (po == null) {
            throw new ResourceNotFoundException("Job", jobId.toString());
        }
        if ("COMPLETED".equals(po.getStatus()) || "CANCELLED".equals(po.getStatus())) {
            return;
        }
        jobRecordMapper.updateStatus(jobId, "CANCELLED", "Cancelled by user");
    }

    @SuppressWarnings("unchecked")
    private JobResponse toResponse(JobRecordPO po) {
        return JobResponse.builder()
                .jobId(po.getId())
                .jobType(po.getJobType())
                .tenantId(po.getTenantId())
                .agentId(po.getAgentId())
                .status(po.getStatus())
                .payload(fromJson(po.getPayload(), Map.class))
                .result(fromJson(po.getResult(), Map.class))
                .errorMessage(po.getErrorMessage())
                .retryCount(po.getRetryCount() != null ? po.getRetryCount() : 0)
                .maxRetries(po.getMaxRetries() != null ? po.getMaxRetries() : 3)
                .createdAt(po.getCreatedAt())
                .startedAt(po.getStartedAt())
                .completedAt(po.getCompletedAt())
                .build();
    }

    private String toJson(Object obj) {
        try { return obj != null ? mapper.writeValueAsString(obj) : null; }
        catch (Exception e) { log.error("JSON serialize failed", e); return null; }
    }

    @SuppressWarnings("unchecked")
    private <T> T fromJson(String json, Class<?> rawType) {
        if (json == null) return null;
        try { return (T) mapper.readValue(json, rawType); }
        catch (Exception e) { log.error("JSON deserialize failed", e); return null; }
    }
}

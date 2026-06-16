package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.JobRecord;
import com.ontology.platform.infrastructure.persistence.JobRecordPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JobRecordConverter {

    public JobRecord toEntity(JobRecordPO po) {
        if (po == null) return null;
        return JobRecord.builder()
                .id(po.getId())
                .jobType(po.getJobType())
                .tenantId(po.getTenantId())
                .agentId(po.getAgentId())
                .idempotencyKey(po.getIdempotencyKey())
                .status(po.getStatus())
                .payload(po.getPayload())
                .result(po.getResult())
                .errorMessage(po.getErrorMessage())
                .retryCount(po.getRetryCount())
                .maxRetries(po.getMaxRetries())
                .nextRetryAt(po.getNextRetryAt())
                .createdAt(po.getCreatedAt())
                .startedAt(po.getStartedAt())
                .completedAt(po.getCompletedAt())
                .build();
    }

    public JobRecordPO toPO(JobRecord entity) {
        if (entity == null) return null;
        return JobRecordPO.builder()
                .id(entity.getId())
                .jobType(entity.getJobType())
                .tenantId(entity.getTenantId())
                .agentId(entity.getAgentId())
                .idempotencyKey(entity.getIdempotencyKey())
                .status(entity.getStatus())
                .payload(entity.getPayload())
                .result(entity.getResult())
                .errorMessage(entity.getErrorMessage())
                .retryCount(entity.getRetryCount())
                .maxRetries(entity.getMaxRetries())
                .nextRetryAt(entity.getNextRetryAt())
                .createdAt(entity.getCreatedAt())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }

    public List<JobRecord> toEntityList(List<JobRecordPO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}

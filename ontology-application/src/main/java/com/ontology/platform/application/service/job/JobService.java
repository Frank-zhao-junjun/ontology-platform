package com.ontology.platform.application.service.job;

import com.ontology.platform.application.dto.job.JobResponse;
import com.ontology.platform.application.dto.job.SubmitJobRequest;
import com.ontology.platform.application.dto.job.SubmitJobResponse;

import java.util.List;
import java.util.UUID;

public interface JobService {

    SubmitJobResponse submitJob(SubmitJobRequest request, String tenantId, String userId);

    JobResponse getJob(UUID jobId);

    List<JobResponse> listJobs(String status, String tenantId, int limit);

    void cancelJob(UUID jobId);
}

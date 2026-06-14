package com.ontology.platform.api.controller.job;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.job.JobResponse;
import com.ontology.platform.application.dto.job.SubmitJobRequest;
import com.ontology.platform.application.dto.job.SubmitJobResponse;
import com.ontology.platform.application.service.job.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Job management API controller. Phase 2b / F01.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Async Job", description = "异步任务管理API")
public class JobController {

    private final JobService jobService;

    @PostMapping
    @Operation(summary = "提交异步任务", description = "将任务提交到队列异步执行")
    public ResponseEntity<ApiResponse<SubmitJobResponse>> submitJob(
            @Valid @RequestBody SubmitJobRequest request,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {

        log.info("REST: Submit job, type={}, tenant={}", request.getJobType(), tenantId);
        SubmitJobResponse response = jobService.submitJob(request, tenantId, userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询任务状态", description = "根据jobId获取任务详情")
    public ResponseEntity<ApiResponse<JobResponse>> getJob(
            @Parameter(description = "任务ID") @PathVariable("id") UUID id) {

        log.debug("REST: Get job, id={}", id);
        JobResponse response = jobService.getJob(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "按状态查询任务列表", description = "分页查询指定状态的任务")
    public ResponseEntity<ApiResponse<List<JobResponse>>> listJobs(
            @Parameter(description = "任务状态") @RequestParam(required = false) String status,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId,
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "50") int limit) {

        log.debug("REST: List jobs, status={}, tenant={}", status, tenantId);
        List<JobResponse> response = jobService.listJobs(status, tenantId, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "取消任务", description = "取消一个未完成的任务")
    public ResponseEntity<ApiResponse<Void>> cancelJob(
            @Parameter(description = "任务ID") @PathVariable("id") UUID id) {

        log.info("REST: Cancel job, id={}", id);
        jobService.cancelJob(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

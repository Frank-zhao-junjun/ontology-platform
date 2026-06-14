package com.ontology.platform.api.controller.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.api.config.GlobalExceptionHandler;
import com.ontology.platform.application.dto.job.JobResponse;
import com.ontology.platform.application.dto.job.SubmitJobRequest;
import com.ontology.platform.application.dto.job.SubmitJobResponse;
import com.ontology.platform.application.service.job.JobService;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link JobController}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JobController Test")
class JobControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JobService jobService;

    @InjectMocks
    private JobController jobController;

    private ObjectMapper objectMapper;

    private static final UUID TEST_JOB_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(jobController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Nested
    @DisplayName("POST /api/v1/jobs - submit job")
    class SubmitJobTests {

        @Test
        @DisplayName("should return 202 with job details")
        void submitJobSuccess() throws Exception {
            var request = SubmitJobRequest.builder()
                    .jobType("import.execute")
                    .payload(Map.of("uploadId", "upload-1"))
                    .idempotencyKey("ik-001")
                    .build();

            var response = SubmitJobResponse.builder()
                    .jobId(TEST_JOB_ID.toString())
                    .jobType("import.execute")
                    .status("QUEUED")
                    .createdAt(Instant.now())
                    .build();

            when(jobService.submitJob(any(), eq("default"), eq("user-1")))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/jobs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-Tenant-Id", "default")
                            .header("X-User-Id", "user-1")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.jobId").value(TEST_JOB_ID.toString()))
                    .andExpect(jsonPath("$.data.status").value("QUEUED"));

            verify(jobService).submitJob(any(), eq("default"), eq("user-1"));
        }

        @Test
        @DisplayName("should return 400 when jobType is blank")
        void submitJobInvalid() throws Exception {
            var request = SubmitJobRequest.builder()
                    .jobType("")
                    .build();

            mockMvc.perform(post("/api/v1/jobs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-Tenant-Id", "default")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(is(not(0))));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/jobs/{id} - get job")
    class GetJobTests {

        @Test
        @DisplayName("should return job details")
        void getJobSuccess() throws Exception {
            var response = JobResponse.builder()
                    .jobId(TEST_JOB_ID)
                    .jobType("import.execute")
                    .tenantId("default")
                    .status("QUEUED")
                    .createdAt(Instant.now())
                    .build();

            when(jobService.getJob(TEST_JOB_ID)).thenReturn(response);

            mockMvc.perform(get("/api/v1/jobs/" + TEST_JOB_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.jobId").value(TEST_JOB_ID.toString()))
                    .andExpect(jsonPath("$.data.status").value("QUEUED"));
        }

        @Test
        @DisplayName("should return 404 when job not found")
        void getJobNotFound() throws Exception {
            when(jobService.getJob(TEST_JOB_ID))
                    .thenThrow(new ResourceNotFoundException("Job", TEST_JOB_ID.toString()));

            mockMvc.perform(get("/api/v1/jobs/" + TEST_JOB_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(is(not(0))));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/jobs - list jobs")
    class ListJobsTests {

        @Test
        @DisplayName("should return job list")
        void listJobsSuccess() throws Exception {
            var job2Id = UUID.randomUUID();
            var jobs = List.of(
                    JobResponse.builder().jobId(TEST_JOB_ID).status("RUNNING").jobType("import").build(),
                    JobResponse.builder().jobId(job2Id).status("RUNNING").jobType("export").build()
            );

            when(jobService.listJobs("RUNNING", "default", 50)).thenReturn(jobs);

            mockMvc.perform(get("/api/v1/jobs")
                            .param("status", "RUNNING")
                            .header("X-Tenant-Id", "default"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }

        @Test
        @DisplayName("should return empty list")
        void listJobsEmpty() throws Exception {
            when(jobService.listJobs(any(), anyString(), anyInt())).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/jobs")
                            .header("X-Tenant-Id", "default"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/jobs/{id} - cancel job")
    class CancelJobTests {

        @Test
        @DisplayName("should cancel job successfully")
        void cancelJobSuccess() throws Exception {
            doNothing().when(jobService).cancelJob(TEST_JOB_ID);

            mockMvc.perform(delete("/api/v1/jobs/" + TEST_JOB_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            verify(jobService).cancelJob(TEST_JOB_ID);
        }

        @Test
        @DisplayName("should return 404 when job not found")
        void cancelJobNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Job", TEST_JOB_ID.toString()))
                    .when(jobService).cancelJob(TEST_JOB_ID);

            mockMvc.perform(delete("/api/v1/jobs/" + TEST_JOB_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(is(not(0))));
        }
    }
}
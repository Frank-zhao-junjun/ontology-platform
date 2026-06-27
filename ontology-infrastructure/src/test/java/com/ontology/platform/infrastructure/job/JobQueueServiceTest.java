package com.ontology.platform.infrastructure.job;

import com.ontology.platform.infrastructure.persistence.JobRecordPOMapper;
import com.ontology.platform.infrastructure.persistence.JobRecordPO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobQueueServiceTest {

    @Mock
    private JobRecordPOMapper jobRecordMapper;

    private JobQueueService service;

    @BeforeEach
    void setUp() {
        service = new JobQueueService(jobRecordMapper);
    }

    @Test
    void enqueue_shouldPersistToDbAndReturnUuid() {
        when(jobRecordMapper.insert(any(JobRecordPO.class))).thenReturn(1);
        var payload = Map.<String, Object>of("key", "value");
        var jobId = service.enqueue("TEST_TYPE", payload, "tenant-1", "agent-1");

        assertNotNull(jobId);
        assertDoesNotThrow(() -> UUID.fromString(jobId));

        // Verify that the record was inserted with correct status
        ArgumentCaptor<JobRecordPO> captor = ArgumentCaptor.forClass(JobRecordPO.class);
        verify(jobRecordMapper).insert(captor.capture());
        assertEquals("QUEUED", captor.getValue().getStatus());
        assertEquals("TEST_TYPE", captor.getValue().getJobType());
        assertEquals("tenant-1", captor.getValue().getTenantId());
    }

    @Test
    void dequeue_shouldReturnNullWhenDbEmpty() {
        when(jobRecordMapper.selectByStatus(eq("QUEUED"), isNull(), anyInt()))
                .thenReturn(Collections.emptyList());
        var msg = service.dequeue();
        assertNull(msg);
    }

    @Test
    void dequeue_shouldReturnJobWhenDbHasQueued() {
        var po = JobRecordPO.builder()
                .id(UUID.randomUUID())
                .jobType("TEST_TYPE")
                .tenantId("tenant-1")
                .agentId("agent-1")
                .status("QUEUED")
                .payload("{\"key\":\"value\"}")
                .build();
        when(jobRecordMapper.selectByStatus(eq("QUEUED"), isNull(), anyInt()))
                .thenReturn(List.of(po));

        var msg = service.dequeue();
        assertNotNull(msg);
        assertEquals(po.getId().toString(), msg.id());
        assertEquals("TEST_TYPE", msg.jobType());
        assertEquals("value", msg.payload().get("key"));
    }

    @Test
    void ack_shouldNotThrow() {
        assertDoesNotThrow(() -> service.ack("job-1", "raw-json"));
    }

    @Test
    void retry_shouldNotThrow() {
        assertDoesNotThrow(() -> service.retry("raw-json"));
    }

    @Test
    void queueSize_shouldReturnCountFromDb() {
        when(jobRecordMapper.selectByStatus(eq("QUEUED"), isNull(), eq(Integer.MAX_VALUE)))
                .thenReturn(Collections.emptyList());
        assertEquals(0L, service.queueSize());

        var po = JobRecordPO.builder()
                .id(UUID.randomUUID()).jobType("T").status("QUEUED").build();
        when(jobRecordMapper.selectByStatus(eq("QUEUED"), isNull(), eq(Integer.MAX_VALUE)))
                .thenReturn(List.of(po, po, po));
        assertEquals(3L, service.queueSize());
    }
}

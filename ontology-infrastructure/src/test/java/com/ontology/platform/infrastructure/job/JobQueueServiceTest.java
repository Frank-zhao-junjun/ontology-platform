package com.ontology.platform.infrastructure.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JobQueueServiceTest {

    private JobQueueService service;

    @BeforeEach
    void setUp() {
        service = new JobQueueService();
    }

    @Test
    void enqueue_shouldReturnUuid() {
        var payload = Map.<String, Object>of("key", "value");
        var jobId = service.enqueue("TEST_TYPE", payload, "tenant-1", "agent-1");

        assertNotNull(jobId);
        assertDoesNotThrow(() -> UUID.fromString(jobId));
    }

    @Test
    void dequeue_shouldReturnNullWhenEmpty() {
        var msg = service.dequeue();
        assertNull(msg);
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
    void queueSize_shouldReturnZero() {
        assertEquals(0L, service.queueSize());
    }
}

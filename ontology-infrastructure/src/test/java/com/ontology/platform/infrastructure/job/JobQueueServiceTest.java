package com.ontology.platform.infrastructure.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class JobQueueServiceTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String QUEUE_KEY = "job:queue";
    private static final String PENDING_KEY = "job:pending";
    private static final Duration POP_TIMEOUT = Duration.ofSeconds(5);

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ListOperations<String, String> listOps;

    private JobQueueService service;

    @BeforeEach
    void setUp() {
        when(redis.opsForList()).thenReturn(listOps);
        service = new JobQueueService(redis);
    }

    @Test
    void enqueue_shouldLeftPushAndReturnUuid() {
        var payload = Map.<String, Object>of("key", "value");
        var jobId = service.enqueue("TEST_TYPE", payload, "tenant-1", "agent-1");

        assertNotNull(jobId);
        assertDoesNotThrow(() -> UUID.fromString(jobId));

        verify(listOps).leftPush(eq(QUEUE_KEY), anyString());
    }

    @Test
    void enqueue_shouldContainAllFieldsInJson() throws Exception {
        var payload = Map.<String, Object>of("query", "select 1");
        var jobId = service.enqueue("QUERY", payload, "tenant-99", null);

        assertNotNull(jobId);

        var captor = ArgumentCaptor.forClass(String.class);
        verify(listOps).leftPush(eq(QUEUE_KEY), captor.capture());

        var json = captor.getValue();
        @SuppressWarnings("unchecked")
        var map = mapper.readValue(json, Map.class);

        assertEquals(jobId, map.get("id"));
        assertEquals("QUERY", map.get("jobType"));
        assertEquals("tenant-99", map.get("tenantId"));
        assertEquals("", map.get("agentId"));
        assertNotNull(map.get("payload"));
    }

    @Test
    void dequeue_shouldReturnJobMessage() throws Exception {
        var raw = mapper.writeValueAsString(Map.of(
                "id", "job-1", "jobType", "TEST",
                "tenantId", "t1", "agentId", "a1",
                "payload", Map.of("x", 1)
        ));

        when(listOps.rightPopAndLeftPush(QUEUE_KEY, PENDING_KEY, POP_TIMEOUT))
                .thenReturn(raw);

        var msg = service.dequeue();

        assertNotNull(msg);
        assertEquals("job-1", msg.id());
        assertEquals("TEST", msg.jobType());
        assertEquals("t1", msg.tenantId());
        assertEquals("a1", msg.agentId());
        assertEquals(raw, msg.raw());
        assertNotNull(msg.payload());
        assertEquals(1, msg.payload().get("x"));
    }

    @Test
    void dequeue_shouldReturnNullWhenEmpty() {
        when(listOps.rightPopAndLeftPush(QUEUE_KEY, PENDING_KEY, POP_TIMEOUT))
                .thenReturn(null);

        var msg = service.dequeue();
        assertNull(msg);
    }

    @Test
    void ack_shouldRemoveFromPending() {
        service.ack("job-1", "raw-json");

        verify(listOps).remove(PENDING_KEY, 1, "raw-json");
    }

    @Test
    void retry_shouldRemoveAndRequeue() {
        service.retry("raw-json");

        verify(listOps).remove(PENDING_KEY, 1, "raw-json");
        verify(listOps).leftPush(QUEUE_KEY, "raw-json");
    }

    @Test
    void queueSize_shouldReturnValue() {
        when(listOps.size(QUEUE_KEY)).thenReturn(42L);

        var size = service.queueSize();

        assertEquals(42L, size);
    }

    @Test
    void queueSize_shouldReturnZeroWhenNull() {
        when(listOps.size(QUEUE_KEY)).thenReturn(null);

        var size = service.queueSize();

        assertEquals(0L, size);
    }
}

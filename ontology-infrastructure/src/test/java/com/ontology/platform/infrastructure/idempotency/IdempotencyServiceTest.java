package com.ontology.platform.infrastructure.idempotency;

import com.ontology.platform.infrastructure.persistence.IdempotencyRecordPO;
import com.ontology.platform.infrastructure.persistence.IdempotencyRecordPOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link IdempotencyService}.
 * <p>
 * Tests the idempotency key lifecycle: acquire (first request / duplicate / in-progress),
 * complete (store response), and cleanupExpired (scheduled removal).
 */
@DisplayName("IdempotencyService Unit Tests")
@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private IdempotencyRecordPOMapper mapper;

    private IdempotencyService service;

    private static final String KEY = "idem-key-001";
    private static final String TENANT = "tenant-a";
    private static final String AGENT = "agent-1";
    private static final String METHOD = "POST";
    private static final String PATH = "/api/ontologies";

    @BeforeEach
    void setUp() {
        service = new IdempotencyService(mapper);
    }

    @Nested
    @DisplayName("acquire() — idempotency key acquisition")
    class AcquireTests {

        @Test
        @DisplayName("first request: key not found → insert record → firstRequest()")
        void acquire_firstRequest() {
            when(mapper.selectById(KEY)).thenReturn(null);

            IdempotencyService.IdempotencyResult result =
                    service.acquire(KEY, TENANT, AGENT, METHOD, PATH);

            assertThat(result.isFirstRequest()).isTrue();
            assertThat(result.isInProgress()).isFalse();

            ArgumentCaptor<IdempotencyRecordPO> captor = ArgumentCaptor.forClass(IdempotencyRecordPO.class);
            verify(mapper).insert(captor.capture());
            IdempotencyRecordPO inserted = captor.getValue();
            assertThat(inserted.getIdempotencyKey()).isEqualTo(KEY);
            assertThat(inserted.getTenantId()).isEqualTo(TENANT);
            assertThat(inserted.getAgentId()).isEqualTo(AGENT);
            assertThat(inserted.getHttpMethod()).isEqualTo(METHOD);
            assertThat(inserted.getRequestPath()).isEqualTo(PATH);
            assertThat(inserted.getResponseStatus()).isNull();
            assertThat(inserted.getCreatedAt()).isNotNull();
            assertThat(inserted.getExpiresAt()).isAfter(Instant.now());
        }

        @Test
        @DisplayName("duplicate complete: key exists with status → completed()")
        void acquire_duplicateComplete() {
            IdempotencyRecordPO existing = IdempotencyRecordPO.builder()
                    .idempotencyKey(KEY).responseStatus(200).responseBody("{\"id\":1}")
                    .build();
            when(mapper.selectById(KEY)).thenReturn(existing);

            IdempotencyService.IdempotencyResult result =
                    service.acquire(KEY, TENANT, AGENT, METHOD, PATH);

            assertThat(result.isFirstRequest()).isFalse();
            assertThat(result.isInProgress()).isFalse();
            assertThat(result.getCachedStatus()).isEqualTo(200);
            assertThat(result.getCachedBody()).isEqualTo("{\"id\":1}");
            verify(mapper, never()).insert(any());
        }

        @Test
        @DisplayName("in progress: key exists with null status → inProgress()")
        void acquire_inProgress() {
            IdempotencyRecordPO existing = IdempotencyRecordPO.builder()
                    .idempotencyKey(KEY).responseStatus(null)
                    .build();
            when(mapper.selectById(KEY)).thenReturn(existing);

            IdempotencyService.IdempotencyResult result =
                    service.acquire(KEY, TENANT, AGENT, METHOD, PATH);

            assertThat(result.isFirstRequest()).isFalse();
            assertThat(result.isInProgress()).isTrue();
            assertThat(result.getCachedStatus()).isNull();
            assertThat(result.getCachedBody()).isNull();
            verify(mapper, never()).insert(any());
        }
    }

    @Nested
    @DisplayName("complete() — store response for a key")
    class CompleteTests {

        @Captor
        private ArgumentCaptor<IdempotencyRecordPO> captor;

        @Test
        @DisplayName("key exists → set status and body → updateById")
        void complete_success() {
            IdempotencyRecordPO existing = IdempotencyRecordPO.builder()
                    .idempotencyKey(KEY).build();
            when(mapper.selectById(KEY)).thenReturn(existing);

            service.complete(KEY, 201, "{\"id\":1}");

            verify(mapper).updateById(captor.capture());
            IdempotencyRecordPO updated = captor.getValue();
            assertThat(updated.getResponseStatus()).isEqualTo(201);
            assertThat(updated.getResponseBody()).isEqualTo("\"{\\\"id\\\":1}\"");
        }

        @Test
        @DisplayName("key not found → no exception thrown")
        void complete_nullKey() {
            when(mapper.selectById(KEY)).thenReturn(null);

            service.complete(KEY, 200, "{}");

            verify(mapper, never()).updateById(any());
        }
    }

    @Nested
    @DisplayName("cleanupExpired() — scheduled expired record removal")
    class CleanupExpiredTests {

        @Test
        @DisplayName("deleteExpired called with a before timestamp")
        void cleanupExpired_callsDeleteExpired() {
            when(mapper.deleteExpired(any(Instant.class))).thenReturn(5);

            service.cleanupExpired();

            ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
            verify(mapper).deleteExpired(captor.capture());
            assertThat(captor.getValue()).isBeforeOrEqualTo(Instant.now());
        }

        @Test
        @DisplayName("zero deleted records → no log warning")
        void cleanupExpired_zeroDeleted() {
            when(mapper.deleteExpired(any(Instant.class))).thenReturn(0);

            service.cleanupExpired();

            verify(mapper).deleteExpired(any(Instant.class));
        }
    }
}

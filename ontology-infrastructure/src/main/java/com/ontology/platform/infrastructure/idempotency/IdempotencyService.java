package com.ontology.platform.infrastructure.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.infrastructure.metrics.PlatformMetrics;
import com.ontology.platform.infrastructure.persistence.IdempotencyRecordPO;
import com.ontology.platform.infrastructure.persistence.IdempotencyRecordPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRecordPOMapper mapper;
    private final PlatformMetrics metrics;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int TTL_HOURS = 24;

    @Transactional
    public IdempotencyResult acquire(String key, String tenantId, String agentId,
                                      String httpMethod, String requestPath) {
        var existing = mapper.selectById(key);
        if (existing != null) {
            if (existing.getResponseStatus() == null) {
                return IdempotencyResult.inProgress();
            }
            metrics.recordIdempotencyHit();
            return IdempotencyResult.completed(existing.getResponseStatus(), existing.getResponseBody());
        }
        var po = IdempotencyRecordPO.builder()
                .idempotencyKey(key).tenantId(tenantId).agentId(agentId)
                .httpMethod(httpMethod).requestPath(requestPath)
                .responseStatus(null).createdAt(Instant.now())
                .expiresAt(Instant.now().plus(TTL_HOURS, ChronoUnit.HOURS))
                .build();
        mapper.insert(po);
        return IdempotencyResult.firstRequest();
    }

    @Transactional
    public void complete(String key, int statusCode, Object responseBody) {
        var po = mapper.selectById(key);
        if (po == null) return;
        try {
            po.setResponseStatus(statusCode);
            po.setResponseBody(objectMapper.writeValueAsString(responseBody));
        } catch (Exception e) {
            po.setResponseStatus(statusCode);
            po.setResponseBody("{}");
        }
        mapper.updateById(po);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpired() {
        var before = Instant.now().minus(TTL_HOURS, ChronoUnit.HOURS);
        int deleted = mapper.deleteExpired(before);
        if (deleted > 0) log.info("Cleaned {} expired idempotency records", deleted);
    }

    public static class IdempotencyResult {
        private final boolean firstRequest;
        private final boolean inProgress;
        private final Integer cachedStatus;
        private final String cachedBody;

        public IdempotencyResult(boolean firstRequest, boolean inProgress,
                                  Integer cachedStatus, String cachedBody) {
            this.firstRequest = firstRequest;
            this.inProgress = inProgress;
            this.cachedStatus = cachedStatus;
            this.cachedBody = cachedBody;
        }

        public boolean isFirstRequest() { return firstRequest; }
        public boolean isInProgress() { return inProgress; }
        public Integer getCachedStatus() { return cachedStatus; }
        public String getCachedBody() { return cachedBody; }

        public static IdempotencyResult firstRequest() {
            return new IdempotencyResult(true, false, null, null);
        }
        public static IdempotencyResult inProgress() {
            return new IdempotencyResult(false, true, null, null);
        }
        public static IdempotencyResult completed(int status, String body) {
            return new IdempotencyResult(false, false, status, body);
        }
    }
}

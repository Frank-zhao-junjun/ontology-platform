package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.AuditLog;
import java.util.List;

public interface AuditLogRepository {
    void save(AuditLog log);
    List<AuditLog> findByApiKeyName(String apiKeyName, int limit);
    List<AuditLog> findBySandboxId(String sandboxId, int limit);
    List<AuditLog> findByAction(String action, int limit);
}

package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, String> {
    List<AuditLogEntity> findTopByApiKeyNameOrderByTimestampDesc(String apiKeyName, org.springframework.data.domain.Pageable pageable);
    List<AuditLogEntity> findTopBySandboxIdOrderByTimestampDesc(String sandboxId, org.springframework.data.domain.Pageable pageable);
    List<AuditLogEntity> findTopByActionOrderByTimestampDesc(String action, org.springframework.data.domain.Pageable pageable);
}

package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.entity.AuditLog;
import com.ontology.platform.domain.repository.AuditLogRepository;
import com.ontology.platform.infrastructure.repository.AuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaAuditLogRepository implements AuditLogRepository {
    private final AuditLogJpaRepository jpa;

    @Override
    public void save(AuditLog log) {
        jpa.save(PersistenceMapper.toEntity(log));
    }

    @Override
    public List<AuditLog> findByApiKeyName(String apiKeyName, int limit) {
        return jpa.findTopByApiKeyNameOrderByTimestampDesc(apiKeyName, PageRequest.of(0, limit))
                .stream().map(PersistenceMapper::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<AuditLog> findBySandboxId(String sandboxId, int limit) {
        return jpa.findTopBySandboxIdOrderByTimestampDesc(sandboxId, PageRequest.of(0, limit))
                .stream().map(PersistenceMapper::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<AuditLog> findByAction(String action, int limit) {
        return jpa.findTopByActionOrderByTimestampDesc(action, PageRequest.of(0, limit))
                .stream().map(PersistenceMapper::fromEntity).collect(Collectors.toList());
    }
}

package com.ontology.platform.application.service.lifecycle;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.dto.lifecycle.EntityLifecycleResponse;
import com.ontology.platform.infrastructure.persistence.EntityLifecycleSnapshotPO;
import com.ontology.platform.infrastructure.persistence.EntityLifecycleSnapshotPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LifecycleService {

    private final EntityLifecycleSnapshotPOMapper lifecycleMapper;
    private final ObjectMapper objectMapper;

    public EntityLifecycleResponse getEntityLifecycle(String ontologyId, String entityId) {
        EntityLifecycleSnapshotPO po = lifecycleMapper.selectOne(
                new QueryWrapper<EntityLifecycleSnapshotPO>()
                        .eq("ontology_id", ontologyId)
                        .eq("entity_id", entityId)
                        .last("LIMIT 1"));
        if (po == null) {
            po = lifecycleMapper.selectOne(
                    new QueryWrapper<EntityLifecycleSnapshotPO>()
                            .eq("entity_id", entityId)
                            .last("LIMIT 1"));
        }
        if (po == null) {
            throw new ResourceNotFoundException("EntityLifecycle", entityId);
        }

        return EntityLifecycleResponse.builder()
                .ontologyId(po.getOntologyId())
                .entityId(po.getEntityId())
                .snapshotVersion(po.getSnapshotVersion())
                .lifecycleData(parseLifecycleData(po.getLifecycleData()))
                .build();
    }

    private Map<String, Object> parseLifecycleData(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse lifecycle data: {}", e.getMessage());
            return Map.of();
        }
    }
}

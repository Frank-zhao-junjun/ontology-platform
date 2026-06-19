package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateErrorRecoveryRequest;
import com.ontology.platform.application.dto.domain.ErrorRecoveryResponse;
import com.ontology.platform.domain.entity.ErrorRecovery;
import com.ontology.platform.infrastructure.persistence.ErrorRecoveryPO;
import com.ontology.platform.infrastructure.persistence.ErrorRecoveryPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class ErrorRecoveryService {
    private final ErrorRecoveryPOMapper mapper;

    @Transactional
    public ErrorRecoveryResponse create(String ontologyId, CreateErrorRecoveryRequest request, String userId) {
        log.info("Creating ErrorRecovery");
        ErrorRecovery entity = ErrorRecovery.create();
        mapRequest(request, entity);
        ErrorRecoveryPO po = toPO(entity);
        mapper.insert(po);
        return toResponse(entity);
    }

    public ErrorRecoveryResponse getById(String id) {
        ErrorRecoveryPO po = mapper.selectById(id);
        return po == null ? null : toResponse(fromPO(po));
    }

    public List<ErrorRecoveryResponse> list() {
        return mapper.selectList(null).stream()
                .map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) { mapper.deleteById(id); }

    private void mapRequest(CreateErrorRecoveryRequest req, ErrorRecovery entity) {
        if (req.getActionId() != null) entity.setActionId(req.getActionId());
        if (req.getErrorPattern() != null) entity.setErrorPattern(req.getErrorPattern());
        if (req.getRecoveryStrategy() != null) entity.setRecoveryStrategy(req.getRecoveryStrategy());
        if (req.getMaxRetries() != null) entity.setMaxRetries(req.getMaxRetries());
        if (req.getFallbackActionId() != null) entity.setFallbackActionId(req.getFallbackActionId());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
    }

        private ErrorRecoveryPO toPO(ErrorRecovery entity) {
        return ErrorRecoveryPO.builder()
                .id(entity.getId())
                .actionId(entity.getActionId())
                .errorPattern(entity.getErrorPattern())
                .recoveryStrategy(entity.getRecoveryStrategy())
                .maxRetries(entity.getMaxRetries())
                .fallbackActionId(entity.getFallbackActionId())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                
                .build();
    }

        private ErrorRecovery fromPO(ErrorRecoveryPO po) {
        return ErrorRecovery.builder()
                .id(po.getId())
                .actionId(po.getActionId())
                .errorPattern(po.getErrorPattern())
                .recoveryStrategy(po.getRecoveryStrategy())
                .maxRetries(po.getMaxRetries())
                .fallbackActionId(po.getFallbackActionId())
                .description(po.getDescription())
                .createdAt(po.getCreatedAt())
                
                .build();
    }

        private ErrorRecoveryResponse toResponse(ErrorRecovery entity) {
        return ErrorRecoveryResponse.builder()
                .id(entity.getId())
                .actionId(entity.getActionId())
                .errorPattern(entity.getErrorPattern())
                .recoveryStrategy(entity.getRecoveryStrategy())
                .maxRetries(entity.getMaxRetries())
                .fallbackActionId(entity.getFallbackActionId())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                
                .build();
    }
}

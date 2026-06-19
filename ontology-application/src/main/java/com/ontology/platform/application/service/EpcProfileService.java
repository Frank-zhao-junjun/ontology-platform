package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateEpcProfileRequest;
import com.ontology.platform.application.dto.domain.EpcProfileResponse;
import com.ontology.platform.domain.entity.EpcProfile;
import com.ontology.platform.infrastructure.persistence.EpcProfilePO;
import com.ontology.platform.infrastructure.persistence.EpcProfilePOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class EpcProfileService {
    private final EpcProfilePOMapper mapper;

    @Transactional
    public EpcProfileResponse create(String ontologyId, CreateEpcProfileRequest request, String userId) {
        log.info("Creating EpcProfile");
        EpcProfile entity = EpcProfile.create();
        EpcProfilePO po = toPO(entity);
        mapper.insert(po);
        return toResponse(entity);
    }

    public EpcProfileResponse getById(String id) {
        EpcProfilePO po = mapper.selectById(id);
        return po == null ? null : toResponse(fromPO(po));
    }

    public List<EpcProfileResponse> list() {
        return mapper.selectList(null).stream()
                .map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) { mapper.deleteById(id); }

    private EpcProfilePO toPO(EpcProfile entity) {
        return EpcProfilePO.builder()
                .id(entity.getId())        .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private EpcProfile fromPO(EpcProfilePO po) {
        return EpcProfile.builder()
                .id(po.getId())        .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    private EpcProfileResponse toResponse(EpcProfile entity) {
        return EpcProfileResponse.builder()
                .id(entity.getId())        .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

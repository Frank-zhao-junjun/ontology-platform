package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateEntityLifecycleSnapshotRequest;
import com.ontology.platform.application.dto.domain.EntityLifecycleSnapshotResponse;
import com.ontology.platform.domain.entity.EntityLifecycleSnapshot;
import com.ontology.platform.infrastructure.persistence.EntityLifecycleSnapshotPO;
import com.ontology.platform.infrastructure.persistence.EntityLifecycleSnapshotPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class EntityLifecycleSnapshotService {
    private final EntityLifecycleSnapshotPOMapper mapper;

    @Transactional
    public EntityLifecycleSnapshotResponse create(String ontologyId, CreateEntityLifecycleSnapshotRequest request, String userId) {
        log.info("Creating EntityLifecycleSnapshot");
        EntityLifecycleSnapshot entity = EntityLifecycleSnapshot.create();
        EntityLifecycleSnapshotPO po = toPO(entity);
        mapper.insert(po);
        return toResponse(entity);
    }

    public EntityLifecycleSnapshotResponse getById(String id) {
        EntityLifecycleSnapshotPO po = mapper.selectById(id);
        return po == null ? null : toResponse(fromPO(po));
    }

    public List<EntityLifecycleSnapshotResponse> list() {
        return mapper.selectList(null).stream()
                .map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) { mapper.deleteById(id); }

    private EntityLifecycleSnapshotPO toPO(EntityLifecycleSnapshot entity) {
        return EntityLifecycleSnapshotPO.builder()
                .id(entity.getId())        .createdAt(entity.getCreatedAt())
                .build();
    }

    private EntityLifecycleSnapshot fromPO(EntityLifecycleSnapshotPO po) {
        return EntityLifecycleSnapshot.builder()
                .id(po.getId())        .createdAt(po.getCreatedAt())
                .build();
    }

    private EntityLifecycleSnapshotResponse toResponse(EntityLifecycleSnapshot entity) {
        return EntityLifecycleSnapshotResponse.builder()
                .id(entity.getId())        .createdAt(entity.getCreatedAt())
                .build();
    }
}

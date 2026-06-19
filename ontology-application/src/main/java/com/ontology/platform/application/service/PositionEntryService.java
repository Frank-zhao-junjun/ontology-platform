package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreatePositionEntryRequest;
import com.ontology.platform.application.dto.domain.PositionEntryResponse;
import com.ontology.platform.domain.entity.PositionEntry;
import com.ontology.platform.infrastructure.persistence.PositionEntryPO;
import com.ontology.platform.infrastructure.persistence.PositionEntryPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PositionEntryService {

    private final PositionEntryPOMapper mapper;

    @Transactional
    public PositionEntryResponse create(String ontologyId, CreatePositionEntryRequest request, String userId) {
        log.info("Creating PositionEntry: ontologyId={}", ontologyId);
        PositionEntry entity = PositionEntry.create();
        return toResponse(entity);
    }

    public PositionEntryResponse getById(String id) {
        PositionEntryPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<PositionEntryResponse> listByOntologyId(String ontologyId) {
        List<PositionEntryPO> pos = mapper.selectByOntologyId(ontologyId);
        return pos.stream().map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) {
        PositionEntryPO po = mapper.selectById(id);
        if (po != null) mapper.deleteById(id);
    }

    private PositionEntryPO toPO(PositionEntry entity) {
        return PositionEntryPO.builder().id(entity.getId())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }

    private PositionEntry fromPO(PositionEntryPO po) {
        return PositionEntry.builder().id(po.getId())
                .createdAt(po.getCreatedAt()).updatedAt(po.getUpdatedAt()).build();
    }

    private PositionEntryResponse toResponse(PositionEntry entity) {
        return PositionEntryResponse.builder().id(entity.getId())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }
}

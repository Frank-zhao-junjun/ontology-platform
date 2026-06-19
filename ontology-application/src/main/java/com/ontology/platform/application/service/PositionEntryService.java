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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PositionEntryService {

    private final PositionEntryPOMapper mapper;

    @Transactional
    public PositionEntryResponse create(String ontologyId, CreatePositionEntryRequest request, String userId) {
        log.info("Creating PositionEntry: ontologyId={}, name={}", ontologyId, request.getName());
        PositionEntry entity = PositionEntry.create(ontologyId);
        mapRequest(request, entity);
        mapper.insert(toPO(entity));
        return toResponse(entity);
    }

    public PositionEntryResponse getById(String id) {
        PositionEntryPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<PositionEntryResponse> listByOntologyId(String ontologyId) {
        return mapper.selectByOntologyId(ontologyId).stream()
                .map(po -> toResponse(fromPO(po)))
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) {
        if (mapper.selectById(id) != null) mapper.deleteById(id);
    }

    private void mapRequest(CreatePositionEntryRequest req, PositionEntry entity) {
        if (req.getName() != null) entity.setName(req.getName());
        if (req.getNameEn() != null) entity.setNameEn(req.getNameEn());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getDepartmentId() != null) entity.setDepartmentId(req.getDepartmentId());
        if (req.getResponsibilities() != null) entity.setResponsibilities(req.getResponsibilities());
    }

    private PositionEntryPO toPO(PositionEntry entity) {
        return PositionEntryPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .name(entity.getName())
                .nameEn(entity.getNameEn())
                .description(entity.getDescription())
                .departmentId(entity.getDepartmentId())
                .responsibilities(entity.getResponsibilities())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private PositionEntry fromPO(PositionEntryPO po) {
        return PositionEntry.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .name(po.getName())
                .nameEn(po.getNameEn())
                .description(po.getDescription())
                .departmentId(po.getDepartmentId())
                .responsibilities(po.getResponsibilities())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    private PositionEntryResponse toResponse(PositionEntry entity) {
        return PositionEntryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .nameEn(entity.getNameEn())
                .description(entity.getDescription())
                .departmentId(entity.getDepartmentId())
                .responsibilities(entity.getResponsibilities())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

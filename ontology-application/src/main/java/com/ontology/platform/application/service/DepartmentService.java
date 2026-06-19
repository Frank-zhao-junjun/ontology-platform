package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateDepartmentRequest;
import com.ontology.platform.application.dto.domain.DepartmentResponse;
import com.ontology.platform.domain.entity.Department;
import com.ontology.platform.infrastructure.persistence.DepartmentPO;
import com.ontology.platform.infrastructure.persistence.DepartmentPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentPOMapper mapper;

    @Transactional
    public DepartmentResponse create(String ontologyId, CreateDepartmentRequest request, String userId) {
        log.info("Creating Department: ontologyId={}, name={}", ontologyId, request.getName());
        Department entity = Department.create(ontologyId);
        mapRequest(request, entity);
        mapper.insert(toPO(entity));
        return toResponse(entity);
    }

    public DepartmentResponse getById(String id) {
        DepartmentPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<DepartmentResponse> listByOntologyId(String ontologyId) {
        return mapper.selectByOntologyId(ontologyId).stream()
                .map(po -> toResponse(fromPO(po)))
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) {
        if (mapper.selectById(id) != null) mapper.deleteById(id);
    }

    private void mapRequest(CreateDepartmentRequest req, Department entity) {
        if (req.getName() != null) entity.setName(req.getName());
        if (req.getNameEn() != null) entity.setNameEn(req.getNameEn());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getParentDepartmentId() != null) entity.setParentDepartmentId(req.getParentDepartmentId());
    }

    private DepartmentPO toPO(Department entity) {
        return DepartmentPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .name(entity.getName())
                .nameEn(entity.getNameEn())
                .description(entity.getDescription())
                .parentDepartmentId(entity.getParentDepartmentId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Department fromPO(DepartmentPO po) {
        return Department.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .name(po.getName())
                .nameEn(po.getNameEn())
                .description(po.getDescription())
                .parentDepartmentId(po.getParentDepartmentId())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    private DepartmentResponse toResponse(Department entity) {
        return DepartmentResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .nameEn(entity.getNameEn())
                .description(entity.getDescription())
                .parentDepartmentId(entity.getParentDepartmentId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

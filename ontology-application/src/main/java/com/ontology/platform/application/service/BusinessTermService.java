package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateBusinessTermRequest;
import com.ontology.platform.application.dto.domain.BusinessTermResponse;
import com.ontology.platform.domain.entity.BusinessTerm;
import com.ontology.platform.infrastructure.persistence.BusinessTermPO;
import com.ontology.platform.infrastructure.persistence.BusinessTermPOMapper;
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
public class BusinessTermService {

    private final BusinessTermPOMapper mapper;

    @Transactional
    public BusinessTermResponse create(String ontologyId, CreateBusinessTermRequest request, String userId) {
        log.info("Creating BusinessTerm: ontologyId={}", ontologyId);
        BusinessTerm entity = BusinessTerm.create();
        return toResponse(entity);
    }

    public BusinessTermResponse getById(String id) {
        BusinessTermPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<BusinessTermResponse> listByOntologyId(String ontologyId) {
        List<BusinessTermPO> pos = mapper.selectByOntologyId(ontologyId);
        return pos.stream().map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) {
        BusinessTermPO po = mapper.selectById(id);
        if (po != null) mapper.deleteById(id);
    }

    private BusinessTermPO toPO(BusinessTerm entity) {
        return BusinessTermPO.builder().id(entity.getId())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }

    private BusinessTerm fromPO(BusinessTermPO po) {
        return BusinessTerm.builder().id(po.getId())
                .createdAt(po.getCreatedAt()).updatedAt(po.getUpdatedAt()).build();
    }

    private BusinessTermResponse toResponse(BusinessTerm entity) {
        return BusinessTermResponse.builder().id(entity.getId())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }
}

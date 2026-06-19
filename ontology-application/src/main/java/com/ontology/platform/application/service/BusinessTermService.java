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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessTermService {

    private final BusinessTermPOMapper mapper;

    @Transactional
    public BusinessTermResponse create(String ontologyId, CreateBusinessTermRequest request, String userId) {
        log.info("Creating BusinessTerm: ontologyId={}, name={}", ontologyId, request.getName());
        BusinessTerm entity = BusinessTerm.create(ontologyId);
        mapRequest(request, entity);
        mapper.insert(toPO(entity));
        return toResponse(entity);
    }

    public BusinessTermResponse getById(String id) {
        BusinessTermPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<BusinessTermResponse> listByOntologyId(String ontologyId) {
        return mapper.selectByOntologyId(ontologyId).stream()
                .map(po -> toResponse(fromPO(po)))
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) {
        if (mapper.selectById(id) != null) mapper.deleteById(id);
    }

    private void mapRequest(CreateBusinessTermRequest req, BusinessTerm entity) {
        if (req.getName() != null) entity.setName(req.getName());
        if (req.getNameEn() != null) entity.setNameEn(req.getNameEn());
        if (req.getDefinition() != null) entity.setDefinition(req.getDefinition());
        if (req.getSynonyms() != null) entity.setSynonyms(req.getSynonyms());
    }

        private BusinessTermPO toPO(BusinessTerm entity) {
        return BusinessTermPO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .nameEn(entity.getNameEn())
                .definition(entity.getDefinition())
                .synonyms(entity.getSynonyms())
                .ontologyId(entity.getOntologyId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

        private BusinessTerm fromPO(BusinessTermPO po) {
        return BusinessTerm.builder()
                .id(po.getId())
                .name(po.getName())
                .nameEn(po.getNameEn())
                .definition(po.getDefinition())
                .synonyms(po.getSynonyms())
                .ontologyId(po.getOntologyId())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

        private BusinessTermResponse toResponse(BusinessTerm entity) {
        return BusinessTermResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .nameEn(entity.getNameEn())
                .definition(entity.getDefinition())
                .synonyms(entity.getSynonyms())
                .ontologyId(entity.getOntologyId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

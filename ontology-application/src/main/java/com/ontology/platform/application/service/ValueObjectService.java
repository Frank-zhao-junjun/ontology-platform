package com.ontology.platform.application.service;

import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.entity.ValueObject;
import com.ontology.platform.domain.repository.ValueObjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ValueObjectService {
    private final ValueObjectRepository voRepo;

    public ValueObject create(String name, String code, String nameEn,
                              String description, String propertiesJson) {
        if (voRepo.existsByCode(code)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "值对象 code 已存在: " + code);
        }
        ValueObject vo = ValueObject.create(name, code, nameEn, description, propertiesJson);
        voRepo.save(vo);
        return vo;
    }

    public List<ValueObject> listAll() {
        return voRepo.findAll();
    }

    public ValueObject getById(String id) {
        return voRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("值对象不存在: " + id));
    }

    public ValueObject update(String id, String name, String nameEn,
                              String description, String propertiesJson) {
        ValueObject existing = getById(id);
        ValueObject updated = ValueObject.builder()
                .id(existing.getId())
                .name(name != null ? name : existing.getName())
                .code(existing.getCode())
                .nameEn(nameEn != null ? nameEn : existing.getNameEn())
                .description(description != null ? description : existing.getDescription())
                .propertiesJson(propertiesJson != null ? propertiesJson : existing.getPropertiesJson())
                .createdAt(existing.getCreatedAt())
                .updatedAt(java.time.Instant.now()).build();
        voRepo.save(updated);
        return updated;
    }
}

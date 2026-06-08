package com.ontology.platform.application.service;

import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.entity.BusinessScenario;
import com.ontology.platform.domain.repository.BusinessScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScenarioService {
    private final BusinessScenarioRepository scenarioRepo;

    public BusinessScenario createScenario(String contextId, String name, String code,
                                           String nameEn, String description) {
        if (scenarioRepo.existsByContextIdAndCode(contextId, code)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "场景 code 已存在: " + code);
        }
        BusinessScenario s = BusinessScenario.create(contextId, name, code, nameEn, description);
        scenarioRepo.save(s);
        return s;
    }

    public List<BusinessScenario> listScenarios(String contextId) {
        return scenarioRepo.findByContextId(contextId);
    }

    public BusinessScenario updateScenario(String id, String name, String nameEn, String description) {
        BusinessScenario existing = scenarioRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("场景不存在: " + id));
        BusinessScenario updated = BusinessScenario.builder()
                .id(existing.getId()).contextId(existing.getContextId())
                .name(name != null ? name : existing.getName())
                .code(existing.getCode())
                .nameEn(nameEn != null ? nameEn : existing.getNameEn())
                .description(description != null ? description : existing.getDescription())
                .applicableObjectTypeIdsJson(existing.getApplicableObjectTypeIdsJson())
                .createdAt(existing.getCreatedAt()).build();
        scenarioRepo.save(updated);
        return updated;
    }

    public BusinessScenario setApplicableObjectTypes(String id, String objectTypeIdsJson) {
        BusinessScenario existing = scenarioRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("场景不存在: " + id));
        BusinessScenario updated = BusinessScenario.builder()
                .id(existing.getId()).contextId(existing.getContextId())
                .name(existing.getName()).code(existing.getCode())
                .nameEn(existing.getNameEn()).description(existing.getDescription())
                .applicableObjectTypeIdsJson(objectTypeIdsJson).createdAt(existing.getCreatedAt()).build();
        scenarioRepo.save(updated);
        return updated;
    }
}

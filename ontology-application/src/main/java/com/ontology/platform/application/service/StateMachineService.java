package com.ontology.platform.application.service;

import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.entity.StateMachine;
import com.ontology.platform.domain.repository.StateMachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StateMachineService {
    private final StateMachineRepository smRepo;

    public StateMachine create(String contextId, String name, String nameEn,
                               String objectTypeId, String statusField,
                               String statesJson, String transitionsJson) {
        if (smRepo.existsByContextIdAndObjectTypeId(contextId, objectTypeId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "该对象类型在此上下文中已有状态机定义");
        }
        StateMachine sm = StateMachine.create(contextId, name, nameEn,
                objectTypeId, statusField, statesJson, transitionsJson);
        smRepo.save(sm);
        return sm;
    }

    public List<StateMachine> listByContext(String contextId) {
        return smRepo.findByContextId(contextId);
    }

    public List<StateMachine> listByObjectType(String objectTypeId) {
        return smRepo.findByObjectTypeId(objectTypeId);
    }

    public StateMachine getById(String id) {
        return smRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("状态机不存在: " + id));
    }

    public StateMachine update(String id, String name, String nameEn,
                               String statesJson, String transitionsJson) {
        StateMachine existing = getById(id);
        StateMachine updated = StateMachine.builder()
                .id(existing.getId()).contextId(existing.getContextId())
                .name(name != null ? name : existing.getName())
                .nameEn(nameEn != null ? nameEn : existing.getNameEn())
                .objectTypeId(existing.getObjectTypeId())
                .statusField(existing.getStatusField())
                .statesJson(statesJson != null ? statesJson : existing.getStatesJson())
                .transitionsJson(transitionsJson != null ? transitionsJson : existing.getTransitionsJson())
                .createdAt(existing.getCreatedAt())
                .updatedAt(java.time.Instant.now()).build();
        smRepo.save(updated);
        return updated;
    }
}

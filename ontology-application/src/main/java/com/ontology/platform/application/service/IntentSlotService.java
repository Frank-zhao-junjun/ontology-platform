package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateIntentSlotRequest;
import com.ontology.platform.application.dto.domain.IntentSlotResponse;
import com.ontology.platform.domain.entity.IntentSlot;
import com.ontology.platform.infrastructure.persistence.IntentSlotPO;
import com.ontology.platform.infrastructure.persistence.IntentSlotPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class IntentSlotService {
    private final IntentSlotPOMapper mapper;

    @Transactional
    public IntentSlotResponse create(String ontologyId, CreateIntentSlotRequest request, String userId) {
        log.info("Creating IntentSlot");
        IntentSlot entity = IntentSlot.create();
        mapRequest(entity, request);
        IntentSlotPO po = toPO(entity);
        mapper.insert(po);
        return toResponse(entity);
    }

    private void mapRequest(IntentSlot entity, CreateIntentSlotRequest request) {
        entity.setIntentId(request.getIntentId());
        entity.setName(request.getName());
        entity.setSlotType(request.getSlotType());
        entity.setRequired(request.getRequired());
        entity.setExamples(request.getExamples());
    }

    public IntentSlotResponse getById(String id) {
        IntentSlotPO po = mapper.selectById(id);
        return po == null ? null : toResponse(fromPO(po));
    }

    public List<IntentSlotResponse> list() {
        return mapper.selectList(null).stream()
                .map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) { mapper.deleteById(id); }

        private IntentSlotPO toPO(IntentSlot entity) {
        return IntentSlotPO.builder()
                .id(entity.getId())
                .intentId(entity.getIntentId())
                .name(entity.getName())
                .slotType(entity.getSlotType())
                .required(entity.getRequired())
                .examples(entity.getExamples())
                .createdAt(entity.getCreatedAt())
                
                .build();
    }

        private IntentSlot fromPO(IntentSlotPO po) {
        return IntentSlot.builder()
                .id(po.getId())
                .intentId(po.getIntentId())
                .name(po.getName())
                .slotType(po.getSlotType())
                .required(po.getRequired())
                .examples(po.getExamples())
                .createdAt(po.getCreatedAt())
                
                .build();
    }

        private IntentSlotResponse toResponse(IntentSlot entity) {
        return IntentSlotResponse.builder()
                .id(entity.getId())
                .intentId(entity.getIntentId())
                .name(entity.getName())
                .slotType(entity.getSlotType())
                .required(entity.getRequired())
                .examples(entity.getExamples())
                .createdAt(entity.getCreatedAt())
                
                .build();
    }
}

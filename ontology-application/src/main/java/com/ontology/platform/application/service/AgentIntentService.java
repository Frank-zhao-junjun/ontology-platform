package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateAgentIntentRequest;
import com.ontology.platform.application.dto.domain.AgentIntentResponse;
import com.ontology.platform.domain.entity.AgentIntent;
import com.ontology.platform.infrastructure.persistence.AgentIntentPO;
import com.ontology.platform.infrastructure.persistence.AgentIntentPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentIntentService {

    private final AgentIntentPOMapper mapper;

    @Transactional
    public AgentIntentResponse create(String ontologyId, CreateAgentIntentRequest request, String userId) {
        log.info("Creating AgentIntent: ontologyId={}, name={}", ontologyId, request.getName());
        AgentIntent entity = AgentIntent.create(ontologyId);
        mapRequest(request, entity);
        mapper.insert(toPO(entity));
        return toResponse(entity);
    }

    public AgentIntentResponse getById(String id) {
        AgentIntentPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<AgentIntentResponse> listByOntologyId(String ontologyId) {
        return mapper.selectByOntologyId(ontologyId).stream()
                .map(po -> toResponse(fromPO(po)))
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) {
        if (mapper.selectById(id) != null) mapper.deleteById(id);
    }

    private void mapRequest(CreateAgentIntentRequest req, AgentIntent entity) {
        if (req.getName() != null) entity.setName(req.getName());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getTriggerPhrases() != null) entity.setTriggerPhrases(req.getTriggerPhrases());
        if (req.getActionId() != null) entity.setActionId(req.getActionId());
    }

    private AgentIntentPO toPO(AgentIntent entity) {
        return AgentIntentPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .name(entity.getName())
                .description(entity.getDescription())
                .triggerPhrases(entity.getTriggerPhrases())
                .actionId(entity.getActionId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private AgentIntent fromPO(AgentIntentPO po) {
        return AgentIntent.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .name(po.getName())
                .description(po.getDescription())
                .triggerPhrases(po.getTriggerPhrases())
                .actionId(po.getActionId())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    private AgentIntentResponse toResponse(AgentIntent entity) {
        return AgentIntentResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .triggerPhrases(entity.getTriggerPhrases())
                .actionId(entity.getActionId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

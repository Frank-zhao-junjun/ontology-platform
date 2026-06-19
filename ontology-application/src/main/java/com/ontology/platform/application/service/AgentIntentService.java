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

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentIntentService {

    private final AgentIntentPOMapper mapper;

    @Transactional
    public AgentIntentResponse create(String ontologyId, CreateAgentIntentRequest request, String userId) {
        log.info("Creating AgentIntent: ontologyId={}", ontologyId);
        AgentIntent entity = AgentIntent.create();
        return toResponse(entity);
    }

    public AgentIntentResponse getById(String id) {
        AgentIntentPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<AgentIntentResponse> listByOntologyId(String ontologyId) {
        List<AgentIntentPO> pos = mapper.selectByOntologyId(ontologyId);
        return pos.stream().map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) {
        AgentIntentPO po = mapper.selectById(id);
        if (po != null) mapper.deleteById(id);
    }

    private AgentIntentPO toPO(AgentIntent entity) {
        return AgentIntentPO.builder().id(entity.getId())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }

    private AgentIntent fromPO(AgentIntentPO po) {
        return AgentIntent.builder().id(po.getId())
                .createdAt(po.getCreatedAt()).updatedAt(po.getUpdatedAt()).build();
    }

    private AgentIntentResponse toResponse(AgentIntent entity) {
        return AgentIntentResponse.builder().id(entity.getId())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }
}

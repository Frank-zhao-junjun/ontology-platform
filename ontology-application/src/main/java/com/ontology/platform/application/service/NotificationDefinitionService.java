package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreateNotificationDefinitionRequest;
import com.ontology.platform.application.dto.domain.NotificationDefinitionResponse;
import com.ontology.platform.domain.entity.NotificationDefinition;
import com.ontology.platform.infrastructure.persistence.NotificationDefinitionPO;
import com.ontology.platform.infrastructure.persistence.NotificationDefinitionPOMapper;
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
public class NotificationDefinitionService {

    private final NotificationDefinitionPOMapper mapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public NotificationDefinitionResponse create(String ontologyId, CreateNotificationDefinitionRequest request, String userId) {
        log.info("Creating NotificationDefinition: ontologyId={}, name={}", ontologyId, request.getNotificationDefinitionName());
        
        NotificationDefinition entity = NotificationDefinition.create(ontologyId, request.getNotificationDefinitionName(), null);
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        NotificationDefinitionPO po = toPO(entity);
        mapper.insert(po);
        
        return toResponse(entity);
    }

    public NotificationDefinitionResponse getById(String id) {
        NotificationDefinitionPO po = mapper.selectById(id);
        if (po == null) return null;
        return toResponse(fromPO(po));
    }

    public List<NotificationDefinitionResponse> listByOntologyId(String ontologyId) {
        List<NotificationDefinitionPO> pos = mapper.selectByOntologyId(ontologyId);
        return pos.stream().map(po -> toResponse(fromPO(po))).collect(Collectors.toList());
    }

    @Transactional
    public NotificationDefinitionResponse update(String id, CreateNotificationDefinitionRequest request) {
        NotificationDefinitionPO po = mapper.selectById(id);
        if (po == null) throw new RuntimeException("NotificationDefinition not found: " + id);
        
        NotificationDefinition entity = fromPO(po);
        mapRequestToEntity(request, entity);
        entity.setUpdatedAt(Instant.now());
        
        mapper.updateById(toPO(entity));
        return toResponse(entity);
    }

    @Transactional
    public void delete(String id) {
        NotificationDefinitionPO po = mapper.selectById(id);
        if (po != null) {
            po.setDeleted(true);
            po.setUpdatedAt(Instant.now());
            mapper.updateById(po);
        }
    }

    // ── mapping helpers ──

        private void mapRequestToEntity(CreateNotificationDefinitionRequest req, NotificationDefinition entity) {
        if (req.getNotifName() != null) entity.setNotifName(req.getNotifName());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getChannel() != null) entity.setChannel(req.getChannel());
        if (req.getTemplate() != null) entity.setTemplate(req.getTemplate());
        if (req.getRecipients() != null) entity.setRecipients(req.getRecipients());
        if (req.getTriggerEvent() != null) entity.setTriggerEvent(req.getTriggerEvent());
        if (req.getEnabled() != null) entity.setEnabled(req.getEnabled());
        if (req.getConfig() != null) entity.setConfig(req.getConfig());
    }

        private NotificationDefinitionPO toPO(NotificationDefinition entity) {
        return NotificationDefinitionPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .notifName(entity.getNotifName())
                .description(entity.getDescription())
                .channel(entity.getChannel())
                .template(entity.getTemplate())
                .recipients(entity.getRecipients())
                .triggerEvent(entity.getTriggerEvent())
                .enabled(entity.getEnabled())
                .config(entity.getConfig())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

        private NotificationDefinition fromPO(NotificationDefinitionPO po) {
        return NotificationDefinition.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .notifName(po.getNotifName())
                .description(po.getDescription())
                .channel(po.getChannel())
                .template(po.getTemplate())
                .recipients(po.getRecipients())
                .triggerEvent(po.getTriggerEvent())
                .enabled(po.getEnabled())
                .config(po.getConfig())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
    }

        private NotificationDefinitionResponse toResponse(NotificationDefinition entity) {
        return NotificationDefinitionResponse.builder()
                .id(entity.getId())
                .notifName(entity.getNotifName())
                .description(entity.getDescription())
                .channel(entity.getChannel())
                .template(entity.getTemplate())
                .recipients(entity.getRecipients())
                .triggerEvent(entity.getTriggerEvent())
                .enabled(entity.getEnabled())
                .config(entity.getConfig())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

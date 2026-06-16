package com.ontology.platform.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.domain.CreateNotificationDefinitionRequest;
import com.ontology.platform.application.dto.domain.NotificationDefinitionResponse;
import com.ontology.platform.domain.entity.NotificationDefinition;
import com.ontology.platform.domain.repository.NotificationDefinitionRepository;
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

    private final NotificationDefinitionRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public NotificationDefinitionResponse create(String ontologyId, CreateNotificationDefinitionRequest request, String userId) {
        log.info("Creating NotificationDefinition: ontologyId={}, name={}", ontologyId, request.getNotifName());
        
        NotificationDefinition entity = NotificationDefinition.create(ontologyId, request.getNotifName(), request.getChannel());
        // Map request fields to entity
        mapRequestToEntity(request, entity);
        
        entity = repository.save(entity);
        
        return toResponse(entity);
    }

    public NotificationDefinitionResponse getById(String id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    public List<NotificationDefinitionResponse> listByOntologyId(String ontologyId) {
        return repository.findByOntologyId(ontologyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationDefinitionResponse update(String id, CreateNotificationDefinitionRequest request) {
        NotificationDefinition entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("NotificationDefinition not found: " + id));
        
        mapRequestToEntity(request, entity);
        entity.setUpdatedAt(Instant.now());
        
        entity = repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public void delete(String id) {
        repository.deleteById(id);
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

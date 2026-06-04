package com.ontology.platform.application.service;

import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.enums.InvocationMode;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.entity.DomainEventDefinition;
import com.ontology.platform.domain.entity.OntologyAction;
import com.ontology.platform.domain.entity.ValidationRule;
import com.ontology.platform.domain.repository.ActionRuleLinkRepository;
import com.ontology.platform.domain.repository.DomainEventRepository;
import com.ontology.platform.domain.repository.OntologyActionRepository;
import com.ontology.platform.domain.repository.ValidationRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BehaviorService {
    private final ModelingService modelingService;
    private final OntologyActionRepository actionRepo;
    private final ValidationRuleRepository ruleRepo;
    private final ActionRuleLinkRepository linkRepo;
    private final DomainEventRepository eventRepo;

    public ValidationRule createRule(String contextId, String manifestCode, String name, String ruleType,
                                     String expressionJson, String errorMessage, String failurePayloadSchema) {
        if (ruleRepo.existsByContextIdAndManifestCode(contextId, manifestCode)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "规则 manifestCode 已存在: " + manifestCode);
        }
        ValidationRule rule = ValidationRule.create(contextId, manifestCode, name, ruleType,
                expressionJson, errorMessage, failurePayloadSchema);
        ruleRepo.save(rule);
        return rule;
    }

    public List<ValidationRule> listRules(String contextId) {
        return ruleRepo.findByContextId(contextId);
    }

    public ValidationRule getRule(String ruleId) {
        return ruleRepo.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found: " + ruleId));
    }

    public OntologyAction createAction(String contextId, String manifestCode, String name, String nameEn,
                                       String description, String aggregateRootId, InvocationMode invocationMode,
                                       String parametersJson, String publishesEventIdsJson,
                                       String allowedStateFromJson, String businessScenarioIdsJson,
                                       String mcpToolName) {
        if (actionRepo.existsByContextIdAndManifestCode(contextId, manifestCode)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "行为 manifestCode 已存在: " + manifestCode);
        }
        modelingService.getAggregateRoot(aggregateRootId);
        OntologyAction action = OntologyAction.create(contextId, manifestCode, name, nameEn, description,
                aggregateRootId, invocationMode, parametersJson, publishesEventIdsJson,
                allowedStateFromJson, businessScenarioIdsJson, mcpToolName);
        actionRepo.save(action);
        return action;
    }

    public List<OntologyAction> listActions(String contextId) {
        return actionRepo.findByContextId(contextId);
    }

    public OntologyAction getAction(String actionId) {
        return actionRepo.findById(actionId)
                .orElseThrow(() -> new ResourceNotFoundException("Action not found: " + actionId));
    }

    public void linkRulesToAction(String actionId, List<String> ruleIdsInOrder) {
        OntologyAction action = getAction(actionId);
        if (ruleIdsInOrder != null) {
            for (String ruleId : ruleIdsInOrder) {
                ValidationRule rule = getRule(ruleId);
                if (!rule.getContextId().equals(action.getContextId())) {
                    throw new BusinessException(ErrorCode.VALIDATION_ERROR, "规则与行为不属于同一上下文");
                }
            }
        }
        linkRepo.replaceLinks(actionId, ruleIdsInOrder);
    }

    public List<String> listRuleIdsForAction(String actionId) {
        getAction(actionId);
        return linkRepo.findRuleIdsByActionId(actionId);
    }

    public DomainEventDefinition createDomainEvent(String contextId, String manifestCode, String name,
                                                   String nameEn, String aggregateRootId, String triggerActionId,
                                                   String payloadSchemaJson) {
        if (eventRepo.existsByContextIdAndManifestCode(contextId, manifestCode)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "事件 manifestCode 已存在: " + manifestCode);
        }
        modelingService.getAggregateRoot(aggregateRootId);
        if (triggerActionId != null && !triggerActionId.isBlank()) {
            getAction(triggerActionId);
        }
        DomainEventDefinition event = DomainEventDefinition.create(contextId, manifestCode, name, nameEn,
                aggregateRootId, triggerActionId, payloadSchemaJson);
        eventRepo.save(event);
        return event;
    }

    public List<DomainEventDefinition> listDomainEvents(String contextId) {
        return eventRepo.findByContextId(contextId);
    }
}

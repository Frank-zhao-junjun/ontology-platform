package com.ontology.platform.application.service.impl;

import com.ontology.platform.application.dto.domain.*;
import com.ontology.platform.application.service.DomainQueryService;
import com.ontology.platform.domain.entity.*;
import com.ontology.platform.domain.repository.*;
import com.ontology.platform.domain.repository.behavior.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DomainQueryServiceImpl implements DomainQueryService {

    private final ActionDefinitionRepository actionDefinitionRepository;
    private final StateMachineRepository stateMachineRepository;
    private final StateTransitionRepository stateTransitionRepository;
    private final DomainEventRepository domainEventRepository;
    private final CausalityRepository causalityRepository;
    private final EpcStepRepository epcStepRepository;

    @Override
    public List<ActionDefinitionResponse> queryActions(String ontologyId, String entityId) {
        List<ActionDefinition> actions;
        if (entityId != null && !entityId.isBlank()) {
            actions = actionDefinitionRepository.findByOntologyIdAndEntityId(ontologyId, entityId);
        } else {
            actions = actionDefinitionRepository.findByOntologyId(ontologyId);
        }

        return actions.stream()
                .map(this::toActionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventDefinitionResponse> queryEvents(String ontologyId, String entityId) {
        List<DomainEvent> events;
        if (entityId != null && !entityId.isBlank()) {
            events = domainEventRepository.findByOntologyIdAndEntityId(ontologyId, entityId);
        } else {
            events = domainEventRepository.findByOntologyId(ontologyId);
        }

        return events.stream()
                .map(this::toEventResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EpcStepResponse> queryEpc(String ontologyId, String flowName) {
        List<EpcStep> steps;
        if (flowName != null && !flowName.isBlank()) {
            steps = epcStepRepository.findByOntologyIdAndFlowName(ontologyId, flowName);
        } else {
            steps = epcStepRepository.findByOntologyId(ontologyId);
        }

        return steps.stream()
                .map(this::toEpcResponse)
                .collect(Collectors.toList());
    }

    private ActionDefinitionResponse toActionResponse(ActionDefinition action) {
        List<StateMachine> machines = stateMachineRepository
                .findByOntologyIdAndEntityId(action.getOntologyId(), action.getEntityId());

        List<StateMachineResponse> machineResponses = machines.stream()
                .map(this::toStateMachineResponse)
                .collect(Collectors.toList());

        return ActionDefinitionResponse.builder()
                .id(action.getId())
                .name(action.getName())
                .displayName(action.getDisplayName())
                .description(action.getDescription())
                .actionType(action.getActionType())
                .domain(action.getDomain())
                .riskLevel(action.getRiskLevel())
                .isAsync(action.getIsAsync())
                .timeoutMs(action.getTimeoutMs())
                .entityId(action.getEntityId())
                .inputSchema(action.getInputSchema())
                .outputSchema(action.getOutputSchema())
                .preRules(action.getPreRules())
                .postRules(action.getPostRules())
                .stateMachines(machineResponses)
                .build();
    }

    private StateMachineResponse toStateMachineResponse(StateMachine machine) {
        List<StateTransition> transitions = stateTransitionRepository
                .findByStateMachineId(machine.getId());

        List<StateTransitionResponse> transitionResponses = transitions.stream()
                .map(t -> StateTransitionResponse.builder()
                        .id(t.getId())
                        .fromState(t.getFromState())
                        .toState(t.getToState())
                        .trigger(t.getTrigger())
                        .guardCondition(t.getGuardCondition())
                        .build())
                .collect(Collectors.toList());

        return StateMachineResponse.builder()
                .id(machine.getId())
                .name(machine.getName())
                .entityId(machine.getEntityId())
                .initialState(machine.getInitialState())
                .states(machine.getStates())
                .transitions(transitionResponses)
                .build();
    }

    private EventDefinitionResponse toEventResponse(DomainEvent event) {
        List<Causality> causalities = new ArrayList<>();
        causalities.addAll(causalityRepository.findByCauseEventId(event.getId()));
        causalities.addAll(causalityRepository.findByEffectEventId(event.getId()));

        List<CausalityResponse> causalityResponses = causalities.stream()
                .map(c -> CausalityResponse.builder()
                        .id(c.getId())
                        .causeEventId(c.getCauseEventId())
                        .effectEventId(c.getEffectEventId())
                        .description(c.getDescription())
                        .delayMs(c.getDelayMs())
                        .condition(c.getCondition())
                        .build())
                .collect(Collectors.toList());

        return EventDefinitionResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .displayName(event.getDisplayName())
                .description(event.getDescription())
                .eventType(event.getEventType())
                .severity(event.getSeverity())
                .entityId(event.getEntityId())
                .payloadSchema(event.getPayloadSchema())
                .source(event.getSource())
                .causalities(causalityResponses)
                .build();
    }

    private EpcStepResponse toEpcResponse(EpcStep step) {
        String eventName = null;
        if (step.getTriggerEventId() != null) {
            eventName = domainEventRepository.findById(step.getTriggerEventId())
                    .map(DomainEvent::getName).orElse(null);
        }

        String actionName = null;
        if (step.getActionId() != null) {
            actionName = actionDefinitionRepository.findById(step.getActionId())
                    .map(ActionDefinition::getName).orElse(null);
        }

        return EpcStepResponse.builder()
                .id(step.getId())
                .flowName(step.getFlowName())
                .stepOrder(step.getStepOrder())
                .triggerEventId(step.getTriggerEventId())
                .triggerEventName(eventName)
                .actionId(step.getActionId())
                .actionName(actionName)
                .conditions(step.getConditions())
                .guards(step.getGuards())
                .timeoutMs(step.getTimeoutMs())
                .build();
    }
}

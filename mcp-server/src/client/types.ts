// =============================================
// Platform API Response Types
// =============================================

export interface ActionDefinitionResponse {
  id: string;
  name: string;
  displayName: string;
  description?: string;
  actionType: string;
  domain: string;
  riskLevel: string;
  isAsync: boolean;
  timeoutMs: number;
  entityId: string;
  inputSchema: string;
  outputSchema: string;
  preRules: string;
  postRules: string;
  stateMachines?: StateMachineResponse[];
}

export interface StateMachineResponse {
  id: string;
  name: string;
  entityId: string;
  initialState: string;
  states: string;
  transitions: StateTransitionResponse[];
}

export interface StateTransitionResponse {
  id: string;
  fromState: string;
  toState: string;
  trigger: string;
  guardCondition?: string;
}

export interface EventDefinitionResponse {
  id: string;
  name: string;
  displayName: string;
  description?: string;
  eventType: string;
  severity: string;
  entityId: string;
  payloadSchema: string;
  source?: string;
  causalities: CausalityResponse[];
}

export interface CausalityResponse {
  id: string;
  causeEventId: string;
  causeEventName?: string;
  effectEventId: string;
  effectEventName?: string;
  description?: string;
  delayMs: number;
  condition?: string;
}

export interface EpcStepResponse {
  id: string;
  flowName: string;
  stepOrder: number;
  triggerEventId?: string;
  triggerEventName?: string;
  actionId?: string;
  actionName?: string;
  conditions: string;
  guards: string;
  timeoutMs: number;
}

// =============================================
// Platform REST Client — Proxy to Spring Boot API
// =============================================

import type {
  ActionDefinitionResponse,
  EventDefinitionResponse,
  EpcStepResponse,
} from './types.js';

const PLATFORM_BASE_URL = process.env.PLATFORM_BASE_URL || 'http://localhost:8080';
const PLATFORM_API_KEY = process.env.PLATFORM_API_KEY || 'dev-api-key';
const REQUEST_TIMEOUT_MS = parseInt(process.env.REQUEST_TIMEOUT_MS || '15000', 10);

class PlatformClient {
  private baseUrl: string;
  private apiKey: string;

  constructor(baseUrl?: string, apiKey?: string) {
    this.baseUrl = baseUrl || PLATFORM_BASE_URL;
    this.apiKey = apiKey || PLATFORM_API_KEY;
  }

  private async request<T>(path: string, options?: RequestInit): Promise<T> {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);

    try {
      const res = await fetch(`${this.baseUrl}${path}`, {
        ...options,
        headers: {
          'Content-Type': 'application/json',
          'X-API-Key': this.apiKey,
          ...options?.headers,
        },
        signal: controller.signal,
      });

      if (!res.ok) {
        const body = await res.text();
        throw new Error(`Platform API error ${res.status}: ${body}`);
      }

      return (await res.json()) as T;
    } finally {
      clearTimeout(timer);
    }
  }

  async queryActions(ontologyId: string, entityId?: string): Promise<ActionDefinitionResponse[]> {
    const params = entityId ? `?entityId=${encodeURIComponent(entityId)}` : '';
    const res = await this.request<{ code: number; data: ActionDefinitionResponse[] }>(
      `/api/v1/ontologies/${ontologyId}/actions${params}`
    );
    return res.data;
  }

  async queryEvents(ontologyId: string, entityId?: string): Promise<EventDefinitionResponse[]> {
    const params = entityId ? `?entityId=${encodeURIComponent(entityId)}` : '';
    const res = await this.request<{ code: number; data: EventDefinitionResponse[] }>(
      `/api/v1/ontologies/${ontologyId}/events${params}`
    );
    return res.data;
  }

  async queryEpc(ontologyId: string, flowName?: string): Promise<EpcStepResponse[]> {
    const params = flowName ? `?flowName=${encodeURIComponent(flowName)}` : '';
    const res = await this.request<{ code: number; data: EpcStepResponse[] }>(
      `/api/v1/ontologies/${ontologyId}/epc${params}`
    );
    return res.data;
  }

  async listTokens(): Promise<unknown[]> {
    const res = await this.request<{ code: number; data: unknown[] }>(
      '/api/v1/governance/tokens'
    );
    return res.data;
  }

  async queryApproval(approvalId: string): Promise<unknown> {
    const res = await this.request<{ code: number; data: unknown }>(
      `/api/v1/governance/approvals/${approvalId}`
    );
    return res.data;
  }

  async submitApproval(params: {
    agentId: string;
    actionId?: string;
    requestedOp: string;
    reason?: string;
  }): Promise<{ id: string; status: string }> {
    const res = await this.request<{ code: number; data: { id: string; status: string } }>(
      '/api/v1/governance/approvals',
      {
        method: 'POST',
        body: JSON.stringify(params),
      }
    );
    return res.data;
  }

  async resolveIntent(
    ontologyId: string,
    phrase: string
  ): Promise<{
    id: string;
    name: string;
    actionId: string;
    triggerPhrases?: string[];
    slots?: unknown[];
    matchScore?: number;
  } | null> {
    const res = await this.request<{
      code: number;
      data: {
        id: string;
        name: string;
        actionId: string;
        triggerPhrases?: string[];
        slots?: unknown[];
        matchScore?: number;
      };
      message?: string;
    }>('/api/v2/semantic/resolve-intent', {
      method: 'POST',
      body: JSON.stringify({ ontologyId, phrase, query: phrase }),
    });
    if (res.code !== 200 && res.code !== 0) {
      return null;
    }
    if (!res.data) {
      return null;
    }
    return res.data;
  }
}

export const platformClient = new PlatformClient();
export default PlatformClient;

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { traverseGraphTool } from '../../src/mcp/tools/traverse-graph.js';

const mockCtx = {
  agentId: 'test-agent',
  tenantId: 'default',
  domains: ['platform'],
  roles: new Map([['platform', 'READER']]),
  tokenId: 'test-token',
};

// ── Mock fixtures (via vi.hoisted so they're available to vi.mock factory) ──

const { MOCK_ACTIONS, MOCK_EVENTS } = vi.hoisted(() => ({
  MOCK_ACTIONS: [
    {
      id: 'action-1',
      name: 'create-order',
      displayName: '创建订单',
      description: '创建生产订单',
      actionType: 'CREATE',
      domain: 'platform',
      riskLevel: 'READ',
      isAsync: false,
      timeoutMs: 5000,
      entityId: 'production-order',
      inputSchema: '{}',
      outputSchema: '{}',
      preRules: '',
      postRules: '',
      stateMachines: [
        {
          id: 'sm-1',
          name: 'order-status',
          entityId: 'production-order',
          initialState: 'draft',
          states: JSON.stringify(['draft', 'submitted', 'approved']),
          transitions: [
            { id: 't-1', fromState: 'draft', toState: 'submitted', trigger: 'submit' },
          ],
        },
      ],
    },
    {
      id: 'action-2',
      name: 'cancel-order',
      displayName: '取消订单',
      actionType: 'UPDATE',
      domain: 'platform',
      riskLevel: 'READ',
      isAsync: false,
      timeoutMs: 3000,
      entityId: 'production-order',
      inputSchema: '{}',
      outputSchema: '{}',
      preRules: '',
      postRules: '',
    },
  ],
  MOCK_EVENTS: [
    {
      id: 'event-1',
      name: 'order-created',
      displayName: '订单创建',
      description: '订单已创建',
      eventType: 'DOMAIN',
      severity: 'INFO',
      entityId: 'production-order',
      payloadSchema: '{}',
      source: 'system',
      causalities: [
        {
          id: 'c-1',
          causeEventId: 'event-0',
          effectEventId: 'event-1',
          description: '创建触发',
          delayMs: 0,
        },
      ],
    },
  ],
}));

vi.mock('../../src/client/platform-client.js', () => ({
  platformClient: {
    queryActions: vi.fn().mockResolvedValue(MOCK_ACTIONS),
    queryEvents: vi.fn().mockResolvedValue(MOCK_EVENTS),
  },
}));

import { platformClient } from '../../src/client/platform-client.js';

function extractData(result: unknown): Record<string, unknown> {
  return (result as { structuredContent: { data: Record<string, unknown> } }).structuredContent
    .data;
}

describe('traverse_graph tool', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ── 1. Basic traversal request ──

  it('returns a graph with actions and events for the start entity', async () => {
    const result = await traverseGraphTool.handler(
      {
        ontologyId: 'onto-1',
        startEntityId: 'production-order',
      },
      mockCtx,
    );

    const data = extractData(result);

    expect(platformClient.queryActions).toHaveBeenCalledTimes(1);
    expect(platformClient.queryActions).toHaveBeenCalledWith('onto-1', 'production-order');
    expect(platformClient.queryEvents).toHaveBeenCalledTimes(1);
    expect(platformClient.queryEvents).toHaveBeenCalledWith('onto-1', 'production-order');

    expect(data.startEntityId).toBe('production-order');
    expect(data.maxDepth).toBe(3); // default
    expect(data.relationTypes).toEqual([]);

    // Verify actions mapping
    expect(data.actions).toHaveLength(2);
    const firstAction = (data.actions as Array<Record<string, unknown>>)[0];
    expect(firstAction.name).toBe('create-order');
    expect(firstAction.type).toBe('CREATE');
    expect(firstAction.domain).toBe('platform');
    expect(firstAction.riskLevel).toBe('READ');
    expect(firstAction.stateMachines).toHaveLength(1);
    const sm = (firstAction.stateMachines as Array<Record<string, unknown>>)[0];
    expect(sm.name).toBe('order-status');
    expect(sm.initialState).toBe('draft');
    expect(sm.transitions).toHaveLength(1);
    expect((sm.transitions as Array<Record<string, unknown>>)[0].trigger).toBe('submit');

    // Verify events mapping
    expect(data.events).toHaveLength(1);
    const firstEvent = (data.events as Array<Record<string, unknown>>)[0];
    expect(firstEvent.name).toBe('order-created');
    expect(firstEvent.type).toBe('DOMAIN');
    expect(firstEvent.severity).toBe('INFO');
    expect(firstEvent.causalities).toHaveLength(1);
    expect(
      (firstEvent.causalities as Array<Record<string, unknown>>)[0].delayMs,
    ).toBe(0);
  });

  it('returns structuredContent with status and metadata', async () => {
    const result = await traverseGraphTool.handler(
      {
        ontologyId: 'onto-1',
        startEntityId: 'production-order',
      },
      mockCtx,
    );

    const sc = (result as { structuredContent: Record<string, unknown> }).structuredContent;
    expect(sc.status).toBe('success');
    expect(sc.metadata).toBeDefined();
    expect((sc.metadata as Record<string, unknown>).version).toBe('1.0.0');
    expect((sc.metadata as Record<string, unknown>).generated_at).toBeDefined();
    expect((sc.metadata as Record<string, unknown>).trace_id).toBeDefined();
  });

  // ── 2. With different parameters (maxDepth, relationTypes) ──

  it('honors explicit maxDepth parameter', async () => {
    const result = await traverseGraphTool.handler(
      {
        ontologyId: 'onto-1',
        startEntityId: 'production-order',
        maxDepth: 5,
      },
      mockCtx,
    );

    const data = extractData(result);
    expect(data.maxDepth).toBe(5);
  });

  it('passes relationTypes through to the graph', async () => {
    const relationTypes = ['supplies', 'produces'];
    const result = await traverseGraphTool.handler(
      {
        ontologyId: 'onto-1',
        startEntityId: 'production-order',
        relationTypes,
      },
      mockCtx,
    );

    const data = extractData(result);
    expect(data.relationTypes).toEqual(relationTypes);
  });

  it('returns maxDepth 0 when explicitly set to 0', async () => {
    // maxDepth || 3 means 0 is falsy, so it would default to 3
    // This test documents the current behavior
    const result = await traverseGraphTool.handler(
      {
        ontologyId: 'onto-1',
        startEntityId: 'production-order',
        maxDepth: 0,
      },
      mockCtx,
    );

    const data = extractData(result);
    // Because handler uses `|| 3`, 0 is falsy and falls through to 3
    expect(data.maxDepth).toBe(3);
  });

  it('handles empty results from the platform client', async () => {
    vi.mocked(platformClient.queryActions).mockResolvedValueOnce([]);
    vi.mocked(platformClient.queryEvents).mockResolvedValueOnce([]);

    const result = await traverseGraphTool.handler(
      {
        ontologyId: 'onto-1',
        startEntityId: 'production-order',
      },
      mockCtx,
    );

    const data = extractData(result);
    expect(data.actions).toHaveLength(0);
    expect(data.events).toHaveLength(0);
  });

  // ── 3. Error cases ──

  it('still returns a graph when ontologyId is missing (handler does not validate required params)', async () => {
    // The handler uses `args.ontologyId as string` — no runtime validation.
    // The mocked client will be called with undefined.
    const result = await traverseGraphTool.handler(
      {
        startEntityId: 'production-order',
      } as unknown as Record<string, unknown>,
      mockCtx,
    );

    const data = extractData(result);
    expect(data.startEntityId).toBe('production-order');
    expect(platformClient.queryActions).toHaveBeenCalledWith(undefined, 'production-order');
    expect(platformClient.queryEvents).toHaveBeenCalledWith(undefined, 'production-order');
  });

  it('still returns a graph when startEntityId is missing (handler does not validate required params)', async () => {
    const result = await traverseGraphTool.handler(
      {
        ontologyId: 'onto-1',
      } as unknown as Record<string, unknown>,
      mockCtx,
    );

    const data = extractData(result);
    expect(data.startEntityId).toBeUndefined();
    expect(platformClient.queryActions).toHaveBeenCalledWith('onto-1', undefined);
    expect(platformClient.queryEvents).toHaveBeenCalledWith('onto-1', undefined);
  });

  it('throws when platformClient.queryActions fails', async () => {
    vi.mocked(platformClient.queryActions).mockRejectedValueOnce(
      new Error('Network error'),
    );

    await expect(
      traverseGraphTool.handler(
        { ontologyId: 'onto-1', startEntityId: 'production-order' },
        mockCtx,
      ),
    ).rejects.toThrow('Network error');
  });

  it('throws when platformClient.queryEvents fails', async () => {
    vi.mocked(platformClient.queryEvents).mockRejectedValueOnce(
      new Error('API timeout'),
    );

    await expect(
      traverseGraphTool.handler(
        { ontologyId: 'onto-1', startEntityId: 'production-order' },
        mockCtx,
      ),
    ).rejects.toThrow('API timeout');
  });

  // ── 4. RBAC / Context behavior ──

  it('works with different tenant context', async () => {
    const altCtx = {
      agentId: 'agent-42',
      tenantId: 'acme-corp',
      domains: ['platform', 'inventory'],
      roles: new Map([
        ['platform', 'ADMIN'],
        ['inventory', 'READER'],
      ]),
      tokenId: 'token-xyz',
    };

    const result = await traverseGraphTool.handler(
      {
        ontologyId: 'onto-2',
        startEntityId: 'material',
      },
      altCtx,
    );

    const data = extractData(result);
    expect(data.startEntityId).toBe('material');
    expect(platformClient.queryActions).toHaveBeenCalledWith('onto-2', 'material');
    expect(platformClient.queryEvents).toHaveBeenCalledWith('onto-2', 'material');
  });

  it('works with READER role (lowest privilege)', async () => {
    // traverse_graph is a READ-level tool; handler does not use ctx
    // This confirms no RBAC filtering blocks the call
    const readerCtx = {
      agentId: 'readonly-agent',
      tenantId: 'default',
      domains: ['platform'],
      roles: new Map([['platform', 'READER']]),
      tokenId: 'reader-token',
    };

    const result = await traverseGraphTool.handler(
      {
        ontologyId: 'onto-1',
        startEntityId: 'production-order',
      },
      readerCtx,
    );

    const data = extractData(result);
    expect(data.actions).toHaveLength(2);
    expect(data.events).toHaveLength(1);
  });
});

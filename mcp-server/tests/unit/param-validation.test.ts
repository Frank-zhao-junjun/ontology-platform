import { describe, it, expect, vi, beforeEach } from 'vitest';
import { traverseGraphTool } from '../../src/mcp/tools/traverse-graph.js';
import { queryOntologyTool } from '../../src/mcp/tools/query-ontology.js';
import { resolveIntentTool } from '../../src/mcp/tools/resolve-intent.js';
import { validateInstructionTool } from '../../src/mcp/tools/validate-instruction.js';
import { executeActionTool } from '../../src/mcp/tools/execute-action.js';

const mockCtx = {
  agentId: 'test-agent',
  tenantId: 'default',
  domains: ['platform'],
  roles: new Map([['platform', 'READER']]),
  tokenId: 'test-token',
};

// ── Mock fixtures (via vi.hoisted so they're available to vi.mock factory) ──

const { MOCK_ACTIONS, MOCK_EPC_RESULT } = vi.hoisted(() => ({
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
      inputSchema: '{"type":"object","properties":{"qty":{"type":"number"}},"required":["qty"]}',
      outputSchema: '{}',
      preRules: '',
      postRules: '',
      stateMachines: [],
    },
    {
      id: 'action-2',
      name: 'delete-order',
      displayName: '删除订单',
      description: '删除生产订单',
      actionType: 'DELETE',
      domain: 'platform',
      riskLevel: 'DELETE',
      isAsync: false,
      timeoutMs: 3000,
      entityId: 'production-order',
      inputSchema: '{}',
      outputSchema: '{}',
      preRules: '',
      postRules: '',
    },
  ],
  MOCK_EPC_RESULT: { steps: [] },
}));

vi.mock('../../src/client/platform-client.js', () => ({
  platformClient: {
    queryActions: vi.fn().mockResolvedValue(MOCK_ACTIONS),
    queryEvents: vi.fn().mockResolvedValue([]),
    queryEpc: vi.fn().mockResolvedValue(MOCK_EPC_RESULT),
    queryEpcCoverage: vi.fn().mockResolvedValue({}),
    querySemanticLayer: vi.fn().mockResolvedValue({}),
    queryLifecycle: vi.fn().mockResolvedValue({}),
    resolveIntent: vi.fn().mockResolvedValue(null),
    submitApproval: vi.fn().mockResolvedValue({ id: 'approval-1' }),
  },
}));

vi.mock('../../src/rule-engine.js', () => ({
  evaluateRules: vi.fn().mockReturnValue([]),
}));

import { platformClient } from '../../src/client/platform-client.js';
import { evaluateRules } from '../../src/rule-engine.js';

// ── Helpers ──

function extractData(result: unknown): Record<string, unknown> {
  return (result as { structuredContent: { data: Record<string, unknown> } }).structuredContent
    .data;
}

function extractStatus(result: unknown): string {
  return (result as { structuredContent: { status: string } }).structuredContent.status;
}

// =============================================================================
// traverse_graph — param validation
// =============================================================================

describe('traverse_graph — parameter validation', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('handles missing required ontologyId gracefully', async () => {
    const result = await traverseGraphTool.handler(
      { startEntityId: 'production-order' } as unknown as Record<string, unknown>,
      mockCtx,
    );

    const data = extractData(result);
    expect(data.startEntityId).toBe('production-order');
    // ontologyId is undefined; mock receives undefined
    expect(platformClient.queryActions).toHaveBeenCalledWith(undefined, 'production-order');
    expect(platformClient.queryEvents).toHaveBeenCalledWith(undefined, 'production-order');
  });

  it('handles missing required startEntityId gracefully', async () => {
    const result = await traverseGraphTool.handler(
      { ontologyId: 'onto-1' } as unknown as Record<string, unknown>,
      mockCtx,
    );

    const data = extractData(result);
    expect(data.startEntityId).toBeUndefined();
    expect(platformClient.queryEvents).toHaveBeenCalledWith('onto-1', undefined);
  });

  it('handles relationTypes passed as string instead of array', async () => {
    // Handler casts `args.relationTypes as string[] | undefined`;
    // a string value is truthy, so it passes through as-is into the graph
    const result = await traverseGraphTool.handler(
      {
        ontologyId: 'onto-1',
        startEntityId: 'production-order',
        relationTypes: 'supplies' as unknown as string[],
      },
      mockCtx,
    );

    const data = extractData(result);
    expect(data.relationTypes).toBe('supplies');
  });

  it('handles maxDepth passed as string instead of number (passes through truthy value)', async () => {
    // Handler casts `args.maxDepth as number` then uses `|| 3`.
    // A non-empty string is truthy, so it passes through as-is without defaulting.
    const result = await traverseGraphTool.handler(
      {
        ontologyId: 'onto-1',
        startEntityId: 'production-order',
        maxDepth: 'infinite' as unknown as number,
      },
      mockCtx,
    );

    const data = extractData(result);
    // truthy string passes through the `|| 3` fallback
    expect(data.maxDepth).toBe('infinite');
  });
});

// =============================================================================
// query_ontology — param validation
// =============================================================================

describe('query_ontology — parameter validation', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('handles missing required ontologyId gracefully', async () => {
    const result = await queryOntologyTool.handler(
      {} as unknown as Record<string, unknown>,
      mockCtx,
    );

    const data = extractData(result);
    expect(data.ontologyId).toBeUndefined();
    // With no entities and includeActions=true (default), calls queryActions with one arg
    expect(platformClient.queryActions).toHaveBeenCalledWith(undefined);
  });

  it('handles empty string ontologyId gracefully', async () => {
    const result = await queryOntologyTool.handler(
      { ontologyId: '' },
      mockCtx,
    );

    const data = extractData(result);
    expect(data.ontologyId).toBe('');
  });

  it('iterates over characters when entities is a string instead of array', async () => {
    // String is iterable in JS, so `for (const eid of entities)` iterates character-by-character
    const result = await queryOntologyTool.handler(
      { ontologyId: 'onto-1', entities: 'ab' as unknown as string[], includeEvents: false },
      mockCtx,
    );

    const data = extractData(result);
    // Two characters = two queryActions calls (one per char)
    expect(platformClient.queryActions).toHaveBeenCalledTimes(2);
    expect(platformClient.queryActions).toHaveBeenCalledWith('onto-1', 'a');
    expect(platformClient.queryActions).toHaveBeenCalledWith('onto-1', 'b');
  });

  it('throws when includeLifecycle is true but entityId is missing', async () => {
    await expect(
      queryOntologyTool.handler(
        { ontologyId: 'onto-1', includeLifecycle: true },
        mockCtx,
      ),
    ).rejects.toThrow('entityId is required when includeLifecycle is true');
  });
});

// =============================================================================
// resolve_intent — param validation
// =============================================================================

describe('resolve_intent — parameter validation', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('defaults missing query to empty string and returns UNKNOWN', async () => {
    const result = await resolveIntentTool.handler(
      {} as unknown as Record<string, unknown>,
      mockCtx,
    );

    const data = extractData(result);
    expect(data.category).toBe('UNKNOWN');
    expect(data.confidence).toBe(0);
    expect(data.source).toBe('keyword-fallback');
    expect(data.entities).toEqual([]);
  });

  it('returns UNKNOWN for empty string query', async () => {
    const result = await resolveIntentTool.handler(
      { query: '' },
      mockCtx,
    );

    const data = extractData(result);
    expect(data.category).toBe('UNKNOWN');
    expect(data.confidence).toBe(0);
    expect(data.entities).toEqual([]);
  });

  it('handles excessively long query string (10k chars) without crashing', async () => {
    // Use a neutral character that doesn't match any intent pattern
    const longQuery = 'x'.repeat(10000);

    const result = await resolveIntentTool.handler(
      { query: longQuery },
      mockCtx,
    );

    const data = extractData(result);
    expect(data.category).toBe('UNKNOWN');
    expect(data.source).toBe('keyword-fallback');
  });

  it('handles special characters and injection attempts in query', async () => {
    const injectionQueries = [
      "查'; DROP TABLE users; --",
      '<script>alert("xss")</script>查询',
      '${process.env.SECRET}查看订单',
      '../../etc/passwd查询',
      '查\n\r\t 订单',
    ];

    for (const maliciousQuery of injectionQueries) {
      const result = await resolveIntentTool.handler(
        { query: maliciousQuery },
        mockCtx,
      );

      const data = extractData(result);
      expect(data.category).toBeDefined();
      expect(typeof data.confidence).toBe('number');
      expect(data.entities).toBeInstanceOf(Array);
      expect(data.source).toBe('keyword-fallback');
    }
  });

  it('handles null query value gracefully', async () => {
    const result = await resolveIntentTool.handler(
      { query: null as unknown as string },
      mockCtx,
    );

    const data = extractData(result);
    // `null as string` → `(null) || ''` → empty string
    expect(data.category).toBe('UNKNOWN');
    expect(data.confidence).toBe(0);
  });
});

// =============================================================================
// validate_instruction — param validation
// =============================================================================

describe('validate_instruction — parameter validation', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('throws when required actionName is missing (split on undefined)', async () => {
    // handler does `actionName.split('.')` where actionName is undefined → TypeError
    await expect(
      validateInstructionTool.handler(
        { entityId: 'production-order' } as unknown as Record<string, unknown>,
        mockCtx,
      ),
    ).rejects.toThrow();
  });

  it('handles missing required entityId gracefully (returns missing-fields when params also empty)', async () => {
    vi.mocked(platformClient.queryActions).mockResolvedValueOnce(MOCK_ACTIONS);

    const result = await validateInstructionTool.handler(
      { actionName: 'platform.create-order' } as unknown as Record<string, unknown>,
      mockCtx,
    );

    // entityId is undefined; the handler finds the action but fails on required param 'qty'
    expect(extractStatus(result)).toBe('error');
    const sc = (result as { structuredContent: { error?: { code: string } } }).structuredContent;
    expect(sc.error?.code).toBe('MISSING_FIELDS');
    expect(platformClient.queryActions).toHaveBeenCalledWith('default', undefined);
  });

  it('handles missing required entityId when params are provided', async () => {
    vi.mocked(platformClient.queryActions).mockResolvedValueOnce(MOCK_ACTIONS);

    const result = await validateInstructionTool.handler(
      { actionName: 'platform.create-order', params: { qty: 10 } } as unknown as Record<string, unknown>,
      mockCtx,
    );

    // With required param qty provided, validation succeeds even without entityId
    expect(extractStatus(result)).toBe('success');
  });

  it('returns action-not-found for empty string actionName', async () => {
    vi.mocked(platformClient.queryActions).mockResolvedValueOnce(MOCK_ACTIONS);

    const result = await validateInstructionTool.handler(
      { actionName: '', entityId: 'production-order' },
      mockCtx,
    );

    // Empty string split → [''], name = '', not found in MOCK_ACTIONS
    expect(extractStatus(result)).toBe('error');
    const sc = (result as { structuredContent: { error?: { code: string } } }).structuredContent;
    expect(sc.error?.code).toBe('ACTION_NOT_FOUND');
  });

  it('handles params with array instead of object type (missing required field)', async () => {
    vi.mocked(platformClient.queryActions).mockResolvedValueOnce(MOCK_ACTIONS);

    const result = await validateInstructionTool.handler(
      {
        actionName: 'platform.create-order',
        entityId: 'production-order',
        params: ['a', 'b'] as unknown as Record<string, unknown>,
      },
      mockCtx,
    );

    // Array is an object in JS, so `|| {}` keeps it; the required field 'qty'
    // is not present in the array → returns MISSING_FIELDS (does not crash)
    const sc = (result as { structuredContent: { error?: { code: string } } }).structuredContent;
    expect(sc.error?.code).toBe('MISSING_FIELDS');
  });
});

// =============================================================================
// execute_action — param validation
// =============================================================================

describe('execute_action — parameter validation', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('throws when required actionName is missing (split on undefined)', async () => {
    // handler does `actionName.split('.')` where actionName is undefined → TypeError
    await expect(
      executeActionTool.handler(
        { entityId: 'production-order' } as unknown as Record<string, unknown>,
        mockCtx,
      ),
    ).rejects.toThrow();
  });

  it('handles missing required entityId gracefully', async () => {
    vi.mocked(platformClient.queryActions).mockResolvedValueOnce(MOCK_ACTIONS);

    const result = await executeActionTool.handler(
      { actionName: 'platform.create-order' } as unknown as Record<string, unknown>,
      mockCtx,
    );

    expect(extractStatus(result)).toBe('success');
    expect(platformClient.queryActions).toHaveBeenCalledWith('default', undefined);
  });

  it('returns action-not-found for empty string actionName', async () => {
    vi.mocked(platformClient.queryActions).mockResolvedValueOnce(MOCK_ACTIONS);

    const result = await executeActionTool.handler(
      { actionName: '', entityId: 'production-order' },
      mockCtx,
    );

    const sc = (result as { structuredContent: { error?: { code: string } } }).structuredContent;
    expect(sc.error?.code).toBe('ACTION_NOT_FOUND');
  });

  it('handles injection characters in actionName (command injection attempt)', async () => {
    vi.mocked(platformClient.queryActions).mockResolvedValueOnce(MOCK_ACTIONS);

    const result = await executeActionTool.handler(
      {
        actionName: 'platform.create-order; DROP TABLE users;',
        entityId: 'production-order',
      },
      mockCtx,
    );

    // Split by '.' → domain='platform', name='create-order; DROP TABLE users;'
    // Name doesn't match any action → ACTION_NOT_FOUND
    const sc = (result as { structuredContent: { error?: { code: string } } }).structuredContent;
    expect(sc.error?.code).toBe('ACTION_NOT_FOUND');
  });

  it('handles params with wrong type (array instead of object)', async () => {
    vi.mocked(platformClient.queryActions).mockResolvedValueOnce(MOCK_ACTIONS);

    const result = await executeActionTool.handler(
      {
        actionName: 'platform.create-order',
        entityId: 'production-order',
        params: ['a', 'b'] as unknown as Record<string, unknown>,
      },
      mockCtx,
    );

    // Array is truthy, so `|| {}` keeps it; doesn't cause a crash in downstream usage
    expect(extractStatus(result)).toBe('success');
  });

  it('handles ontologyId as null (falls back to "default")', async () => {
    vi.mocked(platformClient.queryActions).mockResolvedValueOnce(MOCK_ACTIONS);

    const result = await executeActionTool.handler(
      {
        actionName: 'platform.create-order',
        entityId: 'production-order',
        ontologyId: null as unknown as string,
      },
      mockCtx,
    );

    // `(null as string) || 'default'` → 'default'
    expect(extractStatus(result)).toBe('success');
    expect(platformClient.queryActions).toHaveBeenCalledWith('default', 'production-order');
  });
});

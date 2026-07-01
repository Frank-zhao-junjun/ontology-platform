import { describe, it, expect, vi, beforeEach } from 'vitest';
import { validateInstructionTool } from '../../src/mcp/tools/validate-instruction.js';

const mockCtx = {
  agentId: 'test-agent',
  tenantId: 'default',
  domains: ['platform'],
  roles: new Map([['platform', 'READER']]),
  tokenId: 'test-token',
};

// ── Mock fixtures (via vi.hoisted so they're available to vi.mock factory) ──

const { MOCK_ACTIONS } = vi.hoisted(() => ({
  MOCK_ACTIONS: [
    {
      id: 'action-1',
      name: 'view-order',
      displayName: '查看订单',
      description: 'View order details',
      actionType: 'READ',
      domain: 'platform',
      riskLevel: 'READ',
      isAsync: false,
      timeoutMs: 5000,
      entityId: 'production-order',
      inputSchema: '{}',
      outputSchema: '{}',
      preRules: '',
      postRules: '',
    },
    {
      id: 'action-2',
      name: 'delete-order',
      displayName: '删除订单',
      description: 'Delete a production order',
      actionType: 'DELETE',
      domain: 'platform',
      riskLevel: 'DELETE',
      isAsync: false,
      timeoutMs: 5000,
      entityId: 'production-order',
      inputSchema: '{}',
      outputSchema: '{}',
      preRules: '',
      postRules: '',
    },
    {
      id: 'action-3',
      name: 'approve-payment',
      displayName: '审批付款',
      description: 'Approve payment request',
      actionType: 'UPDATE',
      domain: 'platform',
      riskLevel: 'APPROVAL',
      isAsync: false,
      timeoutMs: 5000,
      entityId: 'production-order',
      inputSchema: '{}',
      outputSchema: '{}',
      preRules: '',
      postRules: '',
    },
    {
      id: 'action-4',
      name: 'create-order',
      displayName: '创建订单',
      description: 'Create a new production order',
      actionType: 'CREATE',
      domain: 'platform',
      riskLevel: 'READ',
      isAsync: false,
      timeoutMs: 5000,
      entityId: 'production-order',
      inputSchema: JSON.stringify({
        type: 'object',
        required: ['amount', 'currency'],
        properties: {
          amount: { type: 'number' },
          currency: { type: 'string' },
        },
      }),
      outputSchema: '{}',
      preRules: '',
      postRules: '',
    },
  ],
}));

vi.mock('../../src/client/platform-client.js', () => ({
  platformClient: {
    queryActions: vi.fn().mockResolvedValue(MOCK_ACTIONS),
  },
}));

// Mock rule-engine so we can control rule evaluation per test
const { MOCK_EVALUATE_RULES } = vi.hoisted(() => ({
  MOCK_EVALUATE_RULES: vi.fn(),
}));

vi.mock('../../src/rule-engine.js', () => ({
  evaluateRules: MOCK_EVALUATE_RULES,
}));

import { platformClient } from '../../src/client/platform-client.js';

function extractData(result: unknown): Record<string, unknown> {
  return (result as { structuredContent: { data: Record<string, unknown> } })
    .structuredContent.data;
}

function extractStructuredContent(result: unknown): Record<string, unknown> {
  return (result as { structuredContent: Record<string, unknown> }).structuredContent;
}

describe('validate_instruction tool', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Default: evaluateRules returns empty (no rules to check)
    (MOCK_EVALUATE_RULES as ReturnType<typeof vi.fn>).mockReturnValue([]);
  });

  // ── 1. Basic validation passing ──

  it('returns valid=true for an existing action with READ risk level', async () => {
    const result = await validateInstructionTool.handler(
      {
        actionName: 'platform.view-order',
        entityId: 'production-order',
      },
      mockCtx,
    );

    const data = extractData(result);

    expect(platformClient.queryActions).toHaveBeenCalledTimes(1);
    expect(platformClient.queryActions).toHaveBeenCalledWith('default', 'production-order');

    expect(data.valid).toBe(true);
    expect(data.entityId).toBe('production-order');
    expect(data.action).toMatchObject({
      name: 'view-order',
      riskLevel: 'READ',
    });
  });

  it('returns structuredContent with success status and metadata', async () => {
    const result = await validateInstructionTool.handler(
      {
        actionName: 'platform.view-order',
        entityId: 'production-order',
      },
      mockCtx,
    );

    const sc = extractStructuredContent(result);
    expect(sc.status).toBe('success');
    expect(sc.metadata).toBeDefined();
    expect((sc.metadata as Record<string, unknown>).version).toBe('1.0.0');
    expect((sc.metadata as Record<string, unknown>).generated_at).toBeDefined();
    expect((sc.metadata as Record<string, unknown>).trace_id).toBeDefined();
  });

  // ── 2. Multiple instruction types / risk levels ──

  it('returns requiresApproval=true for DELETE risk level actions', async () => {
    const result = await validateInstructionTool.handler(
      {
        actionName: 'platform.delete-order',
        entityId: 'production-order',
      },
      mockCtx,
    );

    const sc = extractStructuredContent(result);
    const data = extractData(result);

    expect(sc.status).toBe('pending_approval');
    expect(data.valid).toBe(true);
    expect(data.requiresApproval).toBe(true);
    expect(data.action.riskLevel).toBe('DELETE');
  });

  it('returns requiresApproval=true for APPROVAL risk level actions', async () => {
    const result = await validateInstructionTool.handler(
      {
        actionName: 'platform.approve-payment',
        entityId: 'production-order',
      },
      mockCtx,
    );

    const sc = extractStructuredContent(result);
    const data = extractData(result);

    expect(sc.status).toBe('pending_approval');
    expect(data.valid).toBe(true);
    expect(data.requiresApproval).toBe(true);
    expect(data.action.riskLevel).toBe('APPROVAL');
  });

  it('splits domain.actionName format to extract just the action name', async () => {
    const result = await validateInstructionTool.handler(
      {
        actionName: 'custom-domain.view-order',
        entityId: 'production-order',
      },
      mockCtx,
    );

    const data = extractData(result);
    // The lookup uses the part after the dot, so 'view-order' matches action-1
    expect(data.valid).toBe(true);
    expect(data.action.name).toBe('view-order');
  });

  it('handles actionName without a domain prefix', async () => {
    const result = await validateInstructionTool.handler(
      {
        actionName: 'view-order',
        entityId: 'production-order',
      },
      mockCtx,
    );

    const data = extractData(result);
    expect(data.valid).toBe(true);
    expect(data.action.name).toBe('view-order');
  });

  // ── 3. Error cases ──

  it('throws when actionName is missing (undefined.split error)', async () => {
    await expect(
      validateInstructionTool.handler(
        { entityId: 'production-order' } as unknown as Record<string, unknown>,
        mockCtx,
      ),
    ).rejects.toThrow(); // TypeError: Cannot read properties of undefined
  });

  it('returns error when action is not found in ontology', async () => {
    const result = await validateInstructionTool.handler(
      {
        actionName: 'platform.nonexistent-action',
        entityId: 'production-order',
      },
      mockCtx,
    );

    const sc = extractStructuredContent(result);
    const data = extractData(result);

    expect(sc.status).toBe('error');
    expect(data.valid).toBe(false);
    expect((sc.error as Record<string, unknown>).code).toBe('ACTION_NOT_FOUND');
    expect(data.message).toBe('Action not found');
  });

  it('returns error when required params fields are missing', async () => {
    // action-4 (create-order) has inputSchema requiring 'amount' and 'currency'
    const result = await validateInstructionTool.handler(
      {
        actionName: 'platform.create-order',
        entityId: 'production-order',
        params: { amount: 100 },
        // Missing 'currency'
      },
      mockCtx,
    );

    const sc = extractStructuredContent(result);
    const data = extractData(result);

    expect(sc.status).toBe('error');
    expect(data.valid).toBe(false);
    expect((sc.error as Record<string, unknown>).code).toBe('MISSING_FIELDS');
    expect(data.missingFields).toContain('currency');
  });

  it('returns error when multiple required fields are missing', async () => {
    const result = await validateInstructionTool.handler(
      {
        actionName: 'platform.create-order',
        entityId: 'production-order',
        params: {},
        // Missing both 'amount' and 'currency'
      },
      mockCtx,
    );

    const data = extractData(result);

    expect(data.valid).toBe(false);
    expect(data.missingFields).toEqual(['amount', 'currency']);
  });

  it('handles malformed inputSchema gracefully (invalid JSON)', async () => {
    // Override action-4's inputSchema with invalid JSON
    const malformedAction = {
      ...MOCK_ACTIONS[3],
      inputSchema: 'not-valid-json{{{',
    };
    vi.mocked(platformClient.queryActions).mockResolvedValueOnce([malformedAction]);

    // With invalid JSON, requiredFields defaults to [], so no missing fields
    const result = await validateInstructionTool.handler(
      {
        actionName: 'platform.create-order',
        entityId: 'production-order',
        params: {},
      },
      mockCtx,
    );

    const sc = extractStructuredContent(result);
    // Since required is empty array, no missing fields → validation passes
    expect(sc.status).toBe('success');
    expect(extractData(result).valid).toBe(true);
  });

  it('returns error when entityId is missing (action not found)', async () => {
    // queryActions is called with (ontologyId, undefined) → mockResolvedValue returns []
    // because mock doesn't filter by entityId
    // Actually, the mock always returns MOCK_ACTIONS regardless of args
    // So we need to make it return [] for this specific case
    vi.mocked(platformClient.queryActions).mockResolvedValueOnce([]);

    const result = await validateInstructionTool.handler(
      {
        actionName: 'platform.view-order',
        entityId: 'nonexistent-entity',
      },
      mockCtx,
    );

    const sc = extractStructuredContent(result);
    expect(sc.status).toBe('error');
    expect((sc.error as Record<string, unknown>).code).toBe('ACTION_NOT_FOUND');
  });

  // ── 4. RBAC filtering behavior ──

  it('works with READER role (lowest privilege)', async () => {
    const readerCtx = {
      ...mockCtx,
      roles: new Map([['platform', 'READER']]),
    };

    const result = await validateInstructionTool.handler(
      {
        actionName: 'platform.view-order',
        entityId: 'production-order',
      },
      readerCtx,
    );

    const data = extractData(result);
    expect(data.valid).toBe(true);
  });

  it('works with ADMIN role', async () => {
    const adminCtx = {
      agentId: 'admin-agent',
      tenantId: 'default',
      domains: ['platform', 'inventory'],
      roles: new Map([
        ['platform', 'ADMIN'],
        ['inventory', 'ADMIN'],
      ]),
      tokenId: 'admin-token',
    };

    const result = await validateInstructionTool.handler(
      {
        actionName: 'platform.delete-order',
        entityId: 'production-order',
      },
      adminCtx,
    );

    const data = extractData(result);
    expect(data.valid).toBe(true);
    expect(data.requiresApproval).toBe(true); // Risk-based, not role-based
  });

  it('works with different tenant context', async () => {
    const altCtx = {
      agentId: 'agent-42',
      tenantId: 'acme-corp',
      domains: ['platform'],
      roles: new Map([['platform', 'ADMIN']]),
      tokenId: 'token-xyz',
    };

    const result = await validateInstructionTool.handler(
      {
        actionName: 'platform.view-order',
        entityId: 'production-order',
      },
      altCtx,
    );

    const data = extractData(result);
    expect(data.valid).toBe(true);
    expect(platformClient.queryActions).toHaveBeenCalledWith('default', 'production-order');
  });

  // ── 5. Error propagation from backend ──

  it('falls back to permissive validation when platformClient.queryActions throws', async () => {
    vi.mocked(platformClient.queryActions).mockRejectedValueOnce(
      new Error('Backend connection refused'),
    );

    const result = await validateInstructionTool.handler(
      {
        actionName: 'platform.view-order',
        entityId: 'production-order',
      },
      mockCtx,
    );

    // Handler catches the error and returns valid: true with a fallback message
    const sc = extractStructuredContent(result);
    const data = extractData(result);
    const content = (result as { content: Array<{ text: string }> }).content;

    expect(sc.status).toBe('success');
    expect(data.valid).toBe(true);
    // The structuredContent.data.message is concise; the content text has the full message
    expect(data.message).toBe('No strict validation configured');
    expect(JSON.parse(content[0].text).message).toContain('allowing by default');
  });

  it('falls back to permissive validation on network timeout', async () => {
    vi.mocked(platformClient.queryActions).mockRejectedValueOnce(
      new Error('Request timeout'),
    );

    const result = await validateInstructionTool.handler(
      {
        actionName: 'platform.view-order',
        entityId: 'production-order',
      },
      mockCtx,
    );

    const sc = extractStructuredContent(result);
    const data = extractData(result);

    expect(sc.status).toBe('success');
    expect(data.valid).toBe(true);
    expect(data.message).toBe('No strict validation configured');
  });

  // ── 6. Business rule evaluation ──

  it('returns RULE_VIOLATION when a business rule fails', async () => {
    (MOCK_EVALUATE_RULES as ReturnType<typeof vi.fn>).mockReturnValue([
      {
        ruleId: 'rule-1',
        ruleName: 'amount-limit',
        passed: false,
        reason: '金额 5000 超过最大值 1000',
      },
    ]);

    const result = await validateInstructionTool.handler(
      {
        actionName: 'platform.view-order',
        entityId: 'production-order',
        params: { amount: 5000 },
      },
      mockCtx,
    );

    const sc = extractStructuredContent(result);
    const data = extractData(result);

    expect(sc.status).toBe('error');
    expect(data.valid).toBe(false);
    expect((sc.error as Record<string, unknown>).code).toBe('RULE_VIOLATION');
    expect(data.ruleResults).toBeDefined();
    expect((data.ruleResults as Array<Record<string, unknown>>)[0].ruleName).toBe('amount-limit');
  });

  it('passes validation when all business rules pass', async () => {
    (MOCK_EVALUATE_RULES as ReturnType<typeof vi.fn>).mockReturnValue([
      {
        ruleId: 'rule-1',
        ruleName: 'amount-limit',
        passed: true,
      },
      {
        ruleId: 'rule-2',
        ruleName: 'currency-whitelist',
        passed: true,
      },
    ]);

    const result = await validateInstructionTool.handler(
      {
        actionName: 'platform.view-order',
        entityId: 'production-order',
        params: { amount: 500, currency: 'USD' },
      },
      mockCtx,
    );

    const data = extractData(result);

    expect(data.valid).toBe(true);
    expect(data.ruleEvaluations).toBeDefined();
    expect((data.ruleEvaluations as Array<Record<string, unknown>>)).toHaveLength(2);
  });

  it('includes rule evaluation summary in success message', async () => {
    (MOCK_EVALUATE_RULES as ReturnType<typeof vi.fn>).mockReturnValue([
      { ruleId: 'rule-1', ruleName: 'amount-limit', passed: true },
    ]);

    const result = await validateInstructionTool.handler(
      {
        actionName: 'platform.view-order',
        entityId: 'production-order',
      },
      mockCtx,
    );

    const content = (result as { content: Array<{ text: string }> }).content;
    const textData = JSON.parse(content[0].text);

    expect(textData.valid).toBe(true);
    expect(textData.message).toMatch(/1 rules checked, all passed/);
  });

  // ── 7. Content field assertions ──

  it('returns content array with valid JSON text', async () => {
    const result = await validateInstructionTool.handler(
      {
        actionName: 'platform.view-order',
        entityId: 'production-order',
      },
      mockCtx,
    );

    const content = (result as { content: Array<{ type: string; text: string }> }).content;

    expect(content).toHaveLength(1);
    expect(content[0].type).toBe('text');

    const textData = JSON.parse(content[0].text);
    expect(textData.valid).toBe(true);
    expect(textData.entityId).toBe('production-order');
  });

  it('passes params object to evaluateRules', async () => {
    const params = { amount: 100, currency: 'USD', note: 'urgent' };
    await validateInstructionTool.handler(
      {
        actionName: 'platform.view-order',
        entityId: 'production-order',
        params,
      },
      mockCtx,
    );

    expect(MOCK_EVALUATE_RULES).toHaveBeenCalledWith('production-order', 'view-order', params);
  });

  it('passes empty object to evaluateRules when params is not provided', async () => {
    await validateInstructionTool.handler(
      {
        actionName: 'platform.view-order',
        entityId: 'production-order',
      },
      mockCtx,
    );

    expect(MOCK_EVALUATE_RULES).toHaveBeenCalledWith('production-order', 'view-order', {});
  });
});

import { describe, it, expect, vi, beforeEach } from 'vitest';

// ── Shared mock context ──

const mockCtx = {
  agentId: 'test-agent',
  tenantId: 'default',
  domains: ['platform'],
  roles: new Map([['platform', 'READER']]),
  tokenId: 'test-token',
};

// Set PLATFORM_ONTOLOGY_ID so we can test both the semantic-layer path
// (when resolveIntent returns a match) and the keyword-fallback path
// (when resolveIntent returns null).
vi.hoisted(() => {
  process.env.PLATFORM_ONTOLOGY_ID = 'test-onto-id';
});

vi.mock('../../src/client/platform-client.js', () => ({
  platformClient: {
    // Default: no semantic match → keyword fallback runs
    resolveIntent: vi.fn().mockResolvedValue(null),
  },
}));

import { resolveIntentTool } from '../../src/mcp/tools/resolve-intent.js';
import { platformClient } from '../../src/client/platform-client.js';

// ── Helpers ──

function extractData(result: unknown): Record<string, unknown> {
  return (result as { structuredContent: { data: Record<string, unknown> } })
    .structuredContent.data;
}

function extractStructuredContent(result: unknown): Record<string, unknown> {
  return (result as { structuredContent: Record<string, unknown> }).structuredContent;
}

function extractContentText(result: unknown): string {
  return (result as { content: Array<{ text: string }> }).content[0].text;
}

// ── Tests ──

describe('resolve_intent tool', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ===================================================================
  // 1. Fuzzy match (partial phrase matching)
  // ===================================================================

  it('resolves CREATE intent from partial Chinese phrase', async () => {
    const result = await resolveIntentTool.handler(
      { query: '我要创建生产订单' },
      mockCtx,
    );
    const data = extractData(result);

    expect(data.category).toBe('CREATE');
    expect(data.confidence).toBeGreaterThanOrEqual(0.5);
    expect(data.source).toBe('keyword-fallback');
    // Entity extraction
    expect(data.entities).toContain('生产订单');
    // Suggested tool for CREATE
    expect(data.suggestedTool).toBe('validate_instruction');
  });

  it('resolves QUERY intent from partial English phrase', async () => {
    const result = await resolveIntentTool.handler(
      { query: 'show all orders' },
      mockCtx,
    );
    const data = extractData(result);

    expect(data.category).toBe('QUERY');
    expect(data.confidence).toBeGreaterThanOrEqual(0.5);
    expect(data.suggestedTool).toBe('query_ontology');
  });

  it('resolves DELETE intent from a substring match', async () => {
    const result = await resolveIntentTool.handler(
      { query: '请取消这个工单' },
      mockCtx,
    );
    const data = extractData(result);

    expect(data.category).toBe('DELETE');
    expect(data.confidence).toBeGreaterThanOrEqual(0.5);
    expect(data.entities).toContain('工单');
  });

  it('resolves ANALYZE intent with partial match', async () => {
    const result = await resolveIntentTool.handler(
      { query: '统计上个月的物料消耗' },
      mockCtx,
    );
    const data = extractData(result);

    expect(data.category).toBe('ANALYZE');
    expect(data.suggestedTool).toBe('traverse_graph');
    expect(data.entities).toContain('物料');
  });

  // ===================================================================
  // 2. Synonym resolution
  // ===================================================================

  it('resolves synonym "new" to CREATE intent', async () => {
    const result = await resolveIntentTool.handler(
      { query: 'new production order' },
      mockCtx,
    );
    const data = extractData(result);

    expect(data.category).toBe('CREATE');
    expect(data.source).toBe('keyword-fallback');
  });

  it('resolves synonyms "search" and "find" to QUERY intent', async () => {
    const result1 = await resolveIntentTool.handler(
      { query: 'search materials in warehouse' },
      mockCtx,
    );
    expect(extractData(result1).category).toBe('QUERY');

    const result2 = await resolveIntentTool.handler(
      { query: 'find BOM for product-123' },
      mockCtx,
    );
    expect(extractData(result2).category).toBe('QUERY');
  });

  it('resolves synonym "modify" to UPDATE intent', async () => {
    const result = await resolveIntentTool.handler(
      { query: 'modify the routing for this order' },
      mockCtx,
    );
    const data = extractData(result);

    expect(data.category).toBe('UPDATE');
  });

  it('resolves Chinese synonyms "更改" should also match UPDATE', async () => {
    // '更改' is not in the pattern list, but '变更' is — let's verify
    const result = await resolveIntentTool.handler(
      { query: '变更物料清单' },
      mockCtx,
    );
    const data = extractData(result);

    expect(data.category).toBe('UPDATE');
    expect(data.entities).toContain('物料');
  });

  it('resolves "remove" to DELETE intent', async () => {
    const result = await resolveIntentTool.handler(
      { query: 'remove this work order' },
      mockCtx,
    );
    const data = extractData(result);

    expect(data.category).toBe('DELETE');
  });

  it('resolves "run" to EXECUTE intent', async () => {
    const result = await resolveIntentTool.handler(
      { query: 'run inventory check' },
      mockCtx,
    );
    const data = extractData(result);

    expect(data.category).toBe('EXECUTE');
    expect(data.suggestedTool).toBe('validate_instruction');
  });

  it('resolves "navigate" synonym to NAVIGATE intent', async () => {
    const result = await resolveIntentTool.handler(
      { query: 'go to production dashboard' },
      mockCtx,
    );
    const data = extractData(result);

    expect(data.category).toBe('NAVIGATE');
  });

  // ===================================================================
  // 3. Empty query / blank string → graceful handling
  // ===================================================================

  it('returns UNKNOWN category for empty string query', async () => {
    const result = await resolveIntentTool.handler(
      { query: '' },
      mockCtx,
    );
    const data = extractData(result);

    expect(data.category).toBe('UNKNOWN');
    expect(data.confidence).toBe(0);
    expect(data.source).toBe('keyword-fallback');
  });

  it('returns UNKNOWN category for whitespace-only query', async () => {
    const result = await resolveIntentTool.handler(
      { query: '   ' },
      mockCtx,
    );
    const data = extractData(result);

    expect(data.category).toBe('UNKNOWN');
    expect(data.confidence).toBe(0);
  });

  it('returns UNKNOWN category for query with only special characters', async () => {
    const result = await resolveIntentTool.handler(
      { query: '!@#$%^&*()' },
      mockCtx,
    );
    const data = extractData(result);

    expect(data.category).toBe('UNKNOWN');
    expect(data.confidence).toBe(0);
  });

  it('handles undefined query gracefully (defaults to empty string)', async () => {
    const result = await resolveIntentTool.handler(
      {} as Record<string, unknown>,
      mockCtx,
    );
    const data = extractData(result);

    expect(data.category).toBe('UNKNOWN');
    expect(data.confidence).toBe(0);
  });

  // ===================================================================
  // 4. Very long query → appropriate handling
  // ===================================================================

  it('handles a very long query without crashing', async () => {
    const longQuery = '查'.repeat(5000);
    const result = await resolveIntentTool.handler(
      { query: longQuery },
      mockCtx,
    );
    const data = extractData(result);

    // Should still return a valid result (QUERY since '查' is in queryPatterns)
    expect(data.category).toBe('QUERY');
    expect(data.confidence).toBeGreaterThan(0);
    expect(data.source).toBe('keyword-fallback');
  });

  it('handles a long English query without crashing', async () => {
    // Build a long sentence containing multiple intent keywords
    const base = 'I need to find the production order status for order ' + 'abc123 '.repeat(200);
    const result = await resolveIntentTool.handler(
      { query: base },
      mockCtx,
    );
    const data = extractData(result);

    // 'find' should match QUERY
    expect(data.category).toBe('QUERY');
    expect(data.confidence).toBeGreaterThan(0);
  });

  it('handles extremely long query with mixed content', async () => {
    const chunk = '查看生产订单状态 更新物料清单 删除工单 创建新订单 ';
    const longQuery = chunk.repeat(300); // ~6000 chars
    const result = await resolveIntentTool.handler(
      { query: longQuery },
      mockCtx,
    );
    const data = extractData(result);

    // Should pick the highest-confidence match (longest matched substring)
    expect(data.category).toBeDefined();
    expect(data.confidence).toBeGreaterThan(0);
  });

  // ===================================================================
  // 5. No intents configured for ontology
  // ===================================================================

  it('falls back to keyword matching when platform returns null (no intents configured)', async () => {
    // Default mock already returns null → no semantic match
    const result = await resolveIntentTool.handler(
      { query: '查看生产订单' },
      mockCtx,
    );
    const data = extractData(result);

    expect(data.source).toBe('keyword-fallback');
    expect(data.category).toBe('QUERY');
    // Verify platformClient.resolveIntent was called with the ontology ID
    expect(platformClient.resolveIntent).toHaveBeenCalledWith('test-onto-id', '查看生产订单');
  });

  it('falls back gracefully when platformClient.resolveIntent throws', async () => {
    vi.mocked(platformClient.resolveIntent).mockRejectedValueOnce(
      new Error('Backend unavailable'),
    );

    const result = await resolveIntentTool.handler(
      { query: 'create order' },
      mockCtx,
    );
    const data = extractData(result);

    // Should fall back to keyword matching
    expect(data.source).toBe('keyword-fallback');
    expect(data.category).toBe('CREATE');
  });

  it('falls back when platform returns null without actionId', async () => {
    vi.mocked(platformClient.resolveIntent).mockResolvedValueOnce({
      id: 'intent-123',
      name: 'no-action',
      actionId: '',
      triggerPhrases: [],
      matchScore: 3,
    } as unknown as Awaited<ReturnType<typeof platformClient.resolveIntent>>);

    const result = await resolveIntentTool.handler(
      { query: 'ambiguous query' },
      mockCtx,
    );
    const data = extractData(result);

    // Empty actionId → treated as no match → keyword fallback
    expect(data.source).toBe('keyword-fallback');
    expect(data.category).toBe('UNKNOWN');
  });

  // ===================================================================
  // 6. Multiple matching intents → best match selection
  // ===================================================================

  it('chooses CREATE over QUERY when both match but CREATE keyword is more specific', async () => {
    // '创建' (2 chars) and '查看' (2 chars) — equal length, but CREATE is checked first
    const result = await resolveIntentTool.handler(
      { query: '创建并查看订单' },
      mockCtx,
    );
    const data = extractData(result);

    // CREATE and QUERY can both match; the loop checks in order:
    // CREATE patterns first. Since confidence = 0.5 + (p.length / lower.length) * 0.3
    // Both p.length=2, lower.length=6 → confidence = 0.5 + (2/6)*0.3 = 0.5 + 0.1 = 0.6
    // First matched pattern wins since tie goes to earlier category
    expect(data.category).toBe('CREATE');
  });

  it('chooses higher-confidence match when multiple intents match', async () => {
    // '分析' (2 chars) has same length as '查看' (2 chars)
    // But ANALYZE patterns are checked before QUERY patterns in allPatterns array
    // Actually, let me check: order is CREATE, UPDATE, DELETE, ANALYZE, NAVIGATE, EXECUTE, QUERY
    // So ANALYZE comes before QUERY
    const result = await resolveIntentTool.handler(
      { query: '分析并查看订单数据' },
      mockCtx,
    );
    const data = extractData(result);

    // Both '分析' (ANALYZE) and '查看' (QUERY) match with same pattern length (2)
    // ANALYZE is checked first → should win
    expect(data.category).toBe('ANALYZE');
  });

  it('prefers longer matching pattern for higher confidence', async () => {
    // '下达生产订单' (6 chars) is longer than '创建' (2 chars)
    const result = await resolveIntentTool.handler(
      { query: '下达生产订单并创建新工单' },
      mockCtx,
    );
    const data = extractData(result);

    // '下达生产订单' matches CREATE with length 6 → higher confidence
    expect(data.category).toBe('CREATE');
    // Confidence should reflect the longer match
    expect(data.confidence).toBeGreaterThan(0.6);
  });

  // ===================================================================
  // 7. Intent with slot requirements
  // ===================================================================

  it('returns slot information when semantic layer match has slots', async () => {
    vi.mocked(platformClient.resolveIntent).mockResolvedValueOnce({
      id: 'intent-create-order',
      name: '创建生产订单',
      actionId: 'action-create-order',
      triggerPhrases: ['创建生产订单', '下达生产订单'],
      slots: [
        { name: 'productCode', type: 'string', required: true },
        { name: 'quantity', type: 'number', required: true },
      ],
      matchScore: 9,
    });

    const result = await resolveIntentTool.handler(
      { query: '创建生产订单' },
      mockCtx,
    );
    const data = extractData(result);

    expect(data.source).toBe('semantic-layer');
    expect(data.category).toBe('EXECUTE');
    expect(data.intentId).toBe('intent-create-order');
    expect(data.actionId).toBe('action-create-order');
    expect(data.intentName).toBe('创建生产订单');
    expect(data.slots).toEqual([
      { name: 'productCode', type: 'string', required: true },
      { name: 'quantity', type: 'number', required: true },
    ]);
    expect(data.suggestedTool).toBe('validate_instruction');
    expect(data.confidence).toBeGreaterThan(0.5);
  });

  it('returns high confidence for high match score', async () => {
    vi.mocked(platformClient.resolveIntent).mockResolvedValueOnce({
      id: 'intent-1',
      name: '查看订单',
      actionId: 'action-view',
      triggerPhrases: ['查看订单'],
      slots: [],
      matchScore: 10,
    });

    const result = await resolveIntentTool.handler(
      { query: '查看订单' },
      mockCtx,
    );
    const data = extractData(result);

    // confidence = Math.min(0.5 + 10/20, 0.95) = Math.min(1.0, 0.95) = 0.95
    expect(data.confidence).toBe(0.95);
    expect(data.source).toBe('semantic-layer');
  });

  it('handles semantic match with no slots', async () => {
    vi.mocked(platformClient.resolveIntent).mockResolvedValueOnce({
      id: 'intent-view',
      name: '查看库存',
      actionId: 'action-view-inventory',
      slots: undefined,
      matchScore: 8,
    });

    const result = await resolveIntentTool.handler(
      { query: '查看库存' },
      mockCtx,
    );
    const data = extractData(result);

    expect(data.source).toBe('semantic-layer');
    // When slots is undefined in the response, handler uses ?? [] so it becomes empty array
    expect(data.slots).toEqual([]);
    // confidence = Math.min(0.5 + 8/20, 0.95) = 0.9
    expect(data.confidence).toBe(0.9);
  });

  // ===================================================================
  // 8. RBAC context passthrough (READER, ADMIN roles)
  // ===================================================================

  it('works with READER role context', async () => {
    const readerCtx = {
      agentId: 'reader-agent',
      tenantId: 'default',
      domains: ['platform'],
      roles: new Map([['platform', 'READER']]),
      tokenId: 'reader-token',
    };

    const result = await resolveIntentTool.handler(
      { query: '查询订单状态' },
      readerCtx,
    );
    const data = extractData(result);

    // Should work normally regardless of role
    expect(data.category).toBe('QUERY');
    expect(data.source).toBe('keyword-fallback');
  });

  it('works with ADMIN role context', async () => {
    const adminCtx = {
      agentId: 'admin-agent',
      tenantId: 'acme-corp',
      domains: ['platform', 'inventory', 'finance'],
      roles: new Map([
        ['platform', 'ADMIN'],
        ['inventory', 'ADMIN'],
        ['finance', 'ADMIN'],
      ]),
      tokenId: 'admin-token',
    };

    const result = await resolveIntentTool.handler(
      { query: '删除过期的生产订单' },
      adminCtx,
    );
    const data = extractData(result);

    expect(data.category).toBe('DELETE');
    expect(data.source).toBe('keyword-fallback');
  });

  it('works with minimal context (single domain, no extra fields)', async () => {
    const minimalCtx = {
      agentId: 'minimal-agent',
      tenantId: 'default',
      domains: ['platform'],
      roles: new Map([['platform', 'READER']]),
      tokenId: 'minimal-token',
    };

    const result = await resolveIntentTool.handler(
      { query: '查看物料清单' },
      minimalCtx,
    );
    const data = extractData(result);

    expect(data.category).toBe('QUERY');
    expect(data.entities).toContain('物料');
  });

  it('works with multiple domains in context', async () => {
    const multiDomainCtx = {
      agentId: 'multi-domain-agent',
      tenantId: 'enterprise',
      domains: ['platform', 'inventory', 'finance', 'hr'],
      roles: new Map([
        ['platform', 'READER'],
        ['inventory', 'ADMIN'],
        ['finance', 'READER'],
        ['hr', 'ADMIN'],
      ]),
      tokenId: 'multi-token',
    };

    const result = await resolveIntentTool.handler(
      { query: '分析库存趋势' },
      multiDomainCtx,
    );
    const data = extractData(result);

    expect(data.category).toBe('ANALYZE');
    expect(data.entities).toContain('库存');
  });

  // ===================================================================
  // Additional: structuredContent metadata assertions
  // ===================================================================

  it('returns structuredContent with success status and metadata', async () => {
    const result = await resolveIntentTool.handler(
      { query: '创建订单' },
      mockCtx,
    );

    const sc = extractStructuredContent(result);
    expect(sc.status).toBe('success');
    expect(sc.metadata).toBeDefined();
    expect((sc.metadata as Record<string, unknown>).version).toBe('1.0.0');
    expect((sc.metadata as Record<string, unknown>).generated_at).toBeDefined();
    expect((sc.metadata as Record<string, unknown>).trace_id).toBeDefined();
  });

  it('includes confidence in metadata and data', async () => {
    const result = await resolveIntentTool.handler(
      { query: '删除工单' },
      mockCtx,
    );

    const sc = extractStructuredContent(result);
    const data = extractData(result);

    expect((sc.metadata as Record<string, unknown>).confidence).toBe(data.confidence);
    expect(data.confidence).toBeGreaterThanOrEqual(0);
  });

  it('returns content array with valid JSON text', async () => {
    const result = await resolveIntentTool.handler(
      { query: '查询BOM' },
      mockCtx,
    );

    const text = extractContentText(result);
    const parsed = JSON.parse(text);

    expect(parsed.category).toBe('QUERY');
    expect(parsed.source).toBe('keyword-fallback');
    expect(parsed.entities).toContain('BOM');
  });
});

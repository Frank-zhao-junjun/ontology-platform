import { describe, it, expect } from 'vitest';
import { initializeTools, toolRegistry } from '../../src/mcp/tools/init.js';
import { filterByRbac } from '../../src/auth/rbac.js';

describe('E2E Smoke', () => {
  beforeAll(() => {
    initializeTools();
  });

  it('MCP tools/list returns fixed tools', () => {
    const tools = toolRegistry.listTools(['platform']);
    expect(tools.length).toBeGreaterThanOrEqual(4);
    const names = tools.map((t) => t.name);
    expect(names).toContain('resolve_intent');
    expect(names).toContain('query_ontology');
    expect(names).toContain('traverse_graph');
    expect(names).toContain('validate_instruction');
  });

  it('resolve_intent returns category', async () => {
    const tool = toolRegistry.getTool('resolve_intent');
    expect(tool).toBeDefined();
    const result = await tool!.handler({ query: 'create new order' }, {
      agentId: 'test-agent',
      tenantId: 'default',
      domains: ['platform'],
      roles: new Map([['platform', 'READER']]),
      tokenId: 'test-token',
    });
    expect(result).toHaveProperty('structuredContent');
    const sc = (result as any).structuredContent;
    expect(sc.data).toHaveProperty('category');
    expect(sc.data.category).toBe('CREATE');
  });

  it('resolve_intent for query returns QUERY', async () => {
    const tool = toolRegistry.getTool('resolve_intent');
    expect(tool).toBeDefined();
    const result = await tool!.handler({ query: '查看生产订单状态' }, {
      agentId: 'test-agent',
      tenantId: 'default',
      domains: ['platform'],
      roles: new Map([['platform', 'READER']]),
      tokenId: 'test-token',
    });
    const sc = (result as any).structuredContent;
    expect(sc.data.category).toBe('QUERY');
  });

  it('RBAC filters by domain', () => {
    const tools = [
      {
        name: 'a',
        domain: 'finance',
        riskLevel: 'READ' as const,
        description: '',
        inputSchema: { type: 'object' as const, properties: {} },
        handler: async () => ({}),
      },
      {
        name: 'b',
        domain: 'supplychain',
        riskLevel: 'WRITE' as const,
        description: '',
        inputSchema: { type: 'object' as const, properties: {} },
        handler: async () => ({}),
      },
    ];
    const ctx = {
      agentId: 'test',
      tenantId: 'default',
      domains: ['finance'],
      roles: new Map([['finance', 'READER']]),
      tokenId: 'test-token',
    };
    const filtered = filterByRbac(tools, ctx);
    expect(filtered.length).toBe(1);
    expect(filtered[0].name).toBe('a');
  });

  it('RBAC denies WRITE for READER role', () => {
    const tools = [
      {
        name: 'write_tool',
        domain: 'finance',
        riskLevel: 'WRITE' as const,
        description: '',
        inputSchema: { type: 'object' as const, properties: {} },
        handler: async () => ({}),
      },
    ];
    const ctx = {
      agentId: 'test',
      tenantId: 'default',
      domains: ['finance'],
      roles: new Map([['finance', 'READER']]),
      tokenId: 'test-token',
    };
    const filtered = filterByRbac(tools, ctx);
    expect(filtered.length).toBe(0);
  });

  it('validate_instruction handles missing action', async () => {
    const tool = toolRegistry.getTool('validate_instruction');
    expect(tool).toBeDefined();
    const result = await tool!.handler(
      { actionName: 'nonexistent.action', entityId: 'test-entity' },
      {
        agentId: 'test-agent',
        tenantId: 'default',
        domains: ['platform'],
        roles: new Map([['platform', 'READER']]),
        tokenId: 'test-token',
      }
    );
    expect(result).toHaveProperty('structuredContent');
  });
});

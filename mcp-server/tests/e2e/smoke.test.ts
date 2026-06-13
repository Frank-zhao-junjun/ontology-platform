import { describe, it, expect } from 'vitest';

describe('E2E Smoke', () => {
  it('MCP tools/list returns fixed tools', async () => {
    // Import registry to test tools are registered
    const { toolRegistry } = await import('../../src/mcp/tools/registry.js');
    await import('../../src/mcp/tools/init.js');
    const tools = toolRegistry.listTools(['platform']);
    expect(tools.length).toBeGreaterThanOrEqual(4);
    expect(tools.map(t => t.name)).toContain('resolve_intent');
    expect(tools.map(t => t.name)).toContain('query_ontology');
    expect(tools.map(t => t.name)).toContain('traverse_graph');
    expect(tools.map(t => t.name)).toContain('validate_instruction');
  });

  it('resolve_intent returns category', async () => {
    const { toolRegistry } = await import('../../src/mcp/tools/registry.js');
    await import('../../src/mcp/tools/init.js');
    const tool = toolRegistry.getTool('resolve_intent');
    expect(tool).toBeDefined();
    const result = await tool!.handler({ query: 'create new order' }, {} as any);
    expect(result).toHaveProperty('category');
  });

  it('RBAC filters by domain', () => {
    const { filterByRbac } = require('../../src/auth/rbac.js');
    const tools = [
      { name: 'a', domain: 'finance', riskLevel: 'READ', description: '', inputSchema: { type: 'object', properties: {} } },
      { name: 'b', domain: 'supplychain', riskLevel: 'WRITE', description: '', inputSchema: { type: 'object', properties: {} } }
    ] as any;
    const ctx = { domains: ['finance'], roles: new Map([['finance', 'READER']]) };
    const filtered = filterByRbac(tools, ctx);
    expect(filtered.length).toBe(1);
    expect(filtered[0].name).toBe('a');
  });
});

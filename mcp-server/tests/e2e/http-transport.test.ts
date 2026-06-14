// =============================================
// MCP E2E — HTTP-level integration tests
// Starts Express, sends real JSON-RPC 2.0 requests
// =============================================

import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { createServer } from 'http';
import type { AddressInfo } from 'net';
import jwt from 'jsonwebtoken';
import app from '../../src/index.js';

let baseUrl: string;
let server: ReturnType<typeof createServer>;
let testToken: string;

const DEV_SECRET = 'dev-secret-key-change-in-production';

beforeAll(() => {
  // Sign a dev test token matching the auth middleware's expectations
  testToken = jwt.sign(
    {
      agentId: 'test-agent',
      tenantId: 'default',
      domains: ['platform'],
      roles: { platform: 'ADMIN' },
      tokenId: 'test-token',
    },
    DEV_SECRET,
    { algorithm: 'HS256', expiresIn: '1h' }
  );

  server = createServer(app);
  return new Promise<void>((resolve) => {
    server.listen(0, () => {
      const addr = server.address() as AddressInfo;
      baseUrl = `http://localhost:${addr.port}`;
      resolve();
    });
  });
});

afterAll(() => {
  return new Promise<void>((resolve) => {
    server.close(() => resolve());
  });
});

async function mcpRequest(method: string, params?: unknown, id = 1, token?: string) {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (token !== undefined) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  const res = await fetch(`${baseUrl}/mcp`, {
    method: 'POST',
    headers,
    body: JSON.stringify({
      jsonrpc: '2.0',
      id,
      method,
      params,
    }),
  });
  return {
    status: res.status,
    body: (await res.json()) as {
      jsonrpc: string;
      id: number;
      result?: unknown;
      error?: { code: number; message: string };
    },
  };
}

describe('MCP E2E — HTTP Transport', () => {
  it('GET /health returns ok', async () => {
    const res = await fetch(`${baseUrl}/health`);
    const body = await res.json();
    expect(res.status).toBe(200);
    expect(body.status).toBe('ok');
  });

  it('POST /mcp without auth returns 401', async () => {
    const { status, body } = await mcpRequest('tools/list', undefined, 1, undefined);
    expect(status).toBe(401);
    expect(body.error).toBeDefined();
  });

  it('POST /mcp without JSON-RPC returns error (authenticated)', async () => {
    const res = await fetch(`${baseUrl}/mcp`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${testToken}`,
      },
      body: JSON.stringify({ notJsonRpc: true }),
    });
    expect(res.status).toBe(400);
    const body = await res.json();
    expect(body.error.code).toBe(-32600);
  });

  it('tools/list returns tool schemas', async () => {
    const { status, body } = await mcpRequest('tools/list', undefined, 1, testToken);
    expect(status).toBe(200);
    expect(body.result).toBeDefined();
    const tools = (body.result as { tools: unknown[] }).tools;
    expect(tools.length).toBeGreaterThanOrEqual(4);
    const names = tools.map((t: { name: string }) => t.name);
    expect(names).toContain('resolve_intent');
    expect(names).toContain('query_ontology');
  });

  it('initialize returns capabilities', async () => {
    const { status, body } = await mcpRequest('initialize', undefined, 1, testToken);
    expect(status).toBe(200);
    expect(body.result).toBeDefined();
    const r = body.result as { protocolVersion: string; capabilities: { tools: unknown } };
    expect(r.protocolVersion).toBe('2024-11-05');
    expect(r.capabilities.tools).toBeDefined();
  });

  it('tools/call resolve_intent returns QUERY', async () => {
    const { status, body } = await mcpRequest(
      'tools/call',
      { name: 'resolve_intent', arguments: { query: '查看生产订单状态' } },
      1,
      testToken
    );
    expect(status).toBe(200);
    expect(body.result).toBeDefined();
    const r = body.result as { structuredContent: { data: { category: string } } };
    expect(r.structuredContent.data.category).toBe('QUERY');
  });

  it('tools/call with unknown tool returns error', async () => {
    const { status, body } = await mcpRequest(
      'tools/call',
      { name: 'nonexistent_tool', arguments: {} },
      1,
      testToken
    );
    expect(status).toBe(200);
    expect(body.error).toBeDefined();
    expect(body.error!.code).toBe(-32601);
  });

  it('tools/call without name returns error', async () => {
    const { status, body } = await mcpRequest(
      'tools/call',
      { arguments: {} },
      1,
      testToken
    );
    expect(status).toBe(200);
    expect(body.error).toBeDefined();
    expect(body.error!.code).toBe(-32602);
  });

  it('unknown method returns error', async () => {
    const { status, body } = await mcpRequest('bogus_method', undefined, 1, testToken);
    expect(status).toBe(200);
    expect(body.error).toBeDefined();
    // server.ts returns -32601 for unknown methods
    expect(body.error!.code >= -32601 && body.error!.code <= -32000).toBe(true);
  });

  it('tools/list with RBAC filters by domain', async () => {
    // Token with only 'finance' domain should not see 'platform' tools
    const financeToken = jwt.sign(
      {
        agentId: 'finance-agent',
        tenantId: 'default',
        domains: ['finance'],
        roles: { finance: 'READER' },
        tokenId: 'finance-token',
      },
      DEV_SECRET,
      { algorithm: 'HS256', expiresIn: '1h' }
    );
    const { status, body } = await mcpRequest('tools/list', undefined, 1, financeToken);
    expect(status).toBe(200);
    const tools = (body.result as { tools: unknown[] }).tools;
    // finance domain has no registered tools → empty list
    expect(tools.length).toBe(0);
  });
});

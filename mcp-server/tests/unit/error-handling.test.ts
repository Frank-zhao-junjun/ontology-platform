// =============================================
// MCP Error Handling — Backend error propagation
// & JSON-RPC error wrapping unit tests
// =============================================
// Covers TC-TODO Tasks #14 and #17:
//   #14 — MCP↔Backend REST integration error propagation
//   #17 — MCP error wrapping

import { describe, it, expect, beforeAll, afterEach, vi } from 'vitest';
import { handleMcpRequest } from '../../src/mcp/server.js';
import { initializeTools } from '../../src/mcp/tools/init.js';
import { platformClient } from '../../src/client/platform-client.js';

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

const ADMIN_CTX = {
  agentId: 'test-agent',
  tenantId: 'default',
  domains: ['platform'],
  roles: new Map<string, string>([['platform', 'ADMIN']]),
  tokenId: 'test-token',
};

const QUERY_ONTO_ARGS = { ontologyId: 'test-ontology' };

const QUERY_ONTO_CALL_PARAMS = {
  name: 'query_ontology',
  arguments: QUERY_ONTO_ARGS,
};

// ---------------------------------------------------------------------------
// Suite
// ---------------------------------------------------------------------------

describe('MCP Error Handling — Backend Error Propagation', () => {
  beforeAll(() => {
    initializeTools();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  // --------------------------------------------------
  // #14 — MCP↔Backend REST integration error propagation
  // --------------------------------------------------

  it('wraps backend 500 as JSON-RPC error -32000 with status in message', async () => {
    vi.spyOn(platformClient, 'queryActions').mockRejectedValue(
      new Error('Platform API error 500: Internal Server Error')
    );

    const response = await handleMcpRequest(
      'tools/call',
      QUERY_ONTO_CALL_PARAMS,
      'req-1',
      ADMIN_CTX,
    );

    expect(response.jsonrpc).toBe('2.0');
    expect(response.id).toBe('req-1');
    expect(response.result).toBeUndefined();
    expect(response.error).toBeDefined();
    expect(response.error!.code).toBe(-32000);
    expect(response.error!.message).toContain('500');
    expect(response.error!.message).toContain('Platform API error');
  });

  it('propagates backend 404 error through -32000 wrapper', async () => {
    vi.spyOn(platformClient, 'queryActions').mockRejectedValue(
      new Error('Platform API error 404: {"code":404,"message":"Ontology not found"}')
    );

    const response = await handleMcpRequest(
      'tools/call',
      { name: 'query_ontology', arguments: { ontologyId: 'missing-ontology' } },
      'req-2',
      ADMIN_CTX,
    );

    expect(response.error).toBeDefined();
    expect(response.error!.code).toBe(-32000);
    expect(response.error!.message).toContain('404');
    expect(response.error!.message).toContain('not found');
  });

  it('wraps backend 503 service unavailable as -32000', async () => {
    vi.spyOn(platformClient, 'queryActions').mockRejectedValue(
      new Error('Platform API error 503: Service Unavailable')
    );

    const response = await handleMcpRequest(
      'tools/call',
      QUERY_ONTO_CALL_PARAMS,
      'req-3',
      ADMIN_CTX,
    );

    expect(response.error).toBeDefined();
    expect(response.error!.code).toBe(-32000);
    expect(response.error!.message).toContain('503');
  });

  it('wraps backend 401 unauthorized as -32000', async () => {
    vi.spyOn(platformClient, 'queryActions').mockRejectedValue(
      new Error('Platform API error 401: Unauthorized')
    );

    const response = await handleMcpRequest(
      'tools/call',
      QUERY_ONTO_CALL_PARAMS,
      'req-4',
      ADMIN_CTX,
    );

    expect(response.error).toBeDefined();
    expect(response.error!.code).toBe(-32000);
    expect(response.error!.message).toContain('401');
  });

  it('wraps backend 403 forbidden as -32000', async () => {
    vi.spyOn(platformClient, 'queryActions').mockRejectedValue(
      new Error('Platform API error 403: Forbidden')
    );

    const response = await handleMcpRequest(
      'tools/call',
      QUERY_ONTO_CALL_PARAMS,
      'req-5',
      ADMIN_CTX,
    );

    expect(response.error).toBeDefined();
    expect(response.error!.code).toBe(-32000);
    expect(response.error!.message).toContain('403');
  });

  // --------------------------------------------------
  // #17 — MCP error wrapping (edge cases)
  // --------------------------------------------------

  it('wraps network timeout (AbortError) as -32000', async () => {
    vi.spyOn(platformClient, 'queryActions').mockRejectedValue(
      new DOMException('The operation was aborted', 'AbortError'),
    );

    const response = await handleMcpRequest(
      'tools/call',
      QUERY_ONTO_CALL_PARAMS,
      'req-6',
      ADMIN_CTX,
    );

    expect(response.error).toBeDefined();
    expect(response.error!.code).toBe(-32000);
    expect(response.error!.message).toContain('AbortError');
  });

  it('wraps malformed JSON response as -32000', async () => {
    vi.spyOn(platformClient, 'queryActions').mockRejectedValue(
      new SyntaxError('Unexpected token < in JSON at position 0'),
    );

    const response = await handleMcpRequest(
      'tools/call',
      QUERY_ONTO_CALL_PARAMS,
      'req-7',
      ADMIN_CTX,
    );

    expect(response.error).toBeDefined();
    expect(response.error!.code).toBe(-32000);
    expect(response.error!.message).toContain('Unexpected token');
  });

  it('propagates generic Error from PlatformClient as -32000', async () => {
    vi.spyOn(platformClient, 'queryActions').mockRejectedValue(
      new Error('Connection refused'),
    );

    const response = await handleMcpRequest(
      'tools/call',
      QUERY_ONTO_CALL_PARAMS,
      'req-8',
      ADMIN_CTX,
    );

    expect(response.error).toBeDefined();
    expect(response.error!.code).toBe(-32000);
    expect(response.error!.message).toContain('Connection refused');
  });

  it('wraps non-ok backend response with empty body as -32000', async () => {
    vi.spyOn(platformClient, 'queryActions').mockRejectedValue(
      new Error('Platform API error 502: '),
    );

    const response = await handleMcpRequest(
      'tools/call',
      QUERY_ONTO_CALL_PARAMS,
      'req-9',
      ADMIN_CTX,
    );

    expect(response.error).toBeDefined();
    expect(response.error!.code).toBe(-32000);
    expect(response.error!.message).toContain('502');
  });

  it('wraps network-level TypeError as -32000', async () => {
    vi.spyOn(platformClient, 'queryActions').mockRejectedValue(
      new TypeError('fetch failed'),
    );

    const response = await handleMcpRequest(
      'tools/call',
      QUERY_ONTO_CALL_PARAMS,
      'req-10',
      ADMIN_CTX,
    );

    expect(response.error).toBeDefined();
    expect(response.error!.code).toBe(-32000);
    expect(response.error!.message).toContain('fetch failed');
  });

  // --------------------------------------------------
  // JSON-RPC protocol-level errors
  // --------------------------------------------------

  it('returns -32602 when tool name is missing from tools/call', async () => {
    const response = await handleMcpRequest(
      'tools/call',
      { arguments: { ontologyId: 'test' } },
      'req-11',
      ADMIN_CTX,
    );

    expect(response.error).toBeDefined();
    expect(response.error!.code).toBe(-32602);
    expect(response.error!.message).toContain('Missing tool name');
    expect(response.result).toBeUndefined();
  });

  it('returns -32601 for an unknown tool name', async () => {
    const response = await handleMcpRequest(
      'tools/call',
      { name: 'nonexistent_tool_v99', arguments: {} },
      'req-12',
      ADMIN_CTX,
    );

    expect(response.error).toBeDefined();
    expect(response.error!.code).toBe(-32601);
    expect(response.error!.message).toContain('not found');
    expect(response.error!.message).toContain('nonexistent_tool_v99');
  });

  it('returns -32601 for an unrecognized method', async () => {
    const response = await handleMcpRequest(
      'tools/unknown',
      {},
      'req-13',
      ADMIN_CTX,
    );

    expect(response.error).toBeDefined();
    expect(response.error!.code).toBe(-32601);
    expect(response.error!.message).toContain('tools/unknown');
  });

  it('preserves JSON-RPC structure in every error response', async () => {
    vi.spyOn(platformClient, 'queryActions').mockRejectedValue(
      new Error('Platform API error 500: crash'),
    );

    const response = await handleMcpRequest(
      'tools/call',
      QUERY_ONTO_CALL_PARAMS,
      'req-14',
      ADMIN_CTX,
    );

    // Every MCP response must conform to JSON-RPC 2.0
    expect(response).toHaveProperty('jsonrpc', '2.0');
    expect(response).toHaveProperty('id', 'req-14');
    expect(response).toHaveProperty('error');
    expect(response.error).toHaveProperty('code');
    expect(response.error).toHaveProperty('message');
    expect(Object.keys(response)).toEqual(['jsonrpc', 'id', 'error']);
  });

  it('does NOT leak internal stack traces to the error message', async () => {
    vi.spyOn(platformClient, 'queryActions').mockRejectedValue(
      new Error('Platform API error 500: Internal Server Error'),
    );

    const response = await handleMcpRequest(
      'tools/call',
      QUERY_ONTO_CALL_PARAMS,
      'req-15',
      ADMIN_CTX,
    );

    expect(response.error).toBeDefined();
    // Message should be a simple string — not an object with stack info
    expect(typeof response.error!.message).toBe('string');
    // Stack traces typically contain backslash or "at " — these should NOT appear
    expect(response.error!.message).not.toMatch(/\\n\s+at /);
  });
});

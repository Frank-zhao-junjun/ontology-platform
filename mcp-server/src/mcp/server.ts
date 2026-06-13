// =============================================
// MCP Server Instance — JSON-RPC 2.0 handler
// =============================================

import type { AgentContext } from '../types/index.js';
import { toolRegistry } from './tools/registry.js';
import { initializeTools } from './tools/init.js';
import { applyRbac } from '../auth/rbac.js';

// Initialize tools on first import
let initialized = false;
function ensureInitialized(): void {
  if (!initialized) {
    initializeTools();
    initialized = true;
  }
}

export interface McpResponse {
  jsonrpc: '2.0';
  id: string | number | null;
  result?: unknown;
  error?: {
    code: number;
    message: string;
    data?: unknown;
  };
}

export async function handleMcpRequest(
  method: string,
  params: unknown,
  id: string | number,
  agentContext?: AgentContext
): Promise<McpResponse> {
  ensureInitialized();

  switch (method) {
    case 'tools/list': {
      const ctx = agentContext;
      let tools = toolRegistry.listTools();
      if (ctx) {
        tools = applyRbac(tools, ctx);
      }
      const toolSchemas = tools.map((t) => ({
        name: t.name,
        description: t.description,
        inputSchema: t.inputSchema,
      }));
      return {
        jsonrpc: '2.0',
        id,
        result: { tools: toolSchemas },
      };
    }

    case 'tools/call': {
      const callParams = params as {
        name?: string;
        arguments?: Record<string, unknown>;
      };
      if (!callParams?.name) {
        return {
          jsonrpc: '2.0',
          id,
          error: { code: -32602, message: 'Missing tool name' },
        };
      }

      const ctx = agentContext || {
        agentId: 'anonymous',
        tenantId: 'default',
        domains: ['platform'],
        roles: new Map([['platform', 'READER']]),
        tokenId: 'anonymous',
      };

      // RBAC gate for tools/call
      const visibleTools = applyRbac(toolRegistry.listTools(), ctx);
      const tool = visibleTools.find((t) => t.name === callParams.name);

      if (!tool) {
        return {
          jsonrpc: '2.0',
          id,
          error: {
            code: -32601,
            message: `Tool "${callParams.name}" not found or not authorized`,
          },
        };
      }

      try {
        const result = await tool.handler(
          callParams.arguments || {},
          ctx
        );
        return {
          jsonrpc: '2.0',
          id,
          result: result as unknown,
        };
      } catch (err) {
        console.error(`[mcp] Tool "${callParams.name}" error:`, err);
        return {
          jsonrpc: '2.0',
          id,
          error: {
            code: -32000,
            message: `Tool execution error: ${String(err)}`,
          },
        };
      }
    }

    case 'initialize': {
      return {
        jsonrpc: '2.0',
        id,
        result: {
          protocolVersion: '2024-11-05',
          capabilities: { tools: {} },
          serverInfo: {
            name: 'ontology-platform-mcp',
            version: '1.0.0',
          },
        },
      };
    }

    case 'notifications/initialized': {
      return {
        jsonrpc: '2.0',
        id,
        result: {},
      };
    }

    default:
      return {
        jsonrpc: '2.0',
        id,
        error: { code: -32601, message: `Method "${method}" not found` },
      };
  }
}

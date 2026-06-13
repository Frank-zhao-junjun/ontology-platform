// =============================================
// MCP Server Entry Point — Express + Streamable HTTP
// =============================================

import 'dotenv/config';
import express from 'express';
import cors from 'cors';
import { authMiddleware, extractAgentContext } from './auth/middleware.js';
import { handleMcpRequest } from './mcp/server.js';

const PORT = parseInt(process.env.MCP_PORT || '3001', 10);
const app = express();

// Middleware
app.use(cors());
app.use(express.json({ limit: '1mb' }));

// Health check
app.get('/health', (_req, res) => {
  res.json({ status: 'ok', service: 'ontology-mcp-server', version: '1.0.0' });
});

// MCP endpoint (Streamable HTTP)
app.post('/mcp', authMiddleware, async (req, res) => {
  const body = req.body;

  // Validate JSON-RPC 2.0
  if (!body || body.jsonrpc !== '2.0' || !body.method) {
    res.status(400).json({
      jsonrpc: '2.0',
      id: body?.id ?? null,
      error: { code: -32600, message: 'Invalid Request — must be JSON-RPC 2.0' },
    });
    return;
  }

  const agentContext = extractAgentContext(req);

  try {
    const response = await handleMcpRequest(
      body.method,
      body.params,
      body.id ?? null,
      agentContext
    );
    res.json(response);
  } catch (err) {
    console.error('[mcp] Unhandled error:', err);
    res.status(500).json({
      jsonrpc: '2.0',
      id: body.id ?? null,
      error: { code: -32603, message: 'Internal error' },
    });
  }
});

// Start server
app.listen(PORT, () => {
  console.log(`[mcp-server] Ontology MCP Server running on http://localhost:${PORT}`);
  console.log(`[mcp-server] MCP endpoint: POST http://localhost:${PORT}/mcp`);
  console.log(`[mcp-server] Health check: GET http://localhost:${PORT}/health`);
});

export default app;

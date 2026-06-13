// =============================================
// Auth Middleware — JWT verification + AgentContext
// =============================================

import type { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import type { AgentContext, JwtPayload } from '../types/index.js';

const JWT_PUBLIC_KEY = process.env.JWT_PUBLIC_KEY || 'dev-secret-key-change-in-production';

// Extend Express Request to carry AgentContext
declare global {
  namespace Express {
    interface Request {
      agentContext?: AgentContext;
    }
  }
}

export function authMiddleware(req: Request, res: Response, next: NextFunction): void {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    res.status(401).json({
      jsonrpc: '2.0',
      id: null,
      error: { code: -32001, message: 'Missing or invalid Authorization header' },
    });
    return;
  }

  const token = authHeader.slice(7);

  try {
    const payload = jwt.verify(token, JWT_PUBLIC_KEY, { algorithms: ['RS256', 'HS256'] }) as JwtPayload;

    // Check expiry
    if (payload.exp && payload.exp * 1000 < Date.now()) {
      res.status(401).json({
        jsonrpc: '2.0',
        id: null,
        error: { code: -32001, message: 'Token expired' },
      });
      return;
    }

    const agentContext: AgentContext = {
      agentId: payload.agentId,
      tenantId: payload.tenantId,
      domains: payload.domains || [],
      roles: new Map(Object.entries(payload.roles || {})),
      tokenId: payload.tokenId,
    };

    req.agentContext = agentContext;
    next();
  } catch (err) {
    res.status(401).json({
      jsonrpc: '2.0',
      id: null,
      error: { code: -32001, message: 'Invalid token' },
    });
  }
}

export function extractAgentContext(req: Request): AgentContext | undefined {
  return req.agentContext;
}

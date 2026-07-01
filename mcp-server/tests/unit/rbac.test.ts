// =============================================
// RBAC — Role-Based Access Control Unit Tests
// =============================================
// Tests for filterByRbac, filterByDomain, applyRbac, requiresApproval
// Covers: READER, WRITE (OPERATOR), ADMIN, ANALYST, no-role, cross-domain
// =============================================

import { describe, it, expect } from 'vitest';
import {
  filterByRbac,
  filterByDomain,
  applyRbac,
  requiresApproval,
} from '../../src/auth/rbac.js';
import type { ToolDefinition, AgentContext } from '../../src/types/index.js';

// ── Helpers ──

function makeTool(overrides: Partial<ToolDefinition> & { name: string }): ToolDefinition {
  return {
    description: '',
    inputSchema: { type: 'object' as const, properties: {} },
    domain: 'platform',
    riskLevel: 'READ' as const,
    handler: async () => ({}),
    ...overrides,
  };
}

function makeCtx(overrides: Partial<AgentContext> = {}): AgentContext {
  return {
    agentId: 'test-agent',
    tenantId: 'default',
    domains: ['platform'],
    roles: new Map([['platform', 'READER']]),
    tokenId: 'test-token',
    ...overrides,
  };
}

// ── Fixture tools across risk levels and domains ──

const READ_TOOL = makeTool({ name: 'read_tool', riskLevel: 'READ', domain: 'platform' });
const WRITE_TOOL = makeTool({ name: 'write_tool', riskLevel: 'WRITE', domain: 'platform' });
const DELETE_TOOL = makeTool({ name: 'delete_tool', riskLevel: 'DELETE', domain: 'platform' });
const APPROVAL_TOOL = makeTool({ name: 'approval_tool', riskLevel: 'APPROVAL', domain: 'platform' });

const FINANCE_TOOL = makeTool({ name: 'finance_tool', riskLevel: 'READ', domain: 'finance' });
const SUPPLYCHAIN_TOOL = makeTool({ name: 'supplychain_tool', riskLevel: 'WRITE', domain: 'supplychain' });
const HR_TOOL = makeTool({ name: 'hr_tool', riskLevel: 'DELETE', domain: 'hr' });

const ALL_RISK_TOOLS = [READ_TOOL, WRITE_TOOL, DELETE_TOOL, APPROVAL_TOOL];
const CROSS_DOMAIN_TOOLS = [READ_TOOL, FINANCE_TOOL, SUPPLYCHAIN_TOOL, HR_TOOL];

// ====================================================================
// filterByDomain
// ====================================================================

describe('filterByDomain', () => {
  it('keeps tools whose domain is in ctx.domains', () => {
    const ctx = makeCtx({ domains: ['platform', 'finance'] });
    const result = filterByDomain(CROSS_DOMAIN_TOOLS, ctx);
    expect(result.map((t) => t.name)).toEqual(['read_tool', 'finance_tool']);
  });

  it('filters out tools whose domain is not in ctx.domains', () => {
    const ctx = makeCtx({ domains: ['hr'] });
    const result = filterByDomain(CROSS_DOMAIN_TOOLS, ctx);
    expect(result.map((t) => t.name)).toEqual(['hr_tool']);
  });

  it('returns empty when no domains match', () => {
    const ctx = makeCtx({ domains: ['nonexistent'] });
    const result = filterByDomain(CROSS_DOMAIN_TOOLS, ctx);
    expect(result).toHaveLength(0);
  });

  it('returns empty when domains is empty (includes([]) is always false)', () => {
    const ctx = makeCtx({ domains: [] });
    const result = filterByDomain(CROSS_DOMAIN_TOOLS, ctx);
    expect(result).toHaveLength(0);
  });

  it('tools with empty domain string bypass domain filter when ctx.domains includes empty string', () => {
    const tools = [
      makeTool({ name: 'no_domain_tool', riskLevel: 'READ', domain: '' }),
    ];
    const ctx = makeCtx({ domains: ['platform', ''] });
    const result = filterByDomain(tools, ctx);
    expect(result).toHaveLength(1);
    expect(result[0].name).toBe('no_domain_tool');
  });

  it('tools with empty domain string are filtered out when ctx.domains excludes empty string', () => {
    const tools = [
      makeTool({ name: 'no_domain_tool', riskLevel: 'READ', domain: '' }),
    ];
    const ctx = makeCtx({ domains: ['platform'] });
    const result = filterByDomain(tools, ctx);
    expect(result).toHaveLength(0);
  });
});

// ====================================================================
// requiresApproval
// ====================================================================

describe('requiresApproval', () => {
  it('returns true for DELETE risk level', () => {
    expect(requiresApproval('DELETE')).toBe(true);
  });

  it('returns true for APPROVAL risk level', () => {
    expect(requiresApproval('APPROVAL')).toBe(true);
  });

  it('returns false for READ risk level', () => {
    expect(requiresApproval('READ')).toBe(false);
  });

  it('returns false for WRITE risk level', () => {
    expect(requiresApproval('WRITE')).toBe(false);
  });

  it('returns false for unknown risk level', () => {
    expect(requiresApproval('ANALYZE')).toBe(false);
  });
});

// ====================================================================
// filterByRbac — Role-based access
// ====================================================================

describe('filterByRbac — READER role', () => {
  const readerCtx = makeCtx({ roles: new Map([['platform', 'READER']]) });

  it('allows READ tools', () => {
    const result = filterByRbac(ALL_RISK_TOOLS, readerCtx);
    expect(result.map((t) => t.name)).toEqual(['read_tool']);
  });

  it('denies WRITE tools', () => {
    const result = filterByRbac([WRITE_TOOL], readerCtx);
    expect(result).toHaveLength(0);
  });

  it('denies DELETE tools', () => {
    const result = filterByRbac([DELETE_TOOL], readerCtx);
    expect(result).toHaveLength(0);
  });

  it('denies APPROVAL tools', () => {
    const result = filterByRbac([APPROVAL_TOOL], readerCtx);
    expect(result).toHaveLength(0);
  });
});

describe('filterByRbac — ADMIN role', () => {
  const adminCtx = makeCtx({ roles: new Map([['platform', 'ADMIN']]) });

  it('allows all risk levels: READ, WRITE, DELETE, APPROVAL', () => {
    const result = filterByRbac(ALL_RISK_TOOLS, adminCtx);
    expect(result.map((t) => t.name)).toEqual([
      'read_tool',
      'write_tool',
      'delete_tool',
      'approval_tool',
    ]);
  });

  it('allows each individual risk level', () => {
    for (const tool of ALL_RISK_TOOLS) {
      const result = filterByRbac([tool], adminCtx);
      expect(result).toHaveLength(1);
      expect(result[0].name).toBe(tool.name);
    }
  });
});

describe('filterByRbac — WRITE (OPERATOR) role', () => {
  const operatorCtx = makeCtx({ roles: new Map([['platform', 'OPERATOR']]) });

  it('allows READ and WRITE tools', () => {
    const result = filterByRbac([READ_TOOL, WRITE_TOOL], operatorCtx);
    expect(result.map((t) => t.name)).toEqual(['read_tool', 'write_tool']);
  });

  it('allows APPROVAL tools', () => {
    const result = filterByRbac([APPROVAL_TOOL], operatorCtx);
    expect(result).toHaveLength(1);
    expect(result[0].name).toBe('approval_tool');
  });

  it('denies DELETE tools', () => {
    const result = filterByRbac([DELETE_TOOL], operatorCtx);
    expect(result).toHaveLength(0);
  });
});

describe('filterByRbac — ANALYST role', () => {
  const analystCtx = makeCtx({ roles: new Map([['platform', 'ANALYST']]) });

  it('allows READ tools', () => {
    const result = filterByRbac([READ_TOOL], analystCtx);
    expect(result).toHaveLength(1);
  });

  it('allows ANALYZE risk level (via cast in implementation)', () => {
    const analyzeTool = makeTool({ name: 'analyze_tool', riskLevel: 'ANALYZE' as any });
    const result = filterByRbac([analyzeTool], analystCtx);
    expect(result).toHaveLength(1);
  });

  it('denies WRITE tools', () => {
    const result = filterByRbac([WRITE_TOOL], analystCtx);
    expect(result).toHaveLength(0);
  });

  it('denies DELETE tools', () => {
    const result = filterByRbac([DELETE_TOOL], analystCtx);
    expect(result).toHaveLength(0);
  });
});

describe('filterByRbac — no role / missing domain entry', () => {
  it('denies access when role map has no entry for the tool domain', () => {
    const ctx = makeCtx({
      domains: ['platform'],
      roles: new Map([['other-domain', 'READER']]),
    });
    const result = filterByRbac(ALL_RISK_TOOLS, ctx);
    expect(result).toHaveLength(0);
  });

  it('denies access when roles map is empty', () => {
    const ctx = makeCtx({
      domains: ['platform'],
      roles: new Map(),
    });
    const result = filterByRbac(ALL_RISK_TOOLS, ctx);
    expect(result).toHaveLength(0);
  });
});

describe('filterByRbac — unknown role enums', () => {
  it('defaults to READ-only for unrecognized roles', () => {
    const ctx = makeCtx({ roles: new Map([['platform', 'SUPERVISOR']]) });
    const result = filterByRbac(ALL_RISK_TOOLS, ctx);
    expect(result.map((t) => t.name)).toEqual(['read_tool']);
  });

  it('denies access when role is empty string (falsy, treated as missing role)', () => {
    const ctx = makeCtx({ roles: new Map([['platform', '']]) });
    const result = filterByRbac(ALL_RISK_TOOLS, ctx);
    expect(result).toHaveLength(0);
  });
});

// ====================================================================
// Cross-domain access control
// ====================================================================

describe('Cross-domain RBAC', () => {
  it('user with domain A role can only see domain A tools', () => {
    const ctx = makeCtx({
      domains: ['finance'],
      roles: new Map([['finance', 'ADMIN']]),
    });
    const result = filterByRbac(CROSS_DOMAIN_TOOLS, ctx);
    // Only the finance-domain tool should pass (and only if it matches risk level)
    expect(result.map((t) => t.name)).toEqual(['finance_tool']);
  });

  it('user with domain A role cannot access domain B tools (even with matching risk level)', () => {
    const ctx = makeCtx({
      domains: ['hr'],
      roles: new Map([['hr', 'READER']]),
    });
    // hr_tool is riskLevel DELETE, but READER only allows READ
    const result = filterByRbac(CROSS_DOMAIN_TOOLS, ctx);
    expect(result).toHaveLength(0);
  });

  it('user with ADMIN on domain A and READER on domain B sees both domain tools at appropriate levels', () => {
    const ctx = makeCtx({
      domains: ['platform', 'finance'],
      roles: new Map([
        ['platform', 'ADMIN'],
        ['finance', 'READER'],
      ]),
    });
    // platform domain: ADMIN can see all risk levels in platform
    // finance domain: READER can only see READ-level tools in finance
    const tools = [
      makeTool({ name: 'plat_read', riskLevel: 'READ', domain: 'platform' }),
      makeTool({ name: 'plat_write', riskLevel: 'WRITE', domain: 'platform' }),
      makeTool({ name: 'finance_read', riskLevel: 'READ', domain: 'finance' }),
      makeTool({ name: 'finance_write', riskLevel: 'WRITE', domain: 'finance' }),
    ];
    const result = filterByRbac(tools, ctx);
    expect(result.map((t) => t.name)).toEqual([
      'plat_read',
      'plat_write',
      'finance_read',
    ]);
  });

  it('user with no domains still gets tools filtered by role via filterByRbac (domain check is in applyRbac, not filterByRbac)', () => {
    const ctx = makeCtx({
      domains: [],
      roles: new Map([['platform', 'ADMIN']]),
    });
    // filterByRbac only checks roles, not domains — all platform tools pass
    const result = filterByRbac(ALL_RISK_TOOLS, ctx);
    expect(result).toHaveLength(4);
  });

  it('applyRbac blocks tools when domains is empty (combines domain+role)', () => {
    const ctx = makeCtx({
      domains: [],
      roles: new Map([['platform', 'ADMIN']]),
    });
    const result = applyRbac(ALL_RISK_TOOLS, ctx);
    expect(result).toHaveLength(0);
  });
});

// ====================================================================
// applyRbac — combined domain + role filtering
// ====================================================================

describe('applyRbac', () => {
  it('filters by domain first, then by role', () => {
    const ctx = makeCtx({
      domains: ['platform', 'finance'],
      roles: new Map([
        ['platform', 'READER'],
        ['finance', 'ADMIN'],
      ]),
    });
    const tools = [
      makeTool({ name: 'plat_read', riskLevel: 'READ', domain: 'platform' }),
      makeTool({ name: 'plat_delete', riskLevel: 'DELETE', domain: 'platform' }),
      makeTool({ name: 'finance_delete', riskLevel: 'DELETE', domain: 'finance' }),
      makeTool({ name: 'finance_read', riskLevel: 'READ', domain: 'finance' }),
    ];
    const result = applyRbac(tools, ctx);
    // platform/READER → only READ; finance/ADMIN → all risk levels
    expect(result.map((t) => t.name)).toEqual([
      'plat_read',
      'finance_delete',
      'finance_read',
    ]);
  });

  it('returns empty when no domain matches', () => {
    const ctx = makeCtx({
      domains: ['nonexistent'],
      roles: new Map([['nonexistent', 'ADMIN']]),
    });
    const result = applyRbac(ALL_RISK_TOOLS, ctx);
    expect(result).toHaveLength(0);
  });

  it('returns empty when role is missing for a matching domain', () => {
    const ctx = makeCtx({
      domains: ['platform', 'finance'],
      roles: new Map([['platform', 'ADMIN']]), // finance has no role
    });
    const tools = [
      makeTool({ name: 'plat_tool', riskLevel: 'READ', domain: 'platform' }),
      makeTool({ name: 'fin_tool', riskLevel: 'READ', domain: 'finance' }),
    ];
    const result = applyRbac(tools, ctx);
    expect(result.map((t) => t.name)).toEqual(['plat_tool']);
  });
});

// ====================================================================
// Edge cases
// ====================================================================

describe('RBAC — edge cases', () => {
  it('handles empty tool list', () => {
    const ctx = makeCtx({ roles: new Map([['platform', 'ADMIN']]) });
    expect(filterByRbac([], ctx)).toEqual([]);
    expect(applyRbac([], ctx)).toEqual([]);
  });

  it('handles undefined context roles gracefully', () => {
    // TypeScript would catch this, but check runtime behavior
    const ctx = makeCtx({ roles: undefined as unknown as Map<string, string> });
    expect(() => filterByRbac(ALL_RISK_TOOLS, ctx)).toThrow();
  });

  it('handles multiple tools with same domain and different risk levels correctly filtered for READER', () => {
    const ctx = makeCtx({ roles: new Map([['platform', 'READER']]) });
    const tools = [
      makeTool({ name: 'a', riskLevel: 'READ', domain: 'platform' }),
      makeTool({ name: 'b', riskLevel: 'WRITE', domain: 'platform' }),
      makeTool({ name: 'c', riskLevel: 'READ', domain: 'platform' }),
    ];
    const result = filterByRbac(tools, ctx);
    expect(result.map((t) => t.name)).toEqual(['a', 'c']);
  });

  it('case-insensitive role lookup (lowercase)', () => {
    const ctx = makeCtx({ roles: new Map([['platform', 'admin']]) });
    const result = filterByRbac(ALL_RISK_TOOLS, ctx);
    expect(result).toHaveLength(4);
  });

  it('case-insensitive role lookup (mixed case)', () => {
    const ctx = makeCtx({ roles: new Map([['platform', 'Operator']]) });
    const result = filterByRbac([READ_TOOL, WRITE_TOOL, DELETE_TOOL], ctx);
    expect(result.map((t) => t.name)).toEqual(['read_tool', 'write_tool']);
  });
});

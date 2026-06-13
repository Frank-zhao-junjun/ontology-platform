// =============================================
// RBAC — Role-Based Access Control filter
// =============================================

import type { AgentContext, ToolDefinition } from '../types/index.js';

/**
 * Filter tools by agent's authorized domains.
 * Agent can only see tools whose domain matches their authorized domains.
 */
export function filterByDomain(tools: ToolDefinition[], ctx: AgentContext): ToolDefinition[] {
  return tools.filter((tool) => ctx.domains.includes(tool.domain));
}

/**
 * Filter tools by RBAC: domain + role -> allowed operations.
 * Checks if the agent's role in the tool's domain allows the tool's risk level.
 */
export function filterByRbac(tools: ToolDefinition[], ctx: AgentContext): ToolDefinition[] {
  return tools.filter((tool) => {
    const role = ctx.roles.get(tool.domain);
    if (!role) return false;

    // Role-based operation mapping
    const allowedOps = getAllowedOperations(role);
    return allowedOps.includes(tool.riskLevel);
  });
}

function getAllowedOperations(role: string): string[] {
  switch (role.toUpperCase()) {
    case 'ADMIN':
      return ['READ', 'WRITE', 'DELETE', 'APPROVAL'];
    case 'OPERATOR':
      return ['READ', 'WRITE', 'APPROVAL'];
    case 'ANALYST':
      return ['READ', 'ANALYZE' as unknown as string];
    case 'READER':
      return ['READ'];
    default:
      return ['READ'];
  }
}

/**
 * Check if a specific operation requires approval.
 */
export function requiresApproval(riskLevel: string): boolean {
  return riskLevel === 'DELETE' || riskLevel === 'APPROVAL';
}

/**
 * Full RBAC gate for tools/list and tools/call.
 */
export function applyRbac(
  tools: ToolDefinition[],
  ctx: AgentContext,
  operation?: string
): ToolDefinition[] {
  // Step 1: filter by domain
  let filtered = filterByDomain(tools, ctx);

  // Step 2: filter by role permissions
  filtered = filterByRbac(filtered, ctx);

  // Step 3: if requesting a specific tool/operation, check approval
  if (operation) {
    const tool = filtered.find((t) => t.name === operation);
    if (tool && requiresApproval(tool.riskLevel)) {
      // Mark as requiring approval — don't filter out, but let caller handle
    }
  }

  return filtered;
}

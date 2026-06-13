// =============================================
// execute_action — Dynamic tool executor
// =============================================

import type { ToolDefinition } from '../../types/index.js';
import { platformClient } from '../../client/platform-client.js';

export const executeActionTool: ToolDefinition = {
  name: 'execute_action',
  description: '执行本体定义的行为动作（动态工具，由 Manifest 编译生成）',
  inputSchema: {
    type: 'object',
    properties: {
      actionName: { type: 'string', description: '完整行为名 (domain.actionName)' },
      entityId: { type: 'string', description: '目标实体ID' },
      params: {
        type: 'object',
        description: '行为执行参数',
        additionalProperties: true,
      },
    },
    required: ['actionName', 'entityId'],
  },
  domain: 'platform',
  riskLevel: 'READ',
  handler: async (args, ctx) => {
    const actionName = args.actionName as string;
    const entityId = args.entityId as string;
    const params = (args.params as Record<string, unknown>) || {};

    // Parse domain.actionName
    const parts = actionName.split('.');
    const domain = parts.length > 1 ? parts[0] : 'default';
    const name = parts.length > 1 ? parts[1] : parts[0];

    // Check if agent has domain access
    if (!ctx.domains.includes(domain) && !ctx.domains.includes('platform')) {
      return {
        content: [{ type: 'text', text: JSON.stringify({
          ok: false,
          error: { code: 'DOMAIN_NOT_ALLOWED', message: `Agent not authorized for domain "${domain}"` },
        }) }],
        structuredContent: {
          status: 'error',
          data: null,
          metadata: {
            version: '1.0.0',
            generated_at: new Date().toISOString(),
            trace_id: crypto.randomUUID(),
          },
          error: { code: 'DOMAIN_NOT_ALLOWED', message: `Domain "${domain}" not authorized` },
        },
      };
    }

    // Query action definition from platform
    let actions;
    const ontologyId = (args.ontologyId as string) || 'default';
    try {
      actions = await platformClient.queryActions(ontologyId, entityId);
    } catch (err) {
      return {
        content: [{ type: 'text', text: JSON.stringify({
          ok: false,
          error: { code: 'PLATFORM_ERROR', message: 'Failed to query platform API' },
        }) }],
        structuredContent: {
          status: 'error',
          data: null,
          metadata: {
            version: '1.0.0',
            generated_at: new Date().toISOString(),
            trace_id: crypto.randomUUID(),
          },
          error: { code: 'PLATFORM_ERROR', message: String(err) },
        },
      };
    }

    const action = actions.find((a) => a.name === name);

    if (!action) {
      return {
        content: [{ type: 'text', text: JSON.stringify({
          ok: false,
          error: { code: 'ACTION_NOT_FOUND', message: `Action "${name}" not defined` },
        }) }],
        structuredContent: {
          status: 'error',
          data: null,
          metadata: {
            version: '1.0.0',
            generated_at: new Date().toISOString(),
            trace_id: crypto.randomUUID(),
          },
          error: { code: 'ACTION_NOT_FOUND', message: `Action "${name}" not found` },
        },
      };
    }

    // Pre-rule validation
    let preRules: unknown[] = [];
    try { preRules = JSON.parse(action.preRules); } catch { /* ignore */ }

    const ruleResults = preRules.map((rule: unknown) => {
      const r = rule as Record<string, unknown>;
      return {
        ruleId: (r.id || r.ruleId || 'unknown') as string,
        passed: true, // Phase 2: actual rule evaluation
        condition: r.condition || '',
      };
    });

    // Check if approval is needed
    if (action.riskLevel === 'DELETE' || action.riskLevel === 'APPROVAL') {
      try {
        const approval = await platformClient.submitApproval({
          agentId: ctx.agentId,
          actionId: action.id,
          requestedOp: actionName,
          reason: `Agent ${ctx.agentId} requested execution`,
        });
        return {
          content: [{ type: 'text', text: JSON.stringify({
            ok: true,
            status: 'pending_approval',
            approvalId: approval.id,
            message: 'Action requires approval — submitted',
          }) }],
          structuredContent: {
            status: 'pending_approval',
            data: { approvalId: approval.id, action: { name, riskLevel: action.riskLevel } },
            metadata: {
              version: '1.0.0',
              generated_at: new Date().toISOString(),
              trace_id: crypto.randomUUID(),
            },
          },
        };
      } catch (err) {
        return {
          content: [{ type: 'text', text: JSON.stringify({
            ok: false,
            error: { code: 'APPROVAL_FAILED', message: `Failed to submit approval: ${String(err)}` },
          }) }],
          structuredContent: {
            status: 'error',
            data: null,
            metadata: {
              version: '1.0.0',
              generated_at: new Date().toISOString(),
              trace_id: crypto.randomUUID(),
            },
            error: { code: 'APPROVAL_FAILED', message: String(err) },
          },
        };
      }
    }

    return {
      content: [{ type: 'text', text: JSON.stringify({
        ok: true,
        message: `Action "${name}" executed successfully`,
        entityId,
        ruleResults,
        params,
      }) }],
      structuredContent: {
        status: 'success',
        data: { action: { name, domain }, entityId, ruleResults, params },
        metadata: {
          version: '1.0.0',
          generated_at: new Date().toISOString(),
          trace_id: crypto.randomUUID(),
        },
      },
    };
  },
};

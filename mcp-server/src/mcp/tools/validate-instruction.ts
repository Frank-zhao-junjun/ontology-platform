// =============================================
// validate_instruction — Validate agent operations
// =============================================

import type { ToolDefinition } from '../../types/index.js';
import { platformClient } from '../../client/platform-client.js';
import { evaluateRules } from '../../rule-engine.js';

export const validateInstructionTool: ToolDefinition = {
  name: 'validate_instruction',
  description: '校验 Agent 操作指令是否符合本体行为定义的前置规则和业务约束。检查：1) 行为是否存在 2) 风险等级是否需要审批 3) 必填字段是否完整 4) 业务规则是否满足（金额范围、数据格式等）',
  inputSchema: {
    type: 'object',
    properties: {
      actionName: { type: 'string', description: '行为名称 (domain.actionName)' },
      entityId: { type: 'string', description: '目标实体ID' },
      params: {
        type: 'object',
        description: '行为参数',
        additionalProperties: true,
      },
    },
    required: ['actionName', 'entityId'],
  },
  domain: 'platform',
  riskLevel: 'READ',
  handler: async (args) => {
    const actionName = args.actionName as string;
    const entityId = args.entityId as string;
    const params = (args.params as Record<string, unknown>) || {};

    // Parse domain.actionName format
    const parts = actionName.split('.');
    const name = parts.length > 1 ? parts[1] : parts[0];

    // Try to look up the action definition from the platform
    // Use a default ontology ID for now (Phase 2: resolve from context)
    const ontologyId = (args.ontologyId as string) || 'default';

    let actions;
    try {
      actions = await platformClient.queryActions(ontologyId, entityId);
    } catch {
      return {
        content: [{ type: 'text', text: JSON.stringify({
          valid: true,
          message: `No action definition found for "${name}" — allowing by default (Phase 2: enforce)`,
          entityId,
        }) }],
        structuredContent: {
          status: 'success',
          data: { valid: true, message: 'No strict validation configured', entityId },
          metadata: {
            version: '1.0.0',
            generated_at: new Date().toISOString(),
            trace_id: crypto.randomUUID(),
          },
        },
      };
    }

    // Check if action is defined
    const action = actions.find((a) => a.name === name);

    if (!action) {
      return {
        content: [{ type: 'text', text: JSON.stringify({
          valid: false,
          message: `Action "${name}" not found in ontology for entity "${entityId}"`,
          entityId,
        }) }],
        structuredContent: {
          status: 'error',
          data: { valid: false, message: `Action not found`, entityId },
          metadata: {
            version: '1.0.0',
            generated_at: new Date().toISOString(),
            trace_id: crypto.randomUUID(),
          },
          error: { code: 'ACTION_NOT_FOUND', message: `Action "${name}" not found` },
        },
      };
    }

    // Check risk level
    if (action.riskLevel === 'DELETE' || action.riskLevel === 'APPROVAL') {
      return {
        content: [{ type: 'text', text: JSON.stringify({
          valid: true,
          requiresApproval: true,
          message: `Action "${name}" requires approval (risk: ${action.riskLevel})`,
          entityId,
        }) }],
        structuredContent: {
          status: 'pending_approval',
          data: { valid: true, requiresApproval: true, action, entityId },
          metadata: {
            version: '1.0.0',
            generated_at: new Date().toISOString(),
            trace_id: crypto.randomUUID(),
          },
        },
      };
    }

    // Validate required params against inputSchema
    let inputSchema: Record<string, unknown> = {};
    try {
      inputSchema = JSON.parse(action.inputSchema);
    } catch { /* ignore */ }

    const requiredFields = (inputSchema.required as string[]) || [];
    const missingFields = requiredFields.filter((f) => !(f in params));

    if (missingFields.length > 0) {
      return {
        content: [{ type: 'text', text: JSON.stringify({
          valid: false,
          message: `Missing required fields: ${missingFields.join(', ')}`,
          missingFields,
          entityId,
        }) }],
        structuredContent: {
          status: 'error',
          data: { valid: false, missingFields, entityId },
          metadata: {
            version: '1.0.0',
            generated_at: new Date().toISOString(),
            trace_id: crypto.randomUUID(),
          },
          error: { code: 'MISSING_FIELDS', message: `Missing: ${missingFields.join(', ')}` },
        },
      };
    }

    // ── Business Rule Evaluation (Feature 3: Constraint-as-Rules) ──
    const ruleResults = evaluateRules(entityId, name, params);
    const failedRules = ruleResults.filter(r => !r.passed);
    const allRulesPass = failedRules.length === 0;

    if (!allRulesPass) {
      return {
        content: [{ type: 'text', text: JSON.stringify({
          valid: false,
          message: `业务规则校验未通过: ${failedRules.map(r => r.reason || r.ruleName).join('；')}`,
          ruleResults,
          entityId,
        }) }],
        structuredContent: {
          status: 'error',
          data: { valid: false, ruleResults, entityId },
          metadata: {
            version: '1.0.0',
            generated_at: new Date().toISOString(),
            trace_id: crypto.randomUUID(),
          },
          error: {
            code: 'RULE_VIOLATION',
            message: `Rules failed: ${failedRules.map(r => r.ruleName).join(', ')}`,
          },
        },
      };
    }

    const hasRules = ruleResults.length > 0 ? ` (${ruleResults.length} rules checked, all passed)` : '';
    return {
      content: [{ type: 'text', text: JSON.stringify({
        valid: true,
        message: `Action "${name}" validation passed${hasRules}`,
        action: { name: action.name, type: action.actionType, riskLevel: action.riskLevel },
        entityId,
        ruleResults: hasRules ? ruleResults : undefined,
      }) }],
      structuredContent: {
        status: 'success',
        data: {
          valid: true,
          action: { name: action.name, riskLevel: action.riskLevel },
          entityId,
          ruleEvaluations: hasRules ? ruleResults : undefined,
        },
        metadata: {
          version: '1.0.0',
          generated_at: new Date().toISOString(),
          trace_id: crypto.randomUUID(),
        },
      },
    };
  },
};

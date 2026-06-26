// =============================================
// Rule Engine — Evaluate ontology business rules against agent actions
// Approach 4 (Constraint-as-Rules): rules become agent execution boundaries
// =============================================

import { getOntologyModel, type OntologyRule } from './model-loader.js';

export interface RuleEvaluation {
  ruleId: string;
  ruleName: string;
  passed: boolean;
  reason?: string;
}

export interface ValidationResult {
  valid: boolean;
  ruleResults: RuleEvaluation[];
  message: string;
}

/**
 * Get all rules from the loaded ontology model.
 */
export function getAllRules(): OntologyRule[] {
  const model = getOntologyModel();
  return model?.rules ?? [];
}

/**
 * Get rules that apply to a specific entity.
 */
export function getEntityRules(entityId: string): OntologyRule[] {
  return getAllRules().filter(r =>
    r.entity === entityId || r.entity === '*' || r.entity === 'all',
  );
}

/**
 * Evaluate all rules against an action+params combination.
 * Used by validate_instruction to enforce business constraints.
 */
export function evaluateRules(
  entityId: string,
  actionName: string,
  params: Record<string, unknown>,
): RuleEvaluation[] {
  const rules = getEntityRules(entityId);
  if (rules.length === 0) return [];

  return rules.map(rule => evaluateRule(rule, actionName, params));
}

/**
 * Evaluate a single rule against action params.
 */
function evaluateRule(
  rule: OntologyRule,
  _actionName: string,
  params: Record<string, unknown>,
): RuleEvaluation {
  const condition = rule.condition as Record<string, unknown> | undefined;
  if (!condition || !condition.type) {
    return { ruleId: rule.id, ruleName: rule.name, passed: true, reason: 'No condition defined' };
  }

  const field = rule.field || '';
  const fieldValue = field ? params[field] : undefined;

  switch (condition.type as string) {
    case 'range': {
      const min = condition.min as number | undefined;
      const max = condition.max as number | undefined;
      const val = Number(fieldValue);

      if (isNaN(val)) {
        return { ruleId: rule.id, ruleName: rule.name, passed: true, reason: `Field "${field}" is not a number, skipping range check` };
      }

      if (min !== undefined && val < min) {
        return {
          ruleId: rule.id,
          ruleName: rule.name,
          passed: false,
          reason: rule.description || `${field} = ${val} 低于最小值 ${min}`,
        };
      }
      if (max !== undefined && val > max) {
        return {
          ruleId: rule.id,
          ruleName: rule.name,
          passed: false,
          reason: rule.description || `${field} = ${val} 超过最大值 ${max}`,
        };
      }
      return { ruleId: rule.id, ruleName: rule.name, passed: true };
    }

    case 'expression': {
      const expr = condition.expression as string;
      if (!expr) {
        return { ruleId: rule.id, ruleName: rule.name, passed: true, reason: 'No expression defined' };
      }
      // For MVP: simple string interpolation of params into expression
      // Real implementation would use a safe expression evaluator
      let evaluated = expr;
      const paramKeys = Object.keys(params);
      for (const key of paramKeys) {
        const val = params[key];
        // Split on {key} and join with value (replaces all occurrences)
        const token = `{${key}}`;
        const parts = evaluated.split(token);
        evaluated = parts.join(String(val));
      }

      // Check for simple numeric comparisons
      const matchNumeric = evaluated.match(/^(\d+)\s*(<|<=|>|>=|==|!=)\s*(\d+)$/);
      if (matchNumeric) {
        const a = Number(matchNumeric[1]);
        const op = matchNumeric[2];
        const b = Number(matchNumeric[3]);
        let result = false;
        switch (op) {
          case '<': result = a < b; break;
          case '<=': result = a <= b; break;
          case '>': result = a > b; break;
          case '>=': result = a >= b; break;
          case '==': result = a === b; break;
          case '!=': result = a !== b; break;
        }
        return {
          ruleId: rule.id,
          ruleName: rule.name,
          passed: result,
          reason: result ? undefined : (rule.description || `条件不满足: ${expr}`),
        };
      }

      // Default: pass for unrecognized expression patterns
      return { ruleId: rule.id, ruleName: rule.name, passed: true, reason: `Expression not evaluated (MVP): ${expr}` };
    }

    case 'regex': {
      const pattern = condition.pattern as string;
      if (!pattern || fieldValue === undefined) {
        return { ruleId: rule.id, ruleName: rule.name, passed: true };
      }
      try {
        const regex = new RegExp(pattern);
        const passed = regex.test(String(fieldValue));
        return {
          ruleId: rule.id,
          ruleName: rule.name,
          passed,
          reason: passed ? undefined : (rule.description || `${field} 不符合格式要求`),
        };
      } catch {
        return { ruleId: rule.id, ruleName: rule.name, passed: true, reason: 'Regex pattern invalid' };
      }
    }

    default:
      return { ruleId: rule.id, ruleName: rule.name, passed: true, reason: `Unsupported condition type: ${condition.type}` };
  }
}

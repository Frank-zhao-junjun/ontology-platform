// =============================================
// resolve_intent — Natural language to IntentCategory / Semantic Layer
// =============================================

import type { ToolDefinition } from '../../types/index.js';
import { IntentCategory } from '../../types/index.js';
import { platformClient } from '../../client/platform-client.js';

const PLATFORM_ONTOLOGY_ID = process.env.PLATFORM_ONTOLOGY_ID || '';

function resolveIntentFromQuery(query: string): {
  category: IntentCategory;
  confidence: number;
  entities: string[];
  suggestedTool?: string;
} {
  const lower = query.toLowerCase();

  const queryPatterns = ['查', '查看', '查询', '显示', '列出', 'get', 'list', 'show', 'find', 'search', '获取', '看看', '有哪些'];
  const createPatterns = ['创建', '新建', '添加', 'create', 'add', 'new', '下达', '下达生产订单'];
  const updatePatterns = ['更新', '修改', '变更', 'update', 'edit', 'change', 'modify', '报工'];
  const deletePatterns = ['删除', '取消', '作废', 'delete', 'cancel', 'remove'];
  const analyzePatterns = ['分析', '统计', '汇总', 'analyze', 'analysis', 'report', '趋势', '指标'];
  const navigatePatterns = ['跳转', '导航', 'navigate', 'go to', '打开'];
  const executePatterns = ['执行', '执行行为', 'execute', 'run', 'trigger', '调用'];

  const allPatterns: Array<{ patterns: string[]; category: IntentCategory }> = [
    { patterns: createPatterns, category: IntentCategory.CREATE },
    { patterns: updatePatterns, category: IntentCategory.UPDATE },
    { patterns: deletePatterns, category: IntentCategory.DELETE },
    { patterns: analyzePatterns, category: IntentCategory.ANALYZE },
    { patterns: navigatePatterns, category: IntentCategory.NAVIGATE },
    { patterns: executePatterns, category: IntentCategory.EXECUTE },
    { patterns: queryPatterns, category: IntentCategory.QUERY },
  ];

  let bestCategory = IntentCategory.UNKNOWN;
  let bestConfidence = 0;

  for (const { patterns, category } of allPatterns) {
    for (const p of patterns) {
      if (lower.includes(p)) {
        const confidence = 0.5 + (p.length / lower.length) * 0.3;
        if (confidence > bestConfidence) {
          bestConfidence = confidence;
          bestCategory = category;
        }
      }
    }
  }

  const entities: string[] = [];
  const entityPatterns = [
    '生产订单', '工单', '物料', 'BOM', '库存', '工艺路线',
    'ProductionOrder', 'WorkOrder', 'Material', 'InventoryBalance', 'Routing',
  ];
  for (const e of entityPatterns) {
    if (lower.includes(e.toLowerCase()) || lower.includes(e)) {
      entities.push(e);
    }
  }

  let suggestedTool: string | undefined;
  switch (bestCategory) {
    case IntentCategory.QUERY:
      suggestedTool = 'query_ontology';
      break;
    case IntentCategory.CREATE:
    case IntentCategory.UPDATE:
    case IntentCategory.EXECUTE:
      suggestedTool = 'validate_instruction';
      break;
    case IntentCategory.ANALYZE:
      suggestedTool = 'traverse_graph';
      break;
  }

  return {
    category: bestCategory,
    confidence: Math.min(bestConfidence, 0.95),
    entities,
    suggestedTool,
  };
}

function formatStructuredResult(data: Record<string, unknown>) {
  const confidence = typeof data.confidence === 'number' ? data.confidence : 0.5;
  return {
    content: [{ type: 'text', text: JSON.stringify(data) }],
    structuredContent: {
      status: 'success' as const,
      data,
      metadata: {
        version: '1.0.0',
        generated_at: new Date().toISOString(),
        trace_id: crypto.randomUUID(),
        confidence,
      },
    },
  };
}

export const resolveIntentTool: ToolDefinition = {
  name: 'resolve_intent',
  description: '将自然语言业务查询解析为意图分类、关联实体和建议的下一个工具',
  inputSchema: {
    type: 'object',
    properties: {
      query: { type: 'string', description: '用户原始查询字符串' },
    },
    required: ['query'],
  },
  domain: 'platform',
  riskLevel: 'READ',
  handler: async (args) => {
    const query = (args.query as string) || '';

    if (PLATFORM_ONTOLOGY_ID) {
      try {
        const platformResult = await platformClient.resolveIntent(PLATFORM_ONTOLOGY_ID, query);
        if (platformResult?.actionId) {
          const confidence = Math.min(0.5 + (platformResult.matchScore ?? 5) / 20, 0.95);
          return formatStructuredResult({
            category: IntentCategory.EXECUTE,
            confidence,
            entities: [],
            suggestedTool: 'validate_instruction',
            intentId: platformResult.id,
            actionId: platformResult.actionId,
            intentName: platformResult.name,
            slots: platformResult.slots ?? [],
            source: 'semantic-layer',
          });
        }
      } catch (err) {
        console.warn('[resolve_intent] Platform semantic lookup failed, falling back to keywords:', err);
      }
    }

    const result = resolveIntentFromQuery(query);
    return formatStructuredResult({ ...result, source: 'keyword-fallback' });
  },
};

// =============================================
// traverse_graph — Graph traversal query
// =============================================

import type { ToolDefinition } from '../../types/index.js';
import { platformClient } from '../../client/platform-client.js';

export const traverseGraphTool: ToolDefinition = {
  name: 'traverse_graph',
  description: '遍历本体对象图，按关系类型探索关联实体',
  inputSchema: {
    type: 'object',
    properties: {
      ontologyId: { type: 'string', description: '本体ID' },
      startEntityId: { type: 'string', description: '起始实体ID' },
      relationTypes: {
        type: 'array',
        items: { type: 'string' },
        description: '要遍历的关系类型列表',
      },
      maxDepth: { type: 'number', description: '最大遍历深度', default: 3 },
    },
    required: ['ontologyId', 'startEntityId'],
  },
  domain: 'platform',
  riskLevel: 'READ',
  handler: async (args) => {
    const ontologyId = args.ontologyId as string;
    const startEntityId = args.startEntityId as string;
    const relationTypes = args.relationTypes as string[] | undefined;
    const maxDepth = (args.maxDepth as number) || 3;

    // Query related entities via actions/events/epc
    const actions = await platformClient.queryActions(ontologyId, startEntityId);
    const events = await platformClient.queryEvents(ontologyId, startEntityId);

    const graph = {
      startEntityId,
      maxDepth,
      relationTypes: relationTypes || [],
      actions: actions.map((a) => ({
        name: a.name,
        type: a.actionType,
        domain: a.domain,
        riskLevel: a.riskLevel,
        stateMachines: a.stateMachines?.map((sm) => ({
          name: sm.name,
          initialState: sm.initialState,
          transitions: sm.transitions.map((t) => ({
            from: t.fromState,
            to: t.toState,
            trigger: t.trigger,
          })),
        })),
      })),
      events: events.map((e) => ({
        name: e.name,
        type: e.eventType,
        severity: e.severity,
        causalities: e.causalities.map((c) => ({
          causeEventId: c.causeEventId,
          effectEventId: c.effectEventId,
          description: c.description,
          delayMs: c.delayMs,
        })),
      })),
    };

    return {
      content: [{ type: 'text', text: JSON.stringify(graph) }],
      structuredContent: {
        status: 'success',
        data: graph,
        metadata: {
          version: '1.0.0',
          generated_at: new Date().toISOString(),
          trace_id: crypto.randomUUID(),
        },
      },
    };
  },
};

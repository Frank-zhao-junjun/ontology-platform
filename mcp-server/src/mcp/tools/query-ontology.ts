// =============================================
// query_ontology — Query ontology actions/events/epc
// =============================================

import type { ToolDefinition } from '../../types/index.js';
import { platformClient } from '../../client/platform-client.js';

export const queryOntologyTool: ToolDefinition = {
  name: 'query_ontology',
  description: '查询本体的行为定义、事件定义和 EPC 编排步骤',
  inputSchema: {
    type: 'object',
    properties: {
      ontologyId: { type: 'string', description: '本体ID' },
      entities: {
        type: 'array',
        items: { type: 'string' },
        description: '要查询的实体ID列表',
      },
      includeActions: { type: 'boolean', description: '是否包含行为定义', default: true },
      includeEvents: { type: 'boolean', description: '是否包含事件定义', default: true },
      includeEpc: { type: 'boolean', description: '是否包含EPC步骤', default: false },
    },
    required: ['ontologyId'],
  },
  domain: 'platform',
  riskLevel: 'READ',
  handler: async (args, ctx) => {
    const ontologyId = args.ontologyId as string;
    const entities = args.entities as string[] | undefined;
    const includeActions = args.includeActions !== false;
    const includeEvents = args.includeEvents !== false;
    const includeEpc = args.includeEpc === true;

    const result: Record<string, unknown> = { ontologyId };

    if (includeActions && entities) {
      const allActions = [];
      for (const entityId of entities) {
        const actions = await platformClient.queryActions(ontologyId, entityId);
        allActions.push({ entityId, actions });
      }
      result.actions = allActions;
    } else if (includeActions) {
      result.actions = await platformClient.queryActions(ontologyId);
    }

    if (includeEvents && entities) {
      const allEvents = [];
      for (const entityId of entities) {
        const events = await platformClient.queryEvents(ontologyId, entityId);
        allEvents.push({ entityId, events });
      }
      result.events = allEvents;
    } else if (includeEvents) {
      result.events = await platformClient.queryEvents(ontologyId);
    }

    if (includeEpc) {
      result.epc = await platformClient.queryEpc(ontologyId);
    }

    return {
      content: [{ type: 'text', text: JSON.stringify(result) }],
      structuredContent: {
        status: 'success',
        data: result,
        metadata: {
          version: '1.0.0',
          generated_at: new Date().toISOString(),
          trace_id: crypto.randomUUID(),
        },
      },
    };
  },
};

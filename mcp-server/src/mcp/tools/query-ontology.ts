// =============================================
// query_ontology — Query ontology actions/events/epc
// =============================================

import type { ToolDefinition } from '../../types/index.js';
import { platformClient } from '../../client/platform-client.js';

export const queryOntologyTool: ToolDefinition = {
  name: 'query_ontology',
  description: '查询本体的行为定义、事件定义、EPC 编排、语义层、生命周期与 EPC 覆盖报告',
  inputSchema: {
    type: 'object',
    properties: {
      ontologyId: { type: 'string', description: '本体ID' },
      entities: {
        type: 'array',
        items: { type: 'string' },
        description: '要查询的实体ID列表',
      },
      entityId: { type: 'string', description: '生命周期查询的目标实体ID（includeLifecycle 为 true 时必填）' },
      includeActions: { type: 'boolean', description: '是否包含行为定义', default: true },
      includeEvents: { type: 'boolean', description: '是否包含事件定义', default: true },
      includeEpc: { type: 'boolean', description: '是否包含EPC步骤（legacy flat）', default: false },
      includeEpcCoverage: { type: 'boolean', description: '是否包含 EPC 图覆盖报告（Phase 3d）', default: false },
      includeSemantic: { type: 'boolean', description: '是否包含 Agent 语义层', default: false },
      includeLifecycle: { type: 'boolean', description: '是否包含实体生命周期聚合', default: false },
    },
    required: ['ontologyId'],
  },
  domain: 'platform',
  riskLevel: 'READ',
  handler: async (args, ctx) => {
    const ontologyId = args.ontologyId as string;
    const entities = args.entities as string[] | undefined;
    const entityId = args.entityId as string | undefined;
    const includeActions = args.includeActions !== false;
    const includeEvents = args.includeEvents !== false;
    const includeEpc = args.includeEpc === true;
    const includeEpcCoverage = args.includeEpcCoverage === true;
    const includeSemantic = args.includeSemantic === true;
    const includeLifecycle = args.includeLifecycle === true;

    const result: Record<string, unknown> = { ontologyId };

    if (includeActions && entities) {
      const allActions = [];
      for (const eid of entities) {
        const actions = await platformClient.queryActions(ontologyId, eid);
        allActions.push({ entityId: eid, actions });
      }
      result.actions = allActions;
    } else if (includeActions) {
      result.actions = await platformClient.queryActions(ontologyId);
    }

    if (includeEvents && entities) {
      const allEvents = [];
      for (const eid of entities) {
        const events = await platformClient.queryEvents(ontologyId, eid);
        allEvents.push({ entityId: eid, events });
      }
      result.events = allEvents;
    } else if (includeEvents) {
      result.events = await platformClient.queryEvents(ontologyId);
    }

    if (includeEpc) {
      result.epc = await platformClient.queryEpc(ontologyId);
    }

    if (includeEpcCoverage) {
      result.epcCoverage = await platformClient.queryEpcCoverage(ontologyId);
    }

    if (includeSemantic) {
      result.semanticLayer = await platformClient.querySemanticLayer(ontologyId);
    }

    if (includeLifecycle) {
      if (!entityId) {
        throw new Error('entityId is required when includeLifecycle is true');
      }
      result.lifecycle = await platformClient.queryLifecycle(ontologyId, entityId);
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

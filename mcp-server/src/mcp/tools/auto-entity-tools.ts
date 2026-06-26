// =============================================
// Auto-Entity Tools — Generate MCP tools from ontology model
// Approach 1 (Tool-as-Semantics), lightweight:
// One tool to list + explore the ontology, Agent still uses query_ontology
// =============================================

import type { ToolDefinition } from '../../types/index.js';
import { getOntologyModel } from '../../model-loader.js';

export const listOntologyEntitiesTool: ToolDefinition = {
  name: 'list_ontology_entities',
  description: '查看本体模型中的实体类型列表及其属性、关系定义。调用后 Agent 能知道本体里有哪实体、每个实体有什么字段和关联。',
  inputSchema: {
    type: 'object',
    properties: {
      entityId: {
        type: 'string',
        description: '可选：指定实体 ID 查看详细信息。不传则返回所有实体概览。',
      },
      includeRelations: {
        type: 'boolean',
        description: '是否包含关系定义',
        default: false,
      },
      includeAttributes: {
        type: 'boolean',
        description: '是否包含属性详情',
        default: true,
      },
    },
  },
  domain: 'platform',
  riskLevel: 'READ',
  handler: async (args, _ctx) => {
    const model = getOntologyModel();
    if (!model) {
      return {
        content: [{ type: 'text', text: '尚未加载本体模型。请先通过项目1导出 ontology-model.json，并设置 ONTOLOGY_MODEL_PATH 环境变量后重启服务。' }],
      };
    }

    const entityId = args.entityId as string | undefined;
    const includeRelations = args.includeRelations === true;
    const includeAttributes = args.includeAttributes !== false;

    if (entityId) {
      // Detail view for a single entity
      const entity = model.entities.find(e => e.id === entityId || e.nameEn === entityId || e.name === entityId);
      if (!entity) {
        return {
          content: [{ type: 'text', text: `未找到实体 "${entityId}"。可用实体：${model.entities.map(e => e.nameEn || e.name).join('、')}` }],
        };
      }

      const detail: Record<string, unknown> = {
        id: entity.id,
        name: entity.name,
        nameEn: entity.nameEn || '',
        description: entity.description || '',
      };

      if (includeAttributes) {
        detail.attributes = entity.attributes.map(a => ({
          name: a.name,
          nameEn: a.nameEn || a.name,
          type: a.type,
          required: a.required || false,
          description: a.description || '',
        }));
      }

      if (includeRelations) {
        detail.relations = entity.relations.map(r => ({
          target: r.target,
          type: r.type,
          name: r.name || '',
        }));
      }

      return {
        content: [{ type: 'text', text: JSON.stringify(detail, null, 2) }],
      };
    }

    // Overview: list all entities
    const overview = model.entities.map(e => ({
      id: e.id,
      name: e.name,
      nameEn: e.nameEn || e.name,
      attributeCount: e.attributes.length,
      relationCount: e.relations.length,
      stateMachine: model.stateMachines.find(sm => sm.entity === e.id)?.name || null,
    }));

    // Include project info
    const result = {
      project: model.project,
      entityCount: model.entities.length,
      entities: overview,
      stateMachineCount: model.stateMachines.length,
      ruleCount: model.rules.length,
      metricCount: model.metrics.length,
      dataSourceCount: model.dataSources.length,
    };

    return {
      content: [{ type: 'text', text: JSON.stringify(result, null, 2) }],
    };
  },
};

export function generateEntityTools(): ToolDefinition[] {
  // Only one tool for Feature 2: list_ontology_entities
  // Fine-grained entity tools (search_*, get_*) deferred until needed
  const model = getOntologyModel();
  if (model) {
    console.log(`[auto-entity-tools] Ontology model loaded: "${model.project.name}" (${model.entities.length} entities)`);
  }
  return [listOntologyEntitiesTool];
}

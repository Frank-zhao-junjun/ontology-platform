// =============================================
// Entity/Rule Tool Management — Staging + Apply pattern
// 
// Flow:
//   1. load_ontology_model   — load file, show preview (staged, not registered)
//   2. apply_ontology_model  — user confirms, register tools into MCP server
//
// This prevents unintended model activation. Ontology model changes
// only affect the MCP server when explicitly applied.
// =============================================

import type { ToolDefinition } from '../../types/index.js';
import { toolRegistry } from './registry.js';
import { loadOntologyModel, type OntologyModel } from '../../model-loader.js';

// ── Staged state (not registered until apply) ──
let _stagedModel: OntologyModel | null = null;
let _stagedEntityTools: ToolDefinition[] = [];
let _applied = false;
let _registeredToolNames: string[] = [];

// ── Helpers ──

function mapAttributeType(type: string): string {
  const lower = type.toLowerCase();
  if (['number', 'integer', 'int', 'float', 'double', 'decimal'].includes(lower)) return 'number';
  if (['boolean', 'bool'].includes(lower)) return 'boolean';
  return 'string';
}

function buildEntityDetail(model: OntologyModel, entityId: string): Record<string, unknown> {
  const entity = model.entities.find(e => e.id === entityId || e.nameEn === entityId || e.name === entityId);
  if (!entity) {
    return { error: `未找到实体 "${entityId}"` };
  }

  const detail: Record<string, unknown> = {
    id: entity.id,
    name: entity.name,
    nameEn: entity.nameEn || '',
    description: entity.description || '',
    attributes: entity.attributes.map(a => ({
      name: a.name, nameEn: a.nameEn || a.name, type: a.type, required: a.required || false,
    })),
    relations: entity.relations.map(r => ({ target: r.target, type: r.type, name: r.name || '' })),
  };

  const entityRules = model.rules.filter(r => r.entity === entity.id || r.entity === entity.nameEn);
  if (entityRules.length > 0) {
    detail.businessRules = entityRules.map(r => ({
      id: r.id, name: r.name, description: r.description || '',
      field: r.field || '', condition: r.condition, severity: r.severity,
    }));
  }

  return detail;
}

// ── upload_ontology_model tool ──

export const uploadOntologyModelTool: ToolDefinition = {
  name: 'upload_ontology_model',
  description: '上传本体模型（预览模式）。传入模型 JSON 正文，显示实体列表和规则概览，但不注册到 MCP Server。确认生效请调用 apply_ontology_model。',
  inputSchema: {
    type: 'object',
    properties: {
      modelJson: {
        type: 'string',
        description: '从项目1导出的 .ontology-model.json 完整 JSON 内容',
      },
      entityId: {
        type: 'string',
        description: '可选：查看指定实体的详细信息',
      },
    },
    required: ['modelJson'],
  },
  domain: 'platform',
  riskLevel: 'READ',
  handler: async (args) => {
    const modelJson = args.modelJson as string;
    const entityId = args.entityId as string | undefined;

    let model: OntologyModel | null = null;

    try {
      const parsed = JSON.parse(modelJson);
      if (!parsed.version || !parsed.project || !Array.isArray(parsed.entities)) {
        return {
          content: [{ type: 'text', text: JSON.stringify({
            success: false,
            message: '模型 JSON 格式不正确：缺少 version / project / entities 字段',
          }, null, 2) }],
        };
      }
      model = parsed as OntologyModel;
    } catch {
      return {
        content: [{ type: 'text', text: JSON.stringify({
          success: false,
          message: '模型 JSON 解析失败：不是有效的 JSON 格式',
        }, null, 2) }],
      };
    }

    // Store as staged
    _stagedModel = model;
    _stagedEntityTools = [];
    _applied = false;

    if (entityId) {
      // Detail view for a single entity
      const detail = buildEntityDetail(model, entityId);
      return {
        content: [{ type: 'text', text: JSON.stringify({ success: true, staged: true, applied: false, entity: detail }, null, 2) }],
      };
    }

    // Preview: show overview
    const preview = {
      success: true,
      staged: true,
      applied: false,
      message: '模型已加载（暂存），尚未注册到 MCP Server。确认后请调用 apply_ontology_model。',
      project: model.project,
      entities: model.entities.map(e => ({
        id: e.id,
        name: e.name,
        nameEn: e.nameEn || e.name,
        attributeCount: e.attributes.length,
        relationCount: e.relations.length,
        stateMachine: model.stateMachines.find(sm => sm.entity === e.id)?.name || null,
      })),
      stateMachineCount: model.stateMachines.length,
      ruleCount: model.rules.length,
      metricCount: model.metrics.length,
      dataSourceCount: model.dataSources.length,
      governance: {
        roles: model.governance?.roles?.length ?? 0,
      },
    };

    return {
      content: [{ type: 'text', text: JSON.stringify(preview, null, 2) }],
    };
  },
};

// ── apply_ontology_model tool ──

export const applyOntologyModelTool: ToolDefinition = {
  name: 'apply_ontology_model',
  description: '确认并注册已暂存的本体模型。将 load_ontology_model 加载的模型中的实体信息和规则注册到 MCP Server 工具列表中。调用前必须先执行 load_ontology_model。',
  inputSchema: {
    type: 'object',
    properties: {
      confirm: {
        type: 'boolean',
        description: '确认注册。必须为 true 才会执行。',
      },
    },
    required: ['confirm'],
  },
  domain: 'platform',
  riskLevel: 'WRITE',
  handler: async (args) => {
    const confirm = args.confirm === true;

    if (!confirm) {
      return {
        content: [{ type: 'text', text: JSON.stringify({
          success: false,
          message: '未确认。如已执行 load_ontology_model，请设置 confirm: true 重新调用。',
        }, null, 2) }],
      };
    }

    if (!_stagedModel) {
      return {
        content: [{ type: 'text', text: JSON.stringify({
          success: false,
          message: '尚未加载模型。请先调用 load_ontology_model 加载 .ontology-model.json 文件。',
        }, null, 2) }],
      };
    }

    if (_applied) {
      return {
        content: [{ type: 'text', text: JSON.stringify({
          success: true,
          message: '模型已注册，无需重复操作。如需更新，请先调用 load_ontology_model 重新加载后再 apply。',
          applied: true,
        }, null, 2) }],
      };
    }

    // Generate entity tools
    const entityTools: ToolDefinition[] = [];

    for (const entity of _stagedModel.entities) {
      const entityName = entity.nameEn || entity.name.toLowerCase().replace(/\s+/g, '_');

      // ── search_{entity} tool ──
      const searchProperties: Record<string, unknown> = {
        query: { type: 'string', description: `搜索关键词，匹配${entity.name}的${entity.attributes.slice(0, 3).map(a => a.name).join('、')}` },
        page: { type: 'number', description: '页码，从1开始', default: 1 },
        pageSize: { type: 'number', description: '每页条数', default: 20 },
      };
      for (const attr of entity.attributes.slice(0, 8)) {
        searchProperties[attr.nameEn || attr.name] = {
          type: mapAttributeType(attr.type),
          description: attr.description || attr.name,
        };
      }

      entityTools.push({
        name: `search_${entityName}`,
        description: `搜索${entity.name}列表。根据关键词或属性条件查询${entity.name}信息。${entity.description ? `定义：${entity.description}` : ''}`,
        inputSchema: { type: 'object', properties: searchProperties },
        domain: 'platform',
        riskLevel: 'READ',
        handler: async (innerArgs, _ctx) => ({
          entity: entityName,
          searchParams: innerArgs,
          message: `搜索${entity.name} — 需要对接项目1 API 实现实际查询`,
        }),
      });

      // ── get_{entity} tool ──
      entityTools.push({
        name: `get_${entityName}`,
        description: `获取单个${entity.name}的详细信息。通过 ID 查询。`,
        inputSchema: {
          type: 'object',
          properties: { id: { type: 'string', description: `${entity.name}的 ID` } },
          required: ['id'],
        },
        domain: 'platform',
        riskLevel: 'READ',
        handler: async (innerArgs, _ctx) => ({
          entity: entityName,
          id: innerArgs.id,
          message: `获取${entity.name}详情 — 需要对接项目1 API 实现`,
        }),
      });
    }

    // Register all entity tools
    _registeredToolNames = entityTools.map(t => t.name);
    _stagedEntityTools = entityTools;
    toolRegistry.registerAll(entityTools);

    // Clear staged state
    _applied = true;

    return {
      content: [{ type: 'text', text: JSON.stringify({
        success: true,
        applied: true,
        message: `本体模型 "${_stagedModel.project.name}" 已注册到 MCP Server。`,
        registeredTools: entityTools.length,
        entities: _stagedModel.entities.length,
        rules: _stagedModel.rules.length,
        tools: entityTools.map(t => ({ name: t.name, description: t.description })),
      }, null, 2) }],
    };
  },
};

// ── disable_ontology_model tool ──

export const disableOntologyModelTool: ToolDefinition = {
  name: 'disable_ontology_model',
  description: '停用当前已注册的本体模型。从 MCP Server 中移除所有实体相关的 search_*/get_* 工具。模型变更的完整步骤：先 disable 旧模型，再 load 新模型预览，最后 apply 新模型。',
  inputSchema: {
    type: 'object',
    properties: {
      confirm: {
        type: 'boolean',
        description: '确认停用。必须为 true 才会执行。',
      },
    },
    required: ['confirm'],
  },
  domain: 'platform',
  riskLevel: 'WRITE',
  handler: async (args) => {
    const confirm = args.confirm === true;

    if (!confirm) {
      return {
        content: [{ type: 'text', text: JSON.stringify({
          success: false,
          message: '未确认。请设置 confirm: true 重新调用。',
        }, null, 2) }],
      };
    }

    if (_registeredToolNames.length === 0) {
      return {
        content: [{ type: 'text', text: JSON.stringify({
          success: true,
          message: '当前没有已注册的本体模型工具。',
          removedTools: 0,
        }, null, 2) }],
      };
    }

    // Remove each registered tool using the new registry methods
    let removedCount = 0;
    for (const name of _registeredToolNames) {
      if (toolRegistry.removeByName(name)) removedCount++;
    }

    // Also remove any leftovers by prefix (edge cases)
    removedCount += toolRegistry.removeByPrefix('search_');
    removedCount += toolRegistry.removeByPrefix('get_');

    _registeredToolNames = [];
    _stagedEntityTools = [];
    _applied = false;

    return {
      content: [{ type: 'text', text: JSON.stringify({
        success: true,
        message: `已停用本体模型，移除了 ${removedCount} 个工具。`,
        removedTools: removedCount,
      }, null, 2) }],
    };
  },
};

// ── Export for init.ts (now returns the three management tools) ──

export function generateEntityTools(): ToolDefinition[] {
  return [uploadOntologyModelTool, applyOntologyModelTool, disableOntologyModelTool];
}

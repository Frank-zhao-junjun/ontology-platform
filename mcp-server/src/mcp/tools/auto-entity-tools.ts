// =============================================
// Entity/Rule Tool Management — Auto-register on upload
//
// Flow:
//   1. upload_ontology_model — upload JSON, auto-register tools into MCP server
//   2. disable_ontology_model — remove registered tools
//
// Model is persisted to a local JSON file so it survives restart.
// =============================================

import * as path from 'path';
import * as fs from 'fs';
import type { ToolDefinition } from '../../types/index.js';
import { toolRegistry } from './registry.js';
import { loadOntologyModel, type OntologyModel } from '../../model-loader.js';

// ── Persistence constants ──
const PERSISTENCE_DIR = process.env.ONTOLOGY_PERSIST_DIR || path.join(process.cwd(), 'data');
const PERSISTENCE_FILE = path.join(PERSISTENCE_DIR, '.ontology-model.persisted.json');

// ── State (current active model) ──
let _currentModel: OntologyModel | null = null;
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

/** Generate entity search_ / get_ tools from a loaded model */
function buildEntityTools(model: OntologyModel): ToolDefinition[] {
  const entityTools: ToolDefinition[] = [];

  for (const entity of model.entities) {
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

  return entityTools;
}

/** Persist the current model to disk so it survives restart */
function persistModel(model: OntologyModel): void {
  try {
    if (!fs.existsSync(PERSISTENCE_DIR)) {
      fs.mkdirSync(PERSISTENCE_DIR, { recursive: true });
    }
    fs.writeFileSync(PERSISTENCE_FILE, JSON.stringify(model, null, 2), 'utf-8');
    console.log(`[auto-entity] Model persisted to ${PERSISTENCE_FILE}`);
  } catch (err) {
    console.error(`[auto-entity] Failed to persist model:`, err);
  }
}

function clearPersistedModel(): void {
  try {
    if (fs.existsSync(PERSISTENCE_FILE)) {
      fs.unlinkSync(PERSISTENCE_FILE);
      console.log(`[auto-entity] Cleared persisted model`);
    }
  } catch (err) {
    console.error(`[auto-entity] Failed to clear persisted model:`, err);
  }
}

// ── upload_ontology_model tool (auto-register) ──

export const uploadOntologyModelTool: ToolDefinition = {
  name: 'upload_ontology_model',
  description: '上传本体模型并自动注册到 MCP Server。传入模型 JSON 正文，系统自动校验并注册实体 search_*/get_* 工具。如需停用请调用 disable_ontology_model。',
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
  riskLevel: 'WRITE',
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

    // ── Unregister previous model if exists ──
    if (_registeredToolNames.length > 0) {
      for (const name of _registeredToolNames) {
        toolRegistry.removeByName(name);
      }
      toolRegistry.removeByPrefix('search_');
      toolRegistry.removeByPrefix('get_');
    }

    // ── Generate and register entity tools ──
    const entityTools = buildEntityTools(model);
    _registeredToolNames = entityTools.map(t => t.name);
    toolRegistry.registerAll(entityTools);
    _currentModel = model;

    // ── Persist to disk ──
    persistModel(model);

    if (entityId) {
      const detail = buildEntityDetail(model, entityId);
      return {
        content: [{ type: 'text', text: JSON.stringify({
          success: true,
          registered: true,
          entity: detail,
          registeredTools: entityTools.length,
        }, null, 2) }],
      };
    }

    // Summary
    const summary = {
      success: true,
      registered: true,
      message: `本体模型 "${model.project.name}" 已注册到 MCP Server（${entityTools.length} 个工具），已持久化，重启不丢失。`,
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
      registeredTools: entityTools.map(t => ({ name: t.name, description: t.description })),
    };

    return {
      content: [{ type: 'text', text: JSON.stringify(summary, null, 2) }],
    };
  },
};

// ── apply_ontology_model REMOVED — upload now auto-registers ──

// ── disable_ontology_model tool ──

export const disableOntologyModelTool: ToolDefinition = {
  name: 'disable_ontology_model',
  description: '停用当前已注册的本体模型。从 MCP Server 中移除所有实体相关的 search_*/get_* 工具，并清除持久化数据。',
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

    // Remove each registered tool
    let removedCount = 0;
    for (const name of _registeredToolNames) {
      if (toolRegistry.removeByName(name)) removedCount++;
    }

    // Also remove any leftovers by prefix (edge cases)
    removedCount += toolRegistry.removeByPrefix('search_');
    removedCount += toolRegistry.removeByPrefix('get_');

    _registeredToolNames = [];
    _currentModel = null;

    // Clear persisted data
    clearPersistedModel();

    return {
      content: [{ type: 'text', text: JSON.stringify({
        success: true,
        message: `已停用本体模型，移除了 ${removedCount} 个工具。`,
        removedTools: removedCount,
      }, null, 2) }],
    };
  },
};

// ── Auto-load from persistence on startup ──

export function autoLoadPersistedModel(): boolean {
  try {
    if (!fs.existsSync(PERSISTENCE_FILE)) {
      console.log('[auto-entity] No persisted model found — skipping auto-load');
      return false;
    }

    const raw = fs.readFileSync(PERSISTENCE_FILE, 'utf-8');
    const model = JSON.parse(raw) as OntologyModel;

    if (!model.version || !model.project || !Array.isArray(model.entities)) {
      console.warn('[auto-entity] Persisted model is invalid — clearing');
      clearPersistedModel();
      return false;
    }

    const entityTools = buildEntityTools(model);
    _registeredToolNames = entityTools.map(t => t.name);
    toolRegistry.registerAll(entityTools);
    _currentModel = model;

    console.log(`[auto-entity] Auto-loaded persisted model "${model.project.name}" (${entityTools.length} tools, ${model.entities.length} entities)`);
    return true;
  } catch (err) {
    console.error('[auto-entity] Failed to auto-load persisted model:', err);
    return false;
  }
}

// ── Export for init.ts (returns the two management tools) ──

export function generateEntityTools(): ToolDefinition[] {
  return [uploadOntologyModelTool, disableOntologyModelTool];
}

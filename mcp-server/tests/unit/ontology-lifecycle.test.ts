// =============================================
// Ontology Model Lifecycle Tests
//   upload  → auto-registered (Agent visible)
//   disable → back to empty
//   re-upload → replaces previous registration
//
// NOTE: Uses dynamic imports so vi.resetModules() can clear module-level
// state between every test.
// =============================================

import { describe, it, expect, beforeEach, vi } from 'vitest';
import type { ToolDefinition } from '../../src/types/index.js';

/** Minimal mock context */
const mockCtx = {
  agentId: 'test',
  tenantId: 'default',
  domains: ['platform'],
  roles: new Map([['platform', 'ADMIN']]),
  tokenId: 'test-token',
};

// ── Fixtures ──

/** Minimal valid model JSON — 2 entities for tool registration tests */
const SAMPLE_MODEL = JSON.stringify({
  version: '1',
  project: { name: 'TestProject', id: 'TP-001' },
  entities: [
    {
      id: 'material',
      name: '物料',
      nameEn: 'material',
      description: '生产物料',
      attributes: [
        { name: '物料编码', nameEn: 'code', type: 'string', required: true, description: '唯一编码' },
        { name: '物料名称', nameEn: 'name', type: 'string', required: true },
      ],
      relations: [],
    },
    {
      id: 'vendor',
      name: '供应商',
      nameEn: 'vendor',
      description: '供应商信息',
      attributes: [
        { name: '供应商编码', nameEn: 'code', type: 'string', required: true },
        { name: '联系人', nameEn: 'contact', type: 'string' },
      ],
      relations: [
        { target: 'material', type: 'supplies', name: '供应' },
      ],
    },
  ],
  stateMachines: [],
  rules: [],
  metrics: [],
  dataSources: [],
  governance: { roles: [] },
});

/** Alternative model with 1 entity to verify replacement */
const SAMPLE_MODEL_ALT = JSON.stringify({
  version: '1',
  project: { name: 'AltProject', id: 'TP-002' },
  entities: [
    {
      id: 'customer',
      name: '客户',
      nameEn: 'customer',
      description: '客户信息',
      attributes: [
        { name: '客户编码', nameEn: 'code', type: 'string', required: true },
      ],
      relations: [],
    },
  ],
  stateMachines: [],
  rules: [],
  metrics: [],
  dataSources: [],
  governance: { roles: [] },
});

/** Parse the text content from a handler result */
function parseResult(result: unknown): Record<string, unknown> {
  const content = (result as { content: Array<{ type: string; text: string }> }).content;
  return JSON.parse(content[0].text);
}

// ── Tests ──

describe('Ontology Model Lifecycle', () => {
  beforeEach(async () => {
    vi.resetModules();
  });

  // ── Phase 1: Upload → Auto-registered ──

  describe('Phase 1: upload → auto-registered', () => {
    let uploadOntologyModelTool: ToolDefinition;
    let toolRegistry: typeof import('../../src/mcp/tools/registry.js')['toolRegistry'];

    beforeEach(async () => {
      vi.resetModules();
      const reg = await import('../../src/mcp/tools/registry.js');
      toolRegistry = reg.toolRegistry;
      toolRegistry.clear();
      const mod = await import('../../src/mcp/tools/auto-entity-tools.js');
      uploadOntologyModelTool = mod.uploadOntologyModelTool;
    });

    it('uploads and registers entity tools immediately', async () => {
      const result = await uploadOntologyModelTool.handler({ modelJson: SAMPLE_MODEL }, mockCtx);
      const data = parseResult(result);

      expect(data.success).toBe(true);
      expect(data.registered).toBe(true);
      expect(data.entities).toHaveLength(2);
      expect((data.entities as Array<Record<string, unknown>>)[0].name).toBe('物料');

      expect(toolRegistry.getTool('search_material')).toBeDefined();
      expect(toolRegistry.getTool('get_material')).toBeDefined();
      expect(toolRegistry.getTool('search_vendor')).toBeDefined();
      expect(toolRegistry.getTool('get_vendor')).toBeDefined();
    });

    it('uploads with entityId returns detail view', async () => {
      const result = await uploadOntologyModelTool.handler(
        { modelJson: SAMPLE_MODEL, entityId: 'material' },
        mockCtx,
      );
      const data = parseResult(result);

      expect(data.success).toBe(true);
      expect(data.registered).toBe(true);
      expect((data.entity as Record<string, unknown>).id).toBe('material');
      expect((data.entity as Record<string, unknown>).attributes).toHaveLength(2);
    });

    it('rejects invalid JSON', async () => {
      const result = await uploadOntologyModelTool.handler({ modelJson: 'not json' }, mockCtx);
      const data = parseResult(result);
      expect(data.success).toBe(false);
    });

    it('rejects malformed model (missing entities)', async () => {
      const badModel = JSON.stringify({ version: '1', project: { name: 'X' } });
      const result = await uploadOntologyModelTool.handler({ modelJson: badModel }, mockCtx);
      const data = parseResult(result);
      expect(data.success).toBe(false);
      expect(data.message as string).toContain('缺少');
    });
  });

  // ── Phase 2: Disable → Removed ──

  describe('Phase 2: disable → removed', () => {
    let uploadOntologyModelTool: ToolDefinition;
    let disableOntologyModelTool: ToolDefinition;
    let toolRegistry: typeof import('../../src/mcp/tools/registry.js')['toolRegistry'];

    beforeEach(async () => {
      vi.resetModules();
      const reg = await import('../../src/mcp/tools/registry.js');
      toolRegistry = reg.toolRegistry;
      toolRegistry.clear();
      const mod = await import('../../src/mcp/tools/auto-entity-tools.js');
      uploadOntologyModelTool = mod.uploadOntologyModelTool;
      disableOntologyModelTool = mod.disableOntologyModelTool;
    });

    it('rejects disable without confirm', async () => {
      await uploadOntologyModelTool.handler({ modelJson: SAMPLE_MODEL }, mockCtx);

      const result = await disableOntologyModelTool.handler({ confirm: false }, mockCtx);
      const data = parseResult(result);
      expect(data.success).toBe(false);
      expect(toolRegistry.getTool('search_material')).toBeDefined();
    });

    it('removes all entity tools from registry', async () => {
      await uploadOntologyModelTool.handler({ modelJson: SAMPLE_MODEL }, mockCtx);

      const result = await disableOntologyModelTool.handler({ confirm: true }, mockCtx);
      const data = parseResult(result);

      expect(data.success).toBe(true);
      expect(data.removedTools).toBeGreaterThanOrEqual(4);

      expect(toolRegistry.getTool('search_material')).toBeUndefined();
      expect(toolRegistry.getTool('get_material')).toBeUndefined();
      expect(toolRegistry.getTool('search_vendor')).toBeUndefined();
      expect(toolRegistry.getTool('get_vendor')).toBeUndefined();
    });

    it('returns no-op when nothing registered', async () => {
      const result = await disableOntologyModelTool.handler({ confirm: true }, mockCtx);
      const data = parseResult(result);
      expect(data.success).toBe(true);
      expect(data.removedTools).toBe(0);
    });
  });

  // ── Phase 3: Re-upload replaces previous registration ──

  describe('Phase 3: re-upload replaces previous registration', () => {
    let uploadOntologyModelTool: ToolDefinition;
    let disableOntologyModelTool: ToolDefinition;
    let toolRegistry: typeof import('../../src/mcp/tools/registry.js')['toolRegistry'];

    beforeEach(async () => {
      vi.resetModules();
      const reg = await import('../../src/mcp/tools/registry.js');
      toolRegistry = reg.toolRegistry;
      toolRegistry.clear();
      const mod = await import('../../src/mcp/tools/auto-entity-tools.js');
      uploadOntologyModelTool = mod.uploadOntologyModelTool;
      disableOntologyModelTool = mod.disableOntologyModelTool;
    });

    it('replaces old entity tools with new ones on re-upload', async () => {
      await uploadOntologyModelTool.handler({ modelJson: SAMPLE_MODEL }, mockCtx);
      expect(toolRegistry.getTool('search_material')).toBeDefined();
      expect(toolRegistry.getTool('search_customer')).toBeUndefined();

      const result = await uploadOntologyModelTool.handler({ modelJson: SAMPLE_MODEL_ALT }, mockCtx);
      const data = parseResult(result);

      expect(data.success).toBe(true);
      expect(data.registered).toBe(true);

      // Old tools removed
      expect(toolRegistry.getTool('search_material')).toBeUndefined();
      expect(toolRegistry.getTool('get_material')).toBeUndefined();
      expect(toolRegistry.getTool('search_vendor')).toBeUndefined();

      // New tools registered
      expect(toolRegistry.getTool('search_customer')).toBeDefined();
      expect(toolRegistry.getTool('get_customer')).toBeDefined();
    });

    it('disable after re-upload removes only current tools', async () => {
      await uploadOntologyModelTool.handler({ modelJson: SAMPLE_MODEL_ALT }, mockCtx);
      await disableOntologyModelTool.handler({ confirm: true }, mockCtx);

      expect(toolRegistry.getTool('search_customer')).toBeUndefined();
      expect(toolRegistry.getTool('get_customer')).toBeUndefined();
    });
  });
});

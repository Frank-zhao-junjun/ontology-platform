// =============================================
// Ontology Model Lifecycle Tests
//   upload → staged (not in registry)
//   apply  → registered (Agent visible)
//   disable → back to staged
//   re-apply → re-registered without re-upload
//
// NOTE: Uses dynamic imports so vi.resetModules() can clear module-level
// state (_stagedModel, _applied, etc.) between every test.
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

/** Parse the text content from a handler result */
function parseResult(result: unknown): Record<string, unknown> {
  const content = (result as { content: Array<{ type: string; text: string }> }).content;
  return JSON.parse(content[0].text);
}

// ── Tests ──

describe('Ontology Model Lifecycle', () => {
  // Each test gets fresh module-level state in auto-entity-tools.ts
  beforeEach(async () => {
    vi.resetModules();
  });

  // ── Phase 1: Upload → Staged ──

  describe('Phase 1: upload → staged', () => {
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

    it('uploads and shows preview (staged=true, applied=false)', async () => {
      const result = await uploadOntologyModelTool.handler({ modelJson: SAMPLE_MODEL }, mockCtx);
      const data = parseResult(result);

      expect(data.success).toBe(true);
      expect(data.staged).toBe(true);
      expect(data.applied).toBe(false);
      expect(data.entities).toHaveLength(2);
      expect((data.entities as Array<Record<string, unknown>>)[0].name).toBe('物料');
    });

    it('uploads with entityId returns detail view', async () => {
      const result = await uploadOntologyModelTool.handler(
        { modelJson: SAMPLE_MODEL, entityId: 'material' },
        mockCtx,
      );
      const data = parseResult(result);

      expect(data.success).toBe(true);
      expect(data.staged).toBe(true);
      expect(data.applied).toBe(false);
      expect((data.entity as Record<string, unknown>).id).toBe('material');
      expect((data.entity as Record<string, unknown>).attributes).toHaveLength(2);
    });

    it('does NOT register any tools on upload', async () => {
      await uploadOntologyModelTool.handler({ modelJson: SAMPLE_MODEL }, mockCtx);

      expect(toolRegistry.getTool('search_material')).toBeUndefined();
      expect(toolRegistry.getTool('get_material')).toBeUndefined();
      expect(toolRegistry.getTool('search_vendor')).toBeUndefined();
      expect(toolRegistry.getTool('get_vendor')).toBeUndefined();
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

  // ── Phase 2: Apply → Registered ──

  describe('Phase 2: apply → registered', () => {
    let uploadOntologyModelTool: ToolDefinition;
    let applyOntologyModelTool: ToolDefinition;
    let toolRegistry: typeof import('../../src/mcp/tools/registry.js')['toolRegistry'];

    beforeEach(async () => {
      vi.resetModules();
      const reg = await import('../../src/mcp/tools/registry.js');
      toolRegistry = reg.toolRegistry;
      toolRegistry.clear();
      const mod = await import('../../src/mcp/tools/auto-entity-tools.js');
      uploadOntologyModelTool = mod.uploadOntologyModelTool;
      applyOntologyModelTool = mod.applyOntologyModelTool;
    });

    it('rejects apply without confirm', async () => {
      await uploadOntologyModelTool.handler({ modelJson: SAMPLE_MODEL }, mockCtx);

      const result = await applyOntologyModelTool.handler({ confirm: false }, mockCtx);
      const data = parseResult(result);
      expect(data.success).toBe(false);
      expect(toolRegistry.getTool('search_material')).toBeUndefined();
    });

    it('rejects apply without prior upload', async () => {
      // Fresh module — _stagedModel is null
      const result = await applyOntologyModelTool.handler({ confirm: true }, mockCtx);
      const data = parseResult(result);
      expect(data.success).toBe(false);
      expect((data.message as string)).toContain('尚未');
    });

    it('registers entity tools after apply', async () => {
      await uploadOntologyModelTool.handler({ modelJson: SAMPLE_MODEL }, mockCtx);

      const result = await applyOntologyModelTool.handler({ confirm: true }, mockCtx);
      const data = parseResult(result);

      expect(data.success).toBe(true);
      expect(data.applied).toBe(true);
      expect(data.registeredTools).toBe(4); // 2 entities × (search_ + get_)

      expect(toolRegistry.getTool('search_material')).toBeDefined();
      expect(toolRegistry.getTool('get_material')).toBeDefined();
      expect(toolRegistry.getTool('search_vendor')).toBeDefined();
      expect(toolRegistry.getTool('get_vendor')).toBeDefined();
    });

    it('blocks double-apply', async () => {
      await uploadOntologyModelTool.handler({ modelJson: SAMPLE_MODEL }, mockCtx);
      await applyOntologyModelTool.handler({ confirm: true }, mockCtx);

      const result = await applyOntologyModelTool.handler({ confirm: true }, mockCtx);
      const data = parseResult(result);
      expect(data.applied).toBe(true); // returns "already applied"
    });
  });

  // ── Phase 3: Disable → Back to Staged ──

  describe('Phase 3: disable → back to staged', () => {
    let uploadOntologyModelTool: ToolDefinition;
    let applyOntologyModelTool: ToolDefinition;
    let disableOntologyModelTool: ToolDefinition;
    let toolRegistry: typeof import('../../src/mcp/tools/registry.js')['toolRegistry'];

    beforeEach(async () => {
      vi.resetModules();
      const reg = await import('../../src/mcp/tools/registry.js');
      toolRegistry = reg.toolRegistry;
      toolRegistry.clear();
      const mod = await import('../../src/mcp/tools/auto-entity-tools.js');
      uploadOntologyModelTool = mod.uploadOntologyModelTool;
      applyOntologyModelTool = mod.applyOntologyModelTool;
      disableOntologyModelTool = mod.disableOntologyModelTool;
    });

    it('rejects disable without confirm', async () => {
      await uploadOntologyModelTool.handler({ modelJson: SAMPLE_MODEL }, mockCtx);
      await applyOntologyModelTool.handler({ confirm: true }, mockCtx);

      const result = await disableOntologyModelTool.handler({ confirm: false }, mockCtx);
      const data = parseResult(result);
      expect(data.success).toBe(false);
      expect(toolRegistry.getTool('search_material')).toBeDefined();
    });

    it('removes all entity tools from registry', async () => {
      await uploadOntologyModelTool.handler({ modelJson: SAMPLE_MODEL }, mockCtx);
      await applyOntologyModelTool.handler({ confirm: true }, mockCtx);

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

  // ── Phase 4: Re-apply without re-upload ──

  describe('Phase 4: re-apply after disable (no re-upload)', () => {
    let uploadOntologyModelTool: ToolDefinition;
    let applyOntologyModelTool: ToolDefinition;
    let disableOntologyModelTool: ToolDefinition;
    let toolRegistry: typeof import('../../src/mcp/tools/registry.js')['toolRegistry'];

    beforeEach(async () => {
      vi.resetModules();
      const reg = await import('../../src/mcp/tools/registry.js');
      toolRegistry = reg.toolRegistry;
      toolRegistry.clear();
      const mod = await import('../../src/mcp/tools/auto-entity-tools.js');
      uploadOntologyModelTool = mod.uploadOntologyModelTool;
      applyOntologyModelTool = mod.applyOntologyModelTool;
      disableOntologyModelTool = mod.disableOntologyModelTool;
    });

    it('re-registers tools from staged model', async () => {
      await uploadOntologyModelTool.handler({ modelJson: SAMPLE_MODEL }, mockCtx);
      await applyOntologyModelTool.handler({ confirm: true }, mockCtx);
      expect(toolRegistry.getTool('search_material')).toBeDefined();

      await disableOntologyModelTool.handler({ confirm: true }, mockCtx);
      expect(toolRegistry.getTool('search_material')).toBeUndefined();

      // Re-apply — NO re-upload needed. _stagedModel is still set,
      // _applied was reset to false by disable.
      const result = await applyOntologyModelTool.handler({ confirm: true }, mockCtx);
      const data = parseResult(result);

      expect(data.success).toBe(true);
      expect(data.applied).toBe(true);
      expect(toolRegistry.getTool('search_material')).toBeDefined();
      expect(toolRegistry.getTool('get_material')).toBeDefined();
      expect(toolRegistry.getTool('search_vendor')).toBeDefined();
      expect(toolRegistry.getTool('get_vendor')).toBeDefined();
    });
  });
});

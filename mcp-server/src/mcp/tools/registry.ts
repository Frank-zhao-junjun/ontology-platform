// =============================================
// Tool Registry — Dynamic tool compilation from Manifest
// =============================================

import type { ToolDefinition } from '../../types/index.js';

class ToolRegistry {
  private tools: Map<string, ToolDefinition> = new Map();

  register(tool: ToolDefinition): void {
    if (this.tools.has(tool.name)) {
      console.warn(`Tool "${tool.name}" already registered, overwriting.`);
    }
    this.tools.set(tool.name, tool);
  }

  registerAll(tools: ToolDefinition[]): void {
    for (const tool of tools) {
      this.register(tool);
    }
  }

  /** Load tools from Manifest JSON (dynamic compilation) */
  loadManifest(manifestJson: Record<string, unknown>): void {
    const actions = (manifestJson.actions || manifestJson.action_definition) as Array<Record<string, unknown>> | undefined;
    if (!actions || !Array.isArray(actions)) return;

    for (const action of actions) {
      const name = `${action.domain || 'default'}.${action.name}`;
      const tool: ToolDefinition = {
        name,
        description: (action.display_name as string) || (action.name as string) || name,
        inputSchema: {
          type: 'object',
          properties: (action.input_schema as Record<string, unknown>) || {},
        },
        domain: (action.domain as string) || 'default',
        riskLevel: (action.risk_level as ToolDefinition['riskLevel']) || 'READ',
        handler: async (args, ctx) => {
          // Dynamic execution via execute-action
          const { executeActionTool } = await import('./execute-action.js');
          return executeActionTool.handler(
            { actionName: name, entityId: action.entity_id as string, params: args },
            ctx
          );
        },
      };
      this.register(tool);
    }
  }

  getTool(name: string): ToolDefinition | undefined {
    return this.tools.get(name);
  }

  listTools(domain?: string[]): ToolDefinition[] {
    const all = Array.from(this.tools.values());
    if (!domain || domain.length === 0) return all;
    return all.filter((t) => domain.includes(t.domain));
  }

  listToolSchemas(domain?: string[]): Array<{
    name: string;
    description: string;
    inputSchema: ToolDefinition['inputSchema'];
  }> {
    const tools = this.listTools(domain);
    return tools.map((t) => ({
      name: t.name,
      description: t.description,
      inputSchema: t.inputSchema,
    }));
  }

  clear(): void {
    this.tools.clear();
  }
}

export const toolRegistry = new ToolRegistry();
export default ToolRegistry;

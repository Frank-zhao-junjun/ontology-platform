// =============================================
// Tool Registry — Dynamic tool compilation from Manifest
// =============================================

import type { ToolDefinition } from '../../types/index.js';

class ToolRegistry {
  private tools: Map<string, ToolDefinition> = new Map();

  /** Resolve versioned name: {baseName}_v{version} or bare name (defaults to v1). */
  private versionedName(tool: ToolDefinition): string {
    const v = tool.version || 1;
    return `${tool.name}_v${v}`;
  }

  register(tool: ToolDefinition): void {
    const vname = this.versionedName(tool);
    if (this.tools.has(vname)) {
      console.warn(`Tool "${vname}" already registered, overwriting.`);
    }
    // Check for deprecated tools — log warning on register
    if (tool.deprecated) {
      const sunset = tool.sunsetAt ? ` — sunsets ${tool.sunsetAt}` : '';
      console.warn(`Tool "${vname}" is DEPRECATED${sunset}`);
    }
    this.tools.set(vname, tool);
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
    // Direct lookup (versioned name like "resolve_intent_v1")
    const direct = this.tools.get(name);
    if (direct) return direct;
    // Fallback: try "_v1" suffix for unversioned names (backward compat)
    return this.tools.get(name + "_v1");
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
    version?: number;
    deprecated?: boolean;
  }> {
    const tools = this.listTools(domain);
    return tools.map((t) => ({
      name: t.name,
      description: t.description + (t.deprecated ? ' [DEPRECATED]' : ''),
      inputSchema: t.inputSchema,
      version: t.version || 1,
      deprecated: t.deprecated || false,
    }));
  }

  /** Remove tools past their sunset date. Call periodically. */
  cleanSunsetTools(): number {
    const now = new Date();
    let removed = 0;
    for (const [name, tool] of this.tools) {
      if (tool.sunsetAt && new Date(tool.sunsetAt) <= now) {
        this.tools.delete(name);
        console.warn(`Removed sunset tool: ${name}`);
        removed++;
      }
    }
    return removed;
  }

  clear(): void {
    this.tools.clear();
  }
}

export const toolRegistry = new ToolRegistry();
export default ToolRegistry;

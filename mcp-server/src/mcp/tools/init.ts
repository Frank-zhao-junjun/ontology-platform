// =============================================
// Tool Initialization — Register all tools
// =============================================

import { toolRegistry } from './registry.js';
import { resolveIntentTool } from './resolve-intent.js';
import { queryOntologyTool } from './query-ontology.js';
import { traverseGraphTool } from './traverse-graph.js';
import { validateInstructionTool } from './validate-instruction.js';
import { executeActionTool } from './execute-action.js';

export function initializeTools(): void {
  toolRegistry.register(resolveIntentTool);
  toolRegistry.register(queryOntologyTool);
  toolRegistry.register(traverseGraphTool);
  toolRegistry.register(validateInstructionTool);
  toolRegistry.register(executeActionTool);
  console.log('[tools] 5 tools registered (4 fixed + 1 dynamic)');
}

export { toolRegistry };
